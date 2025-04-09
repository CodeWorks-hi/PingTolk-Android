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

    EditText editNickname, editFamilyCode, editPassword;
    Button btnCreate, btnJoin;
    FirebaseFirestore db;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult();
                        Log.d("FCM", "내 토큰: " + token); // 🔹 Logcat에서 확인 가능
                    }
                });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // ✅ 화면 연결

        // ✅ 위젯 연결
        editNickname = findViewById(R.id.editNickname);
        editFamilyCode = findViewById(R.id.editFamilyCode);
        editPassword = findViewById(R.id.editPassword);
        btnCreate = findViewById(R.id.btnCreate);
        btnJoin = findViewById(R.id.btnJoin);

        // ✅ SharedPreferences 설정 및 값 채워넣기
        prefs = getSharedPreferences("PingTalkPrefs", MODE_PRIVATE);
        editNickname.setText(prefs.getString("nickname", ""));
        editFamilyCode.setText(prefs.getString("familyCode", ""));
        editPassword.setText(prefs.getString("password", ""));

        db = FirebaseFirestore.getInstance(); // ✅ Firebase 초기화

        // ✅ 저장된 정보가 모두 있을 경우 자동 입장 → 리스트 화면으로 이동
        String savedNick = prefs.getString("nickname", "");
        String savedCode = prefs.getString("familyCode", "");
        String savedPw = prefs.getString("password", "");

        if (!savedNick.isEmpty() && !savedCode.isEmpty() && !savedPw.isEmpty()) {
            db.collection("rooms").document(savedCode).get().addOnSuccessListener(doc -> {
                if (doc.exists() && savedPw.equals(doc.getString("password"))) {
                    openRoomList(savedNick);
                }
            });
        }

        // ✅ 방 만들기
        btnCreate.setOnClickListener(v -> {
            String nickname = editNickname.getText().toString().trim();
            String familyCode = editFamilyCode.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (nickname.isEmpty() || familyCode.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("rooms").document(familyCode).get().addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Toast.makeText(this, "이미 존재하는 가족코드입니다", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> roomData = new HashMap<>();
                    roomData.put("password", password);
                    roomData.put("created_by", nickname);
                    roomData.put("created_at", new Date());

                    db.collection("rooms").document(familyCode).set(roomData)
                            .addOnSuccessListener(unused -> {
                                // ✅ 저장
                                prefs.edit()
                                        .putString("nickname", nickname)
                                        .putString("familyCode", familyCode)
                                        .putString("password", password)
                                        .apply();

                                Toast.makeText(this, "방이 생성되었습니다", Toast.LENGTH_SHORT).show();
                                openRoomList(nickname); // ✅ 리스트로 이동
                            });
                }
            });
        });

        // ✅ 방 입장
        btnJoin.setOnClickListener(v -> {
            String nickname = editNickname.getText().toString().trim();
            String familyCode = editFamilyCode.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (nickname.isEmpty() || familyCode.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "모든 정보를 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("rooms").document(familyCode).get().addOnSuccessListener(doc -> {
                if (!doc.exists()) {
                    Toast.makeText(this, "존재하지 않는 방입니다", Toast.LENGTH_SHORT).show();
                } else {
                    String savedPwCheck = doc.getString("password");
                    if (savedPwCheck != null && savedPwCheck.equals(password)) {
                        prefs.edit()
                                .putString("nickname", nickname)
                                .putString("familyCode", familyCode)
                                .putString("password", password)
                                .apply();
                        openRoomList(nickname); // ✅ 리스트로 이동
                    } else {
                        Toast.makeText(this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    // ✅ 채팅방 리스트 화면 이동
    private void openRoomList(String nickname) {
        Intent intent = new Intent(MainActivity.this, RoomListActivity.class);
        intent.putExtra("nickname", nickname);
        startActivity(intent);
    }
}
