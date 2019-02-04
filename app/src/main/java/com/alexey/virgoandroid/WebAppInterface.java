package com.alexey.virgoandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;


public class WebAppInterface extends MainActivity{
    Context context;



    /** Instantiate the interface and set the context */
    public WebAppInterface(Context _context) {
        context = _context;

    }
    /** Далее идут методы, которые появятся в JavaScript */
    @JavascriptInterface
    public void sendSms(String phoneNumber, String message) {
        Log.e("WebAppInterface",phoneNumber);
    }

    @JavascriptInterface
    public void log(String message) {
        Log.e("WebAppInterface",message);
    }


    // This function can be called in our JS script now
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
    }


    @JavascriptInterface
    public void openScanner() {
//        IntentIntegrator integrator = new IntentIntegrator( MainActivity.activity );
//        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
//        integrator.setOrientationLocked(true);
//        integrator.setPrompt("");
//        integrator.setCameraId(0);
//        integrator.setBeepEnabled(false);
//        integrator.setBarcodeImageEnabled(false);
//        integrator.initiateScan();
    }

    @JavascriptInterface
    public void openScannerCamera() {
//        IntentIntegrator integrator = new IntentIntegrator( MainActivity.activity );
//        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
//        integrator.setOrientationLocked(true);
//        integrator.setPrompt("");
//        integrator.setCameraId(1);
//        integrator.setBeepEnabled(false);
//        integrator.setBarcodeImageEnabled(false);
//        integrator.initiateScan();
    }

}


