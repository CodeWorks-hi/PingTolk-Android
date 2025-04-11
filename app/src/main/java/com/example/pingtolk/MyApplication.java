package com.example.pingtolk;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //  Firebase 초기화
        FirebaseApp.initializeApp(this);

        //익명 로그인 (필요 시 자동 로그인)
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // 익명 로그인 성공
                        } else {
                            // 실패 시 로그 출력
                            task.getException().printStackTrace();
                        }
                    });
        }

        //  SharedPreferences 기반 다크모드 설정
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
