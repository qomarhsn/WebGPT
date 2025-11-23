package com.qomarhsn.webgpt

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private val FILE_CHOOSER_REQUEST_CODE = 100
    private val PERMISSION_REQUEST_CODE = 101
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null
    private var pendingPermissionRequest: PermissionRequest? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this).apply {
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
                allowFileAccess = false
                allowContentAccess = true
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                mediaPlaybackRequiresUserGesture = false

                // Note: We don't use forceDark because modern websites like ChatGPT
                // have their own dark mode implementation using CSS media query
                // @media (prefers-color-scheme: dark). WebView automatically passes
                // the system theme preference to websites via this media query.
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: return false
                    
                    return if (shouldOpenInApp(url)) {
                        view?.loadUrl(url)
                        false
                    } else {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        true
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest?) {
                    val resources = request?.resources ?: return
                    if (resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                        if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == 
                            android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            request.grant(arrayOf(PermissionRequest.RESOURCE_AUDIO_CAPTURE))
                        } else {
                            pendingPermissionRequest = request
                            requestPermissions(
                                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                                PERMISSION_REQUEST_CODE
                            )
                        }
                    } else {
                        super.onPermissionRequest(request)
                    }
                }

                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    fileUploadCallback?.onReceiveValue(null)
                    fileUploadCallback = filePathCallback

                    try {
                        val intent = fileChooserParams?.createIntent()
                        try {
                            startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE)
                        } catch (e: Exception) {
                            val genericIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                            }
                            startActivityForResult(genericIntent, FILE_CHOOSER_REQUEST_CODE)
                        }
                    } catch (e: Exception) {
                        fileUploadCallback = null
                        return false
                    }
                    return true
                }
            }

            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                try {
                    val request = android.app.DownloadManager.Request(Uri.parse(url))
                    request.setMimeType(mimetype)
                    request.addRequestHeader("User-Agent", userAgent)
                    request.setDescription("Downloading file...")
                    request.setTitle(android.webkit.URLUtil.guessFileName(url, contentDisposition, mimetype))
                    request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.setDestinationInExternalPublicDir(
                        android.os.Environment.DIRECTORY_DOWNLOADS,
                        android.webkit.URLUtil.guessFileName(url, contentDisposition, mimetype)
                    )
                    
                    val dm = getSystemService(android.content.Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
                    dm.enqueue(request)
                    android.widget.Toast.makeText(applicationContext, "Downloading...", android.widget.Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    android.widget.Toast.makeText(applicationContext, "Download failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        val urlToLoad = intent?.dataString ?: "https://chatgpt.com"
        webView.loadUrl(urlToLoad)

        setContentView(webView)
        applySystemTheme()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                var results: Array<Uri>? = null
                if (data?.data != null) {
                    results = arrayOf(data.data!!)
                } else if (data?.clipData != null) {
                    val count = data.clipData!!.itemCount
                    results = Array(count) { i -> data.clipData!!.getItemAt(i).uri }
                }
                fileUploadCallback?.onReceiveValue(results)
            } else {
                fileUploadCallback?.onReceiveValue(null)
            }
            fileUploadCallback = null
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                pendingPermissionRequest?.grant(arrayOf(PermissionRequest.RESOURCE_AUDIO_CAPTURE))
            } else {
                pendingPermissionRequest?.deny()
            }
            pendingPermissionRequest = null
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.dataString?.let { url ->
            if (shouldOpenInApp(url)) {
                webView.loadUrl(url)
            }
        }
    }

    /**
     * Determines if a URL should open in the app or system browser.
     * Validates the actual domain to prevent spoofing.
     * Returns true only if the URL is from chatgpt.com, auth.openai.com, or specific login pages.
     */
    private fun shouldOpenInApp(url: String): Boolean {
        return try {
            val uri = android.net.Uri.parse(url)
            val host = uri.host?.lowercase() ?: return false
            
            if (host == "chatgpt.com" ||
                host.endsWith(".chatgpt.com") ||
                host == "auth.openai.com" ||
                host.endsWith(".auth.openai.com")) {
                return true
            }
            
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Applies system theme to status bar appearance and colors.
     * Dark mode: #212121 background, white icons
     * Light mode: #ffffff background, dark icons
     */
    private fun applySystemTheme() {
        val isDarkMode = (resources.configuration.uiMode and 
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.statusBarColor = if (isDarkMode) {
                android.graphics.Color.parseColor("#212121")
            } else {
                android.graphics.Color.parseColor("#ffffff")
            }
            
            window.insetsController?.setSystemBarsAppearance(
                if (isDarkMode) 0 else WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = if (isDarkMode) {
                android.graphics.Color.parseColor("#212121")
            } else {
                android.graphics.Color.parseColor("#ffffff")
            }
            
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = if (isDarkMode) {
                0
            } else {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        } else {
            window.statusBarColor = if (isDarkMode) {
                android.graphics.Color.parseColor("#212121")
            } else {
                android.graphics.Color.parseColor("#ffffff")
            }
        }
    }

    // Note: onBackPressed() is deprecated in favor of OnBackPressedCallback,
    // but OnBackPressedCallback requires androidx.activity.ComponentActivity.
    // We use plain Activity for minimal APK size (no androidx dependencies).
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }
}
