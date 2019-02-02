package com.alexey.virgoandroid;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;

import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    protected static Activity activity;
    protected static WebView webView;

    //"file:///android_asset/index.html"
    private final String LOCAL_FILE = "file:///android_asset/index.html";


    SurfaceView QRcameraPreview;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;


    private Camera camera;
    private Camera.Parameters parameters;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
























        activity = this;



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











        //-------------------------------------//
        QRcameraPreview = (SurfaceView) findViewById(R.id.QR_SurfaceView);
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedFps(30.0f)
                //.setRequestedFps(16.0f)
                //.setRequestedPreviewSize(540, 960)
                .setRequestedPreviewSize(540, 960)
                .build();


        QRcameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CAMERA},1001);
                    return;
                }
                try {
                    cameraSource.start(QRcameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> codes = detections.getDetectedItems();
                if(codes.size() != 0 ){
                    Log.e("CODES",codes.valueAt(0).displayValue);

                    webView.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl("javascript:$('#QRCODE').html('"+codes.valueAt(0).displayValue+"')");
                        }
                    });
                }

            }
        });
        //----------------------//



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!=null){
            if( result.getContents() == null){
                //Toast.makeText(this, "RESULT: " + "cancel" , Toast.LENGTH_LONG).show();
            }else {
                String contents = result.getContents();
                webView.loadUrl("javascript:$('.test').html('"+contents+"')");
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
