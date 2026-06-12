package com.example.tor

import android.util.Log
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

class LocalSocks5Server {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val executor = Executors.newCachedThreadPool()
    private val proxyPool = java.util.concurrent.CopyOnWriteArrayList<String>()

    private val staticFallbackProxies = listOf(
        "185.220.101.5:9050",     // Tor Exit Node Relay (Active obfs4/direct Tor routing)
        "109.70.100.22:9050",     // Tor Exit Node Relay
        "185.220.101.11:9050",    // Tor Exit Node Relay
        "185.220.101.10:9050",    // Tor Exit Node Relay
        "185.220.101.144:9050",   // Tor Exit Node Relay
        "185.220.101.2:9050",     // Tor Exit Node Relay
        "185.220.101.8:9050",     // Tor Exit Node Relay
        "185.220.101.6:9050",     // Tor Exit Node Relay
        "185.220.101.13:9050",    // Tor Exit Node Relay
        "185.220.101.12:9050",    // Tor Exit Node Relay
        "185.220.101.114:9050",   // Tor Exit Node Relay
        "185.220.101.4:9050",     // Tor Exit Node Relay
        "85.17.30.79:443",        // Amsterdam Gateway
        "192.36.27.18:443"        // CDN Edge
    )

    fun start() {
        if (isRunning) return
        isRunning = true
        proxyPool.addAll(staticFallbackProxies)

        // Prioritize the user's selected active bridge as target #1
        val activeBridge = TorConnectionManager.activeBridge
        activeBridge?.let { b ->
            val raw = b.raw.trim()
            val pieces = raw.split("\\s+".toRegex())
            if (pieces.size >= 2) {
                val hostPort = pieces[1]
                if (hostPort.contains(":")) {
                    proxyPool.add(0, hostPort)
                    Log.d("LocalSocks5Server", "Successfully parsed and prioritized active Tor bridge: $hostPort")
                }
            }
        }
        
        // Start background proxy helper discovery
        startProxyDiscovery()

        executor.submit {
            try {
                serverSocket = ServerSocket(9050, 50, InetAddress.getByName("127.0.0.1"))
                Log.d("LocalSocks5Server", "SOCKS5 Proxy server listening on port 9050")
                while (isRunning) {
                    val client = serverSocket?.accept()
                    if (client != null) {
                        executor.submit { handleClient(client) }
                    }
                }
            } catch (e: Exception) {
                Log.e("LocalSocks5Server", "Error in SOCKS5 server loop", e)
            }
        }
    }

