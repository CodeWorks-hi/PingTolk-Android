package com.example.pingtolk;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Locale;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 1001;
    private static final int REQUEST_IMAGE_PICK = 1002;

    private ImageView profileImage;
    private TextView textNickname;
    private Switch switchDarkMode, switchNotification;
    private Spinner spinnerLanguage;

    private SharedPreferences prefs;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLocale(); // 언어 설정 적용
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        storage = FirebaseStorage.getInstance();

        // 뒤로가기 버튼
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 저장 버튼
        findViewById(R.id.btnSave).setOnClickListener(v -> {
            String nickname = textNickname.getText().toString();
            prefs.edit().putString("nickname", nickname).apply();
            Toast.makeText(this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);  // 결과 설정
        });

        // 닉네임
        textNickname = findViewById(R.id.textNickname);
        String savedNickname = prefs.getString("nickname", getString(R.string.nickname_sample)); // ✅ 저장된 닉네임 불러오기
        textNickname.setText(savedNickname);

        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            builder.setTitle("닉네임 수정");

            final EditText input = new EditText(ProfileActivity.this);
            input.setText(textNickname.getText().toString());
            input.setSelection(input.getText().length()); // 커서를 맨 뒤로
            builder.setView(input);

            builder.setPositiveButton("확인", (dialog, which) -> {
                String newNickname = input.getText().toString().trim();
                if (!newNickname.isEmpty()) {
                    textNickname.setText(newNickname);
                    prefs.edit().putString("nickname", newNickname).apply();
                    Toast.makeText(ProfileActivity.this, getString(R.string.toast_saved), Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

            builder.show();
        });

        // 다크모드
        switchDarkMode = findViewById(R.id.switchDarkMode);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDark);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int mode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            AppCompatDelegate.setDefaultNightMode(mode);
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            recreate(); // 현재 화면 재시작
        });

        // 알림 설정
        switchNotification = findViewById(R.id.switchNotification);
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? getString(R.string.toast_notification_on) : getString(R.string.toast_notification_off);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // 프로필 이미지 설정
        profileImage = findViewById(R.id.profileImage);
        String savedUrl = prefs.getString("profile_image_url", null);
        if (savedUrl != null && !savedUrl.isEmpty()) {
            Glide.with(this)
                    .load(savedUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profileImage);
        }

        profileImage.setOnClickListener(v -> checkGalleryPermission());

        // 언어 선택 스피너
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        String savedLang = prefs.getString("language", "ko");
        spinnerLanguage.setSelection(savedLang.equals("ko") ? 0 : 1);

        spinnerLanguage.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedLang = (position == 0) ? "ko" : "en";
                if (!selectedLang.equals(prefs.getString("language", "ko"))) {
                    prefs.edit().putString("language", selectedLang).apply();
                    recreate(); // 언어 변경 적용
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // 개인정보 및 도움말 클릭
        findViewById(R.id.layoutPrivacy).setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.label_privacy), Toast.LENGTH_SHORT).show());

        findViewById(R.id.layoutHelp).setOnClickListener(v ->
                Toast.makeText(this, getString(R.string.label_help), Toast.LENGTH_SHORT).show());
    }


    // 갤러리 접근 권한 확인
    private void checkGalleryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION_CODE);
            } else {
                openGallery();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
            } else {
                openGallery();
            }
        }
    }

    // 갤러리 열기
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // 이미지 선택 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // 1) 선택 즉시 이미지 미리보기 표시
                profileImage.setImageURI(selectedImageUri);

                // 2) 이후 Firebase Storage에 업로드
                uploadToFirebase(selectedImageUri);
            }
        }
    }

    // 이미지 업로드
    private void uploadToFirebase(Uri imageUri) {
        StorageReference imageRef = storage.getReference()
                .child("profile_images/" + UUID.randomUUID());

        UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            Glide.with(this)
                                    .load(downloadUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .into(profileImage);
                            prefs.edit().putString("profile_image_url", downloadUrl).apply();
                        })
                )
                .addOnFailureListener(e -> {
                    Toast.makeText(this, getString(R.string.toast_upload_fail), Toast.LENGTH_SHORT).show();
                    Log.e("ProfileImage", "업로드 실패", e);
                });
    }

    // 언어 설정 적용
    private void setLocale() {
        String lang = getSharedPreferences("settings", MODE_PRIVATE).getString("language", "ko");
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        android.content.res.Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, getString(R.string.toast_permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
