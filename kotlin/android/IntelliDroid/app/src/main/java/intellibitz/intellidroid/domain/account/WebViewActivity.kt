package intellibitz.intellidroid.domain.account

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import intellibitz.intellidroid.R
import intellibitz.intellidroid.data.ContactItem

class WebViewActivity : AppCompatActivity() {

    private var webView: WebView? = null
    private var user: ContactItem? = null

    override fun onResume() {
        super.onResume()
        user?.emailURL?.let { url ->
            webView?.loadUrl(url)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        user = intent.getParcelableExtra(ContactItem.USER_CONTACT)
        webView = findViewById<WebView>(R.id.webview).apply {
            clearHistory()
            clearFormData()
            clearCache(true)
            settings.javaScriptEnabled = true
            webViewClient = MyWebViewClient()
        }
    }

    private inner class MyWebViewClient : WebViewClient() {
        @Deprecated("Deprecated in Java")
        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            val done = url != null && url.contains("code=")
            if (done && url != null) {
                val start = url.indexOf("code=")
                if (start >= 0) {
                    val codedQuery = url.substring(start)
                    val code = codedQuery.substring(5)
                    user?.emailCode = code
                    user?.emailURL = url

                    val intent = intent.apply {
                        putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
            return done
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val uri = request?.url
            val url = uri?.toString()
            val done = url != null && url.contains("code=")
            if (done && url != null) {
                val start = url.indexOf("code=")
                if (start >= 0) {
                    val codedQuery = url.substring(start)
                    val code = codedQuery.substring(5)
                    user?.emailCode = code
                    user?.emailURL = url

                    val intent = intent.apply {
                        putExtra(ContactItem.USER_CONTACT, user as? Parcelable)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
            return done
        }
    }
}
