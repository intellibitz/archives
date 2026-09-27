package intellibitz.intellidroid.domain.account;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import intellibitz.intellidroid.data.ContactItem;
import intellibitz.intellidroid.R;
import intellibitz.intellidroid.data.ContactItem;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;
    private ContactItem user;

    @Override
    protected void onResume() {
        super.onResume();
        webView.loadUrl(user.getEmailURL());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        user = getIntent().getParcelableExtra(ContactItem.USER_CONTACT);
        webView = (WebView) findViewById(R.id.webview);
        webView.clearHistory();
        webView.clearFormData();
        webView.clearCache(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(false);
        webView.setWebViewClient(new MyWebViewClient());
    }

    private class MyWebViewClient extends WebViewClient {
        /**
         * Give the host application a chance to take over the control when a new
         * url is about to be loaded in the current WebView. If WebViewClient is not
         * provided, by default WebView will ask Activity Manager to choose the
         * proper handler for the url. If WebViewClient is provided, return true
         * means the host application handles the url, while return false means the
         * current WebView handles the url.
         * This method is not called for requests using the POST "method".
         *
         * @param view The WebView that is initiating the callback.
         * @param url  The url to be loaded.
         * @return True if the host application wants to leave the current WebView
         * and handle the url itself, otherwise return false.
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            boolean done = (null != url && url.contains("code="));
            if (done) {
                int start = url.indexOf("code=");
                if (start >= 0) {
                    String codedQuery = url.substring(start);
                    String code = codedQuery.substring(5);
                    user.setEmailCode(code);
                    user.setEmailURL(url);
//                    MainApplicationSingleton mainApplication = MainApplicationSingleton.getInstance();
//                    mainApplication.putGlobalVariable(MainApplicationSingleton.EMAIL_PARAM, mEmail);
//                    mainApplication.putGlobalVariable(MainApplicationSingleton.CODE_PARAM, code);

//                    Bundle bundle = new Bundle();
//                    bundle.putString(MainApplicationSingleton.UID_PARAM, uid);
//                    bundle.putString(MainApplicationSingleton.TOKEN_PARAM, token);
//                    bundle.putString(MainApplicationSingleton.DEVICE_PARAM, device);
//                    bundle.putString(MainApplicationSingleton.EMAIL_PARAM, mEmail);
//                    bundle.putString(MainApplicationSingleton.CODE_PARAM, code);
//                    bundle.putString(MainApplicationSingleton.URL_PARAM, url);

//        this activity, is called for result with this intent.. so the parent is aware
                    Intent intent = getIntent();
                    intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                    setResult(Activity.RESULT_OK, intent);
//        startActivity(intent);
                    finish();

//                Start the get token activity
//                    Intent intent = new Intent(WebViewActivity.this, NewEmailGetTokenActivity.class);
//                    intent.putExtra(ContactItem.TAG, (Parcelable)user);
//                    intent.putExtras(bundle);
//                    startActivity(intent);
//                    finish();
                }
            }
            return done;
            // This is my web site, so do not override; let my WebView load the page
//            view.loadUrl(url);
//            return false;
//                // TODO: 06-02-2016
//                to handle failures.. redirect back to registration
/*
            if (isComplete) {
                return true;
            } else {
            }
*/
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            String url = uri.getPath();
            boolean done = (null != url && url.contains("code="));
            if (done) {
                int start = url.indexOf("code=");
                if (start >= 0) {
                    String codedQuery = url.substring(start);
                    String code = codedQuery.substring(5);
                    user.setEmailCode(code);
                    user.setEmailURL(url);
//                    MainApplicationSingleton mainApplication = MainApplicationSingleton.getInstance();
//                    mainApplication.putGlobalVariable(MainApplicationSingleton.EMAIL_PARAM, mEmail);
//                    mainApplication.putGlobalVariable(MainApplicationSingleton.CODE_PARAM, code);

//                    Bundle bundle = new Bundle();
//                    bundle.putString(MainApplicationSingleton.UID_PARAM, uid);
//                    bundle.putString(MainApplicationSingleton.TOKEN_PARAM, token);
//                    bundle.putString(MainApplicationSingleton.DEVICE_PARAM, device);
//                    bundle.putString(MainApplicationSingleton.EMAIL_PARAM, mEmail);
//                    bundle.putString(MainApplicationSingleton.CODE_PARAM, code);
//                    bundle.putString(MainApplicationSingleton.URL_PARAM, url);

//        this activity, is called for result with this intent.. so the parent is aware
                    Intent intent = getIntent();
                    intent.putExtra(ContactItem.USER_CONTACT, (Parcelable) user);
                    setResult(Activity.RESULT_OK, intent);
//        startActivity(intent);
                    finish();

//                Start the get token activity
//                    Intent intent = new Intent(WebViewActivity.this, NewEmailGetTokenActivity.class);
//                    intent.putExtra(ContactItem.TAG, (Parcelable)user);
//                    intent.putExtras(bundle);
//                    startActivity(intent);
//                    finish();
                }
            }
            return done;
            // This is my web site, so do not override; let my WebView load the page
//            view.loadUrl(url);
//            return false;
//                // TODO: 06-02-2016
//                to handle failures.. redirect back to registration
/*
            if (isComplete) {
                return true;
            } else {
            }
*/
        }
    }

}
