package com.alexey.virgoandroid;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.Toast;




public class WebAppInterface extends MainActivity{
    Context context;

    //-------------------------------------------------------------------------------------//
    /** Instantiate the interface and set the context */
    public WebAppInterface(Context _context) {
        context = _context;

    }
    //-------------------------------------------------------------------------------------//
    /** Далее идут методы, которые появятся в JavaScript */
    @JavascriptInterface
    public void sendSms(String phoneNumber, String message) {
        Log.e("WebAppInterface",phoneNumber);
    }
    //-------------------------------------------------------------------------------------//
    @JavascriptInterface
    public void log(String message) {
        Log.e("WebAppInterface",message);
    }

    //-------------------------------------------------------------------------------------//
    // This function can be called in our JS script now
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_LONG).show();

    }



    //-------------------------------------------------------------------------------------//
    // This function can be called in our JS script now
    @JavascriptInterface
    public void playBeep() {


        MainActivity.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGen1.startTone(ToneGenerator.TONE_DTMF_S,150);
            }
        });


    }

    //-------------------------------------------------------------------------------------//
    @JavascriptInterface
    public void openScanner() {
        MainActivity.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final float scale = MainActivity.activity.getResources().getDisplayMetrics().density;
                layoutParams.setMargins(0, (int)(220 * scale) ,0,0);
                constraintLayout.setLayoutParams(layoutParams);
            }
        });
    }
    //-------------------------------------------------------------------------------------//
    @JavascriptInterface
    public void closeScanner() {
        MainActivity.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final float scale = MainActivity.activity.getResources().getDisplayMetrics().density;
                layoutParams.setMargins(0, 0 ,0,0);
                constraintLayout.setLayoutParams(layoutParams);
            }
        });
    }
    //-------------------------------------------------------------------------------------//




}


