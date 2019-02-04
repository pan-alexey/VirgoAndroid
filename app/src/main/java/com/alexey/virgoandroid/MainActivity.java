package com.alexey.virgoandroid;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;


public class MainActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {

    protected static Activity activity;
    protected static WebView webView;

    //"file:///android_asset/index.html"
    private final String LOCAL_FILE = "file:///android_asset/index.html";
    ///------------------------------------//
    private ZBarScannerView mScannerView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

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
        webView.loadUrl(LOCAL_FILE);// загрузили нашу страничку




    }



    @Override
    protected void onResume() {
        super.onResume();
        //-------------------------//
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA},1001);
            return;
        } else {
            ViewGroup contentFrame = (ViewGroup) findViewById(R.id.ZBAR_Frame);
            mScannerView = new ZBarScannerView(this);
            mScannerView.setPadding(0,0,0,0);
            contentFrame.addView(mScannerView);
            mScannerView.setResultHandler(this);
            mScannerView.setCameraDistance(13.0f);
            mScannerView.startCamera();
        }
        //-------------------------//
    }


    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();

    }


    //ZBAR ACTIVITY
    @Override
    public void handleResult(Result result) {
        Toast.makeText(this, "Contents = " + result.getContents() + ", Format = " + result.getBarcodeFormat().getName(), Toast.LENGTH_SHORT).show();
        //result.getBarcodeFormat().getName()
        //result.getContents()

        // add second deleay for whatching
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.resumeCameraPreview(MainActivity.this);
            }
        }, 2000);
    }
}