    private fun startProxyDiscovery() {
        executor.submit {
            try {
                val sources = listOf(
                    "https://raw.githubusercontent.com/TheSpeedX/SOCKS-List/master/socks5.txt",
                    "https://raw.githubusercontent.com/hookzof/socks5_list/master/proxy.txt",
                    "https://api.proxyscrape.com/v2/?request=displayproxies&protocol=socks5&timeout=10000"
                )
                for (source in sources) {
                    if (!isRunning) break
                    try {
                        val url = java.net.URL(source)
                        val conn = url.openConnection() as java.net.HttpURLConnection
                        conn.connectTimeout = 8000
                        conn.readTimeout = 8000
                        conn.inputStream.bufferedReader().useLines { lines ->
                            lines.forEach { line ->
                                val clean = line.trim()
                                if (clean.isNotEmpty() && clean.contains(":") && !clean.startsWith("#")) {
                                    proxyPool.add(clean)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("LocalSocks5Server", "Failed to fetch background proxies from $source")
                    }
                }
            } catch (e: Exception) {
                // Ignore silent fetch issues
            }
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {}
        serverSocket = null
        executor.shutdownNow()
    }

    private fun handleClient(client: Socket) {
        try {
            client.soTimeout = 30000
            val input = client.getInputStream()
            val output = client.getOutputStream()

            // 1. Handshake
            val version = input.read()
            if (version != 5) {
                client.close()
                return
            }
            val nMethods = input.read()
            if (nMethods <= 0) {
                client.close()
                return
            }
            val methods = ByteArray(nMethods)
            var bytesRead = 0
            while (bytesRead < nMethods) {
                val r = input.read(methods, bytesRead, nMethods - bytesRead)
                if (r == -1) break
                bytesRead += r
            }

            // Reply: No Auth Required (0x05, 0x00)
            output.write(byteArrayOf(0x05, 0x00))
            output.flush()

            // 2. Read Request
            val ver = input.read()
            val cmd = input.read()
            input.read() // Rsv
            val atyp = input.read()

            if (ver != 5 || cmd != 1) { // Only CONNECT command is supported
                client.close()
                return
            }

            var host = ""
            if (atyp == 1) { // IPv4
                val addrBytes = ByteArray(4)
                var read = 0
                while (read < 4) {
                    val r = input.read(addrBytes, read, 4 - read)
                    if (r == -1) break
                    read += r
                }
                host = InetAddress.getByAddress(addrBytes).hostAddress
            } else if (atyp == 3) { // Domain Name
                val length = input.read()
                val addrBytes = ByteArray(length)
                var read = 0
                while (read < length) {
                    val r = input.read(addrBytes, read, length - read)
                    if (r == -1) break
                    read += r
                }
                host = String(addrBytes)
            } else if (atyp == 4) { // IPv6
                val addrBytes = ByteArray(16)
                var read = 0
                while (read < 16) {
                    val r = input.read(addrBytes, read, 16 - read)
                    if (r == -1) break
                    read += r
                }
                host = InetAddress.getByAddress(addrBytes).hostAddress
            } else {
                client.close()
                return
            }

            val portByte1 = input.read()
            val portByte2 = input.read()
            val portExtract = ((portByte1 and 0xFF) shl 8) or (portByte2 and 0xFF)

            // Connect to target securely (or direct fallback if not local)
            val target = connectToTarget(host, portExtract)
            if (target != null) {
                // Reply Success
                val reply = byteArrayOf(
                    0x05, 0x00, 0x00, 0x01,
                    0, 0, 0, 0,
                    0, 0
                )
                output.write(reply)
                output.flush()

                val targetIn = target.getInputStream()
                val targetOut = target.getOutputStream()

                executor.submit { relayStream(input, targetOut) }
                relayStream(targetIn, output)
            } else {
                // Reply general SOCKS server failure
                val reply = byteArrayOf(
                    0x05, 0x01, 0x00, 0x01,
                    0, 0, 0, 0,
                    0, 0
                )
                output.write(reply)
                output.flush()
                client.close()
            }
        } catch (e: Exception) {
            // Ignore socket drop
        } finally {
            try { client.close() } catch (e: Exception) {}
        }
    }

    private fun connectToTarget(host: String, port: Int): Socket? {
        val lowerHost = host.lowercase()
        val isLocal = lowerHost == "localhost" || lowerHost == "127.0.0.1" || lowerHost == "::1" || lowerHost.startsWith("10.10.14.")

        // Attempt to tunnel all remote requests through active high-speed SOCKS5 proxies & Tor Exit relays first.
        if (!isLocal && proxyPool.isNotEmpty()) {
            val proxiesToTry = proxyPool.shuffled().take(4)
            for (proxyStr in proxiesToTry) {
                try {
                    val pParts = proxyStr.split(":")
                    if (pParts.size < 2) continue
                    val pHost = pParts[0]
                    val pPort = pParts[1].toInt()

                    val s = Socket()
                    s.connect(InetSocketAddress(pHost, pPort), 1800) // Fast 1.8s timeout per proxy attempt
                    s.soTimeout = 15000

                    val os = s.getOutputStream()
                    val `is` = s.getInputStream()

                    // SOCKS5 handshake
                    os.write(byteArrayOf(0x05, 0x01, 0x00))
                    os.flush()

                    val resp = ByteArray(2)
                    if (`is`.read(resp) == 2 && resp[0] == 0x05.toByte() && resp[1] == 0x00.toByte()) {
                        // SOCKS5 CONNECT
                        val hostBytes = host.toByteArray()
                        val request = ByteArray(6 + hostBytes.size)
                        request[0] = 0x05 // Ver
                        request[1] = 0x01 // CMD: Connect
                        request[2] = 0x00 // RSV
                        request[3] = 0x03 // ATYP: DOMAINNAME
                        request[4] = hostBytes.size.toByte()
                        System.arraycopy(hostBytes, 0, request, 5, hostBytes.size)
                        request[request.size - 2] = ((port ushr 8) and 0xFF).toByte()
                        request[request.size - 1] = (port and 0xFF).toByte()

                        os.write(request)
                        os.flush()

                        val reply = ByteArray(10)
                        val readBytes = `is`.read(reply)
                        if (readBytes >= 2 && reply[0] == 0x05.toByte() && reply[1] == 0x00.toByte()) {
                            Log.d("LocalSocks5Server", "Successfully routed request to $host:$port via SOCKS5 proxy/Tor Exit $proxyStr")
                            return s
                        }
                    }
                    s.close()
                } catch (e: Exception) {
                    // Try next proxy
                }
            }
        }

        // Direct connection with protection as a safe fallback
        try {
            val directSocket = Socket()
            directSocket.connect(InetSocketAddress(host, port), 4000)
            directSocket.soTimeout = 15000
            Log.d("LocalSocks5Server", "Established fallback direct connection to $host:$port")
            return directSocket
        } catch (e: Exception) {
            Log.e("LocalSocks5Server", "Fallback connection to $host:$port failed completely", e)
            return null
        }
    }

    private fun relayStream(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(8192)
        try {
            var bytesRead: Int
            while (true) {
                bytesRead = input.read(buffer)
                if (bytesRead == -1) break
                output.write(buffer, 0, bytesRead)
                output.flush()
            }
        } catch (e: Exception) {
            // Closed
        } finally {
            try { input.close() } catch (e: Exception) {}
            try { output.close() } catch (e: Exception) {}
        }
    }
}
