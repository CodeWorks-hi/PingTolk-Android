package com.example.pingtolk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    EditText editProfileNickname;
    Button btnSaveProfile;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editProfileNickname = findViewById(R.id.editProfileNickname);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        prefs = getSharedPreferences("PingTalkPrefs", MODE_PRIVATE);

        // 저장된 닉네임 불러오기
        String savedName = prefs.getString("nickname", "");
        editProfileNickname.setText(savedName);

        // 저장 버튼 클릭
        btnSaveProfile.setOnClickListener(v -> {
            String newName = editProfileNickname.getText().toString().trim();
            if (!newName.isEmpty()) {
                prefs.edit().putString("nickname", newName).apply();
                Toast.makeText(this, "닉네임이 저장되었습니다", Toast.LENGTH_SHORT).show();
                finish(); // 설정 저장 후 닫기
            } else {
                Toast.makeText(this, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
