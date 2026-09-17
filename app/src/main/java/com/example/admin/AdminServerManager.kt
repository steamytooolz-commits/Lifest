package com.example.admin

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.Inet4Address
import java.net.NetworkInterface

object AdminServerManager {
    private const val TAG = "AdminServerManager"
    private var server: AdminHttpServer? = null

    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

    private val _serverUrl = MutableStateFlow<String?>(null)
    val serverUrl: StateFlow<String?> = _serverUrl.asStateFlow()

    private val _serverLogs = MutableStateFlow<List<String>>(emptyList())
    val serverLogs: StateFlow<List<String>> = _serverLogs.asStateFlow()

    fun log(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        val entry = "[$timestamp] $message"
        _serverLogs.value = (_serverLogs.value + entry).takeLast(100)
        Log.d(TAG, message)
    }

    @Synchronized
    fun startServer(context: Context, preferredPort: Int = 8080): Result<String> {
        if (_isServerRunning.value && server != null) {
            val url = _serverUrl.value ?: "http://localhost:$preferredPort"
            log("Server already active at $url")
            return Result.success(url)
        }

        var port = preferredPort
        var started = false
        var lastError: Exception? = null

        // Try preferred port, then 8081, 8082 fallback
        for (attempt in 0..2) {
            val currentPort = port + attempt
            try {
                val newServer = AdminHttpServer(context.applicationContext, currentPort)
                newServer.start()
                server = newServer
                port = currentPort
                started = true
                break
            } catch (e: Exception) {
                lastError = e
                log("Port $currentPort occupied or failed: ${e.message}")
            }
        }

        if (!started) {
            val errMsg = "Failed to bind Admin HTTP Server: ${lastError?.message}"
            log(errMsg)
            return Result.failure(Exception(errMsg))
        }

        val ip = getLocalIpAddress() ?: "127.0.0.1"
        val url = "http://$ip:$port"
        _isServerRunning.value = true
        _serverUrl.value = url
        log("Embedded Admin Server online at $url (port $port)")

        return Result.success(url)
    }

    @Synchronized
    fun stopServer() {
        try {
            server?.stop()
            server = null
            _isServerRunning.value = false
            _serverUrl.value = null
            log("Embedded Admin Server stopped")
        } catch (e: Exception) {
            log("Error stopping server: ${e.message}")
        }
    }

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving local IP", e)
        }
        return "127.0.0.1"
    }
}
