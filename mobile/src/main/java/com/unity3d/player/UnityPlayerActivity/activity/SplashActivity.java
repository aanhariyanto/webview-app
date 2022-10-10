package com.unity3d.player.UnityPlayerActivity.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;

import com.unity3d.player.UnityPlayerActivity.R;
import com.unity3d.player.UnityPlayerActivity.WebViewAppConfig;

public class SplashActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }, WebViewAppConfig.SPLASH_SCREEN_TIME_OUT);
    }
}

