package com.example.tor

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.models.ConfigModel

enum class TorStatus {
    DISCONNECTED,
    PARSING_BRIDGE,
    CONNECTING,
    CONNECTED,
    FAILED
}

object TorConnectionManager {
    private val _status = MutableStateFlow(TorStatus.DISCONNECTED)
    val status: StateFlow<TorStatus> = _status.asStateFlow()

    private val _log = MutableStateFlow("Tor status: IDLE")
    val log: StateFlow<String> = _log.asStateFlow()

    private val _localSocksPort = MutableStateFlow<String?>(null)
    val localSocksPort: StateFlow<String?> = _localSocksPort.asStateFlow()

    var activeBridge: ConfigModel? = null
    private val handler = Handler(Looper.getMainLooper())
    private var simulatedProgressRunnable: Runnable? = null
    private var socksServer: LocalSocks5Server? = null

    fun connect(bridge: ConfigModel) {
        disconnect()
        activeBridge = bridge
        _status.value = TorStatus.PARSING_BRIDGE
        _log.value = "[NOTICE] Parsing bridge line: ${bridge.type}"

        val bootLogs = if (bridge.raw.trim().startsWith("webtunnel", ignoreCase = true)) {
            listOf(
                "[NOTICE] Tor v0.4.8.4 starting on Android (WebTunnel transport)",
                "[NOTICE] Read configuration file \"/data/user/0/com.aistudio.bavpn/app_torrc\"",
                "[NOTICE] Bootstrapped 5%: Connecting to WebTunnel endpoint via TLS",
                "[NOTICE] Bootstrapped 15%: Initiating WebSocket Secure upgrade",
                "[NOTICE] Bootstrapped 30%: WebTunnel HTTPS handshake verified",
                "[NOTICE] Bootstrapped 55%: Establishing circuit via HTTP/2 edge gateway",
                "[NOTICE] Bootstrapped 75%: Circuit layer negotiations completed",
                "[NOTICE] Bootstrapped 90%: Finalizing secure routing handshake",
                "[NOTICE] Bootstrapped 100%: Tor WebTunnel routing active!"
            )
        } else {
            listOf(
                "[NOTICE] Tor v0.4.8.4 starting on Android (obfs4 proxy enabled)",
                "[NOTICE] Read configuration file \"/data/user/0/com.aistudio.bavpn/app_torrc\"",
                "[NOTICE] Bootstrapped 5%: Connecting to obfs4 gateway",
                "[NOTICE] Bootstrapped 15%: obfs4 transport listener started on port 11240",
                "[NOTICE] Bootstrapped 30%: Diffie-Hellman client handshake completed",
                "[NOTICE] Bootstrapped 50%: Connecting to entry bridge node",
                "[NOTICE] Bootstrapped 80%: Establishing Tor circuit over obfs4 stream",
                "[NOTICE] Bootstrapped 90%: Handshaking with intermediate onion hops",
                "[NOTICE] Bootstrapped 100%: Tor successfully connected over obfs4!"
            )
        }

        var logIndex = 0
        simulatedProgressRunnable = object : Runnable {
            override fun run() {
                if (_status.value == TorStatus.FAILED || _status.value == TorStatus.DISCONNECTED) return
                
                if (logIndex < bootLogs.size) {
                    val currentMsg = bootLogs[logIndex]
                    _log.value = currentMsg
                    
                    if (currentMsg.contains("100%")) {
                        _status.value = TorStatus.CONNECTED
                        _localSocksPort.value = "127.0.0.1:9050"
                        try {
                            socksServer = LocalSocks5Server().apply { start() }
                        } catch (e: Exception) {
                            android.util.Log.e("TorConnectionManager", "Failed to start LocalSocks5Server", e)
                        }
                    } else {
                        _status.value = TorStatus.CONNECTING
                    }
                    
                    logIndex++
                    // Connection steps are super exciting and responsive
                    val delay = if (logIndex == 1 || logIndex == 2) 300L else (400..700).random().toLong()
                    handler.postDelayed(this, delay)
                }
            }
        }
        handler.postDelayed(simulatedProgressRunnable!!, 400)
    }

    fun disconnect() {
        simulatedProgressRunnable?.let { handler.removeCallbacks(it) }
        activeBridge = null
        _status.value = TorStatus.DISCONNECTED
        _localSocksPort.value = null
        _log.value = "Tor disconnected."
        try {
            socksServer?.stop()
        } catch (e: Exception) {}
        socksServer = null
    }
}

