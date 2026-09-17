package com.example.presentation.ui.components

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.remote.PuterBridge

/**
 * Headless Puter.js runtime bridge Composable.
 * Hosts a hidden 1dp x 1dp WebView initialized with SOFTWARE rendering layer,
 * enabling Puter.js (https://docs.puter.com) free AI inference, streaming,
 * image generation, TTS, and KV cloud storage.
 */
@Composable
fun PuterBridgeWebView(
    puterBridge: PuterBridge,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.size(1.dp),
        factory = { context ->
            try {
                WebView(context).apply {
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                    visibility = View.INVISIBLE
                    layoutParams = ViewGroup.LayoutParams(1, 1)
                    puterBridge.attachWebView(this)
                }
            } catch (e: Throwable) {
                // Fallback placeholder if WebView runtime fails to load
                View(context).apply {
                    visibility = View.GONE
                    layoutParams = ViewGroup.LayoutParams(1, 1)
                }
            }
        }
    )
}
