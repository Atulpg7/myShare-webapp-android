package com.example.myshare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        boolean isAlreadyLoggedIn = getSharedPreferences(Constant.myPrefs, MODE_PRIVATE).getBoolean(Constant.isAlreadyLoggedIn, false);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isAlreadyLoggedIn) {
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashScreen.this, LoginActivity.class));
            }
            finish();
        }, 500);
    }
}