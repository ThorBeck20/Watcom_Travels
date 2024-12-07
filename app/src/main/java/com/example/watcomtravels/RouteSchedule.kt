package com.example.watcomtravels

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.watcomtravels.ui.theme.AppTheme


class RouteSchedule : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("url")

        val webView = WebView(this).apply {
            webViewClient = WebViewClient() // Ensures the WebView handles navigation
            //settings.javaScriptEnabled = true // Enable JavaScript if required
            loadUrl(url ?: "file:///android_asset/route-details.html")
        }

        setContentView(webView)
    }
}
