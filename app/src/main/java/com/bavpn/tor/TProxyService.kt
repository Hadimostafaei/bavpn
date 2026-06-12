package com.bavpn.tor

import android.util.Log

object TProxyService {
    @JvmStatic
    external fun TProxyStartService(config_path: String?, fd: Int)
    @JvmStatic
    external fun TProxyStopService()
    external fun TProxyGetStats(): LongArray?

    var isLibraryLoaded = false

    init {
        try {
            System.loadLibrary("hev-socks5-tunnel")
            isLibraryLoaded = true
            Log.d("TProxyService", "Successfully loaded hev-socks5-tunnel library")
        } catch (e: UnsatisfiedLinkError) {
            isLibraryLoaded = false
            Log.e("TProxyService", "Failed to load hev-socks5-tunnel library, using secure tunnels fallback", e)
        }
    }

    const val VIRTUAL_GATEWAY_IPV4 = "198.18.0.1"
    const val VIRTUAL_GATEWAY_IPV6 = "fc00::1"
    const val FAKE_DNS = "198.18.0.2"
    const val TASK_SIZE = 81920
    const val TUNNEL_MTU = 8500
}
