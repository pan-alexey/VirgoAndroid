package com.alexey.virgoandroid;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

import static me.dm7.barcodescanner.zbar.BarcodeFormat.ALL_FORMATS;
import static me.dm7.barcodescanner.zbar.BarcodeFormat.QRCODE;



//ConstraintLayout

public class MainActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {

    protected static Activity activity;
    protected static WebView webView;

    protected static ConstraintLayout constraintLayout;
    protected static ConstraintLayout.LayoutParams layoutParams;

    private final String LOCAL_FILE = "file:///android_asset/index.html";
    //private final String LOCAL_FILE = "http://192.168.2.101:3000";

    protected ZBarScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        activity = this;
        setContentView(R.layout.activity_main);

        constraintLayout = (ConstraintLayout) findViewById (R.id.ConstraintLayoutWebView);
        layoutParams = (ConstraintLayout.LayoutParams) constraintLayout.getLayoutParams();







        //--------------------------------------------------------------//
        webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowFileAccessFromFileURLs(true);
            webView.getSettings().setAllowFileAccessFromFileURLs(true);
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }

        webView.setVerticalScrollBarEnabled(false);        // отключили прокрутку
        webView.setHorizontalScrollBarEnabled(false);      // отключили прокрутку
        webView.getSettings().setJavaScriptEnabled(true);  // включили JavaScript
        webView.getSettings().setDomStorageEnabled(true);  // включили localStorage и т.п.
        webView.getSettings().setSupportZoom(false);       // отключили зум, т.к. нормальные приложения подобным функционалом не обладают
        webView.getSettings().setSupportMultipleWindows(false);   // отключили поддержку вкладок.
        // Т.к. пользователь должен сидеть в SPA приложении
        webView.addJavascriptInterface(new WebAppInterface(this), "_API");   // прокидываем объект в JavaScript.
        webView.setWebChromeClient(new WebChromeClient() {
            // Need to accept permissions to use the camera and audio
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {
                        request.grant(request.getResources());
                    }
                });
            }
        });
        webView.setWebViewClient(new CustomWebViewClient());



        webView.loadUrl(LOCAL_FILE);// загрузили нашу страничку
        //--------------------------------------------------------------//

        //--------------------------------------------------------------//
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},1001);
        } else {
            List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
            formats.add( BarcodeFormat.QRCODE );
            ViewGroup contentFrame = (ViewGroup) findViewById(R.id.ZBAR_Frame);
            mScannerView = new ZBarScannerView(this);
            mScannerView.setPadding(0,0,0,0);
            contentFrame.addView(mScannerView);
            mScannerView.setResultHandler(this);
            mScannerView.setFormats( formats );
            mScannerView.startCamera();
        }
        //--------------------------------------------------------------//
    }
    //============================================================================================//
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1001: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //--------------------------------------------------------------//
                    List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
                    formats.add( BarcodeFormat.QRCODE );
                    ViewGroup contentFrame = (ViewGroup) findViewById(R.id.ZBAR_Frame);
                    mScannerView = new ZBarScannerView(this);
                    mScannerView.setPadding(0,0,0,0);
                    contentFrame.addView(mScannerView);
                    mScannerView.setResultHandler(this);
                    mScannerView.setFormats( formats );
                    mScannerView.startCamera();
                    //--------------------------------------------------------------//
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }
    //============================================================================================//
    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            //--------------------------------------------------------------//
            List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
            formats.add( BarcodeFormat.QRCODE );
            ViewGroup contentFrame = (ViewGroup) findViewById(R.id.ZBAR_Frame);
            mScannerView = new ZBarScannerView(this);
            mScannerView.setPadding(0,0,0,0);
            contentFrame.addView(mScannerView);
            mScannerView.setResultHandler(this);
            mScannerView.setFormats( formats );
            mScannerView.startCamera();
            //--------------------------------------------------------------//
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},1001);
            }
        }
        webView.loadUrl(LOCAL_FILE);// загрузили нашу страничку для разработчика
    }
    //============================================================================================//
    @Override
    protected void onPause() {
        super.onPause();
    }
    //============================================================================================//
    //ZBAR ACTIVITY
    @Override
    public void handleResult(Result result) {
        //Toast.makeText(this, "Contents = " + result.getContents() + ", Format = " + result.getBarcodeFormat().getName(), Toast.LENGTH_SHORT).show();
        //result.getBarcodeFormat().getName()
        //result.getContents()
        //result.getContents()
        // add second deleay for whatching
        //MainActivity.webView.loadUrl("javascript:alert('"+result.getContents()+"');");
        //MainActivity.webView.loadUrl("javascript:detectResult('"+result.getContents()+"');)");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript("detectResult('"+result.getContents()+"');", null);
        } else {
            webView.loadUrl("javascript:detectResult('"+result.getContents()+"');");
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(MainActivity.this);
            }
        }, 1200);
    }







    //============================================================================================//
    private class CustomWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView wv, String url) {
            if(url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;
            }
            return false;
        }
    }
    //============================================================================================//






}