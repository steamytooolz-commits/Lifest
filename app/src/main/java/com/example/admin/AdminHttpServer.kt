package com.example.admin

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import java.io.InputStream

class AdminHttpServer(
    private val context: Context,
    port: Int = 8080
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri

        return try {
            when {
                uri == "/" || uri == "/index.html" -> {
                    serveAsset("admin/index.html", "text/html")
                }
                uri.endsWith(".css") -> {
                    serveAsset("admin/style.css", "text/css")
                }
                uri.endsWith(".js") -> {
                    serveAsset("admin/app.js", "application/javascript")
                }
                uri == "/api/status" -> {
                    newFixedLengthResponse(
                        Response.Status.OK,
                        "application/json",
                        """{"status":"online","port":$listeningPort,"engine":"NanoHTTPD/2.3.1"}"""
                    )
                }
                else -> {
                    newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "404 Not Found")
                }
            }
        } catch (e: Exception) {
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Error: ${e.message}")
        }
    }

    private fun serveAsset(assetPath: String, mimeType: String): Response {
        val stream: InputStream = context.assets.open(assetPath)
        return newChunkedResponse(Response.Status.OK, mimeType, stream)
    }
}
