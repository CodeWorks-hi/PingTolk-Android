package com.example.pingtolk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText editNickname, editPassword;
    Button btnJoin;
    FirebaseFirestore db;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d("FCM", "내 토큰: " + token);
                    }
                });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 위젯 연결
        editNickname = findViewById(R.id.editNickname);
        editPassword = findViewById(R.id.editPassword);
        btnJoin = findViewById(R.id.btnJoin);

        // SharedPreferences
        prefs = getSharedPreferences("PingTalkPrefs", MODE_PRIVATE);
        editNickname.setText(prefs.getString("nickname", ""));
        editPassword.setText(prefs.getString("password", ""));

        db = FirebaseFirestore.getInstance();

        String savedNick = prefs.getString("nickname", "");
        String savedPw = prefs.getString("password", "");

        if (!savedNick.isEmpty() && !savedPw.isEmpty()) {
            String autoRoomCode = savedNick + "_room";
            db.collection("rooms").document(autoRoomCode).get().addOnSuccessListener(doc -> {
                if (doc.exists() && savedPw.equals(doc.getString("password"))) {
                    openRoomList(savedNick);
                }
            });
        }

        btnJoin.setOnClickListener(v -> {
            String nickname = editNickname.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (nickname.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "닉네임과 비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            saveInfo(nickname, password); // SharedPreferences 저장
            openRoomList(nickname);       // 바로 입장
        });
    }

    private void saveInfo(String nickname, String password) {
        prefs.edit()
                .putString("nickname", nickname)
                .putString("password", password)
                .apply();
    }

    private void openRoomList(String nickname) {
        Intent intent = new Intent(MainActivity.this, RoomListActivity.class);
        intent.putExtra("nickname", nickname);
        startActivity(intent);
    }
}
