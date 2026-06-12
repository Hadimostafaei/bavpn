package com.bavpn.tor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.system.OsConstants
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import java.io.File
import java.io.FileOutputStream

class BavpnVpnService : VpnService() {

    companion object {
        const val ACTION_START_VPN = "com.bavpn.tor.ACTION_START_VPN"
        const val ACTION_STOP_VPN  = "com.bavpn.tor.ACTION_STOP_VPN"
        const val EXTRA_SOCKS_PORT = "socks_port"
        private const val TAG = "BavpnVpnService"
        private const val CHANNEL_ID = "BavpnVpnChannel"
        private const val NOTIFICATION_ID = 1002
    }

    private var tunInterface: ParcelFileDescriptor? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_VPN -> {
                val socksPort = intent.getIntExtra(EXTRA_SOCKS_PORT, 9050)
                startVpn(socksPort)
            }
            ACTION_STOP_VPN -> stopVpn()
        }
        return START_NOT_STICKY
    }

    private fun startVpn(socksPort: Int) {
        try {
            stopVpn()

            // Register service in Foreground to ensure survival when minimized
            val notification = buildNotification(socksPort)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID, 
                    notification, 
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }

            val builder = Builder()
                .setSession("BAVPN")
                .addAddress(TProxyService.VIRTUAL_GATEWAY_IPV4, 32)
                .addAddress(TProxyService.VIRTUAL_GATEWAY_IPV6, 128)
                .setMtu(TProxyService.TUNNEL_MTU)
                .addRoute("0.0.0.0", 0)
                .addRoute("::", 0)
                .addDnsServer(TProxyService.FAKE_DNS)
                .addDisallowedApplication(packageName)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setMetered(false)
                builder.allowFamily(OsConstants.AF_INET)
                builder.allowFamily(OsConstants.AF_INET6)
            }

            tunInterface = builder.establish()

            tunInterface?.let { tun ->
                val conf = createHevConfig(socksPort)
                Log.d(TAG, "Starting TProxy with fd=${tun.fd} socks=$socksPort")
                try {
                    TProxyService.TProxyStartService(conf.absolutePath, tun.fd)
                    Log.d(TAG, "VPN tunnel active")
                } catch (e: UnsatisfiedLinkError) {
                    Log.e(TAG, "Native TProxyStartService call failed, falling back to local gateway", e)
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting TProxyService", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "VPN start error", e)
        }
    }

    private fun stopVpn() {
        try {
            TProxyService.TProxyStopService()
        } catch (_: Exception) {
        } catch (_: UnsatisfiedLinkError) {}
        try {
            tunInterface?.close()
            tunInterface = null
        } catch (_: Exception) {}
    }

    private fun createHevConfig(socksPort: Int): File {
        val file = File(cacheDir, "tproxy.conf")
        val config = """
misc:
  log-level: warn
  task-stack-size: ${TProxyService.TASK_SIZE}
tunnel:
  ipv4: ${TProxyService.VIRTUAL_GATEWAY_IPV4}
  ipv6: '${TProxyService.VIRTUAL_GATEWAY_IPV6}'
  mtu: ${TProxyService.TUNNEL_MTU}
socks5:
  port: $socksPort
  address: 127.0.0.1
  udp: 'udp'
mapdns:
  address: ${TProxyService.FAKE_DNS}
  port: 53
  network: 240.0.0.0
  netmask: 240.0.0.0
  cache-size: 10000
""".trimIndent()
        FileOutputStream(file, false).use { it.write(config.toByteArray()) }
        return file
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    private fun buildNotification(socksPort: Int): Notification {
        val clickIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, clickIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Bavpn TProxy Service Active")
            .setContentText("Tunneling traffic through local SOCKS proxy on port $socksPort")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bavpn TProxy Service Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps raw TProxy service active when app is closed or minimized"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
