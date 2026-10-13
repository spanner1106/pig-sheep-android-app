package com.pigsheep.online;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Intent;

/** Full-screen, landscape WebView wrapper for the hosted Pig Sheep game. */
public class MainActivity extends Activity {
    private static final String GAME_URL = "https://pig-sheep-online.onrender.com/";
    private static final String ALLOWED_HOST = "pig-sheep-online.onrender.com";
    private WebView webView;
    private boolean showingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(Color.rgb(13, 45, 99));
        getWindow().setNavigationBarColor(Color.rgb(13, 45, 99));
        enterImmersiveMode();

        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(13, 45, 99));
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // Required by the online game and Socket.IO client.
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, false);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 80) enterImmersiveMode();
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("https".equalsIgnoreCase(uri.getScheme()) && ALLOWED_HOST.equalsIgnoreCase(uri.getHost())) {
                    return false;
                }
                // Keep the game host inside the app; open unrelated links in the user's browser.
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (Exception ignored) { }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                showingError = false;
                enterImmersiveMode();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                enterImmersiveMode();
                applyMobileViewport();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame() && !showingError) {
                    showingError = true;
                    showConnectionError();
                }
            }
        });

        setContentView(webView);
        if (savedInstanceState != null) {
            if (webView.restoreState(savedInstanceState) == null) webView.loadUrl(GAME_URL);
        } else {
            webView.loadUrl(GAME_URL);
        }
    }

    @SuppressWarnings("deprecation")
    private void enterImmersiveMode() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void applyMobileViewport() {
        if (webView == null) return;
        String js = "(function(){try{" +
                "var h=document.head||document.getElementsByTagName('head')[0];" +
                "if(h){var m=document.querySelector('meta[name=viewport]');" +
                "if(!m){m=document.createElement('meta');m.name='viewport';h.appendChild(m);}" +
                "m.setAttribute('content','width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no, viewport-fit=cover');}" +
                "document.documentElement.style.width='100%';" +
                "document.documentElement.style.minHeight='100%';" +
                "document.body.style.margin='0';" +
                "}catch(e){}})();";
        webView.evaluateJavascript(js, null);
    }

    private void showConnectionError() {
        String html = "<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1,user-scalable=no'><meta charset='utf-8'><style>" +
                "*{box-sizing:border-box}body{margin:0;min-height:100vh;display:flex;align-items:center;justify-content:center;background:#0d2d63;color:#fff;font-family:sans-serif;text-align:center}" +
                ".panel{padding:28px;max-width:520px}.muted{color:#ccdaef;font-size:14px}button{border:0;border-radius:12px;padding:13px 24px;background:#d8b66d;color:#17335e;font-size:16px;font-weight:700}" +
                "</style></head><body><div class='panel'><h2>暂时无法连接游戏</h2><p class='muted'>请检查手机网络，或等服务器唤醒后重试。</p><button onclick=\"location.href='" + GAME_URL + "'\">重新连接</button></div></body></html>";
        webView.loadDataWithBaseURL(GAME_URL, html, "text/html", "UTF-8", null);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) enterImmersiveMode();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("退出猪羊弟？")
                .setMessage("退出后，当前房间连接可能中断。")
                .setNegativeButton("继续游戏", (dialog, which) -> enterImmersiveMode())
                .setPositiveButton("退出", (dialog, which) -> finish())
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enterImmersiveMode();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
