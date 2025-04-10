package com.example.pingtolk;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 1001;
    private static final int REQUEST_IMAGE_PICK = 1002;

    private ImageView profileImage;
    private TextView textNickname;
    private Switch switchDarkMode, switchNotification;
    private SharedPreferences prefs;

    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        storage = FirebaseStorage.getInstance();

        // 뒤로가기 버튼
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 저장 버튼
        findViewById(R.id.btnSave).setOnClickListener(v ->
                Toast.makeText(this, "저장되었습니다", Toast.LENGTH_SHORT).show()
        );

        // 닉네임 수정
        textNickname = findViewById(R.id.textNickname);
        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            if (textNickname.getText().toString().equals("김민지")) {
                textNickname.setText("홍길동");
            } else {
                textNickname.setText("김민지");
            }
        });

        // 다크모드 설정
        switchDarkMode = findViewById(R.id.switchDarkMode);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDark);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int mode = isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
            AppCompatDelegate.setDefaultNightMode(mode);
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            recreate();
        });

        // 알림 설정 (토스트만)
        switchNotification = findViewById(R.id.switchNotification);
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "알림이 켜졌습니다" : "알림이 꺼졌습니다";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // 프로필 이미지
        profileImage = findViewById(R.id.profileImage);

        // 저장된 이미지 URL 불러오기
        String savedUrl = prefs.getString("profile_image_url", null);
        if (savedUrl != null && !savedUrl.isEmpty()) {
            Log.d("ProfileImage", "이미지 로드 시도: " + savedUrl);

            Glide.with(this)
                    .load(savedUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profileImage);
        }

        profileImage.setOnClickListener(v -> checkGalleryPermission());

        findViewById(R.id.layoutPrivacy).setOnClickListener(v ->
                Toast.makeText(this, "개인정보 보호 설정 화면입니다", Toast.LENGTH_SHORT).show());

        findViewById(R.id.layoutHelp).setOnClickListener(v ->
                Toast.makeText(this, "도움말 화면입니다", Toast.LENGTH_SHORT).show());
    }

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

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                uploadToFirebase(selectedImageUri);
            }
        }
    }

    private void uploadToFirebase(Uri imageUri) {
        StorageReference imageRef = storage.getReference()
                .child("profile_images/" + UUID.randomUUID());

        UploadTask uploadTask = imageRef.putFile(imageUri);

        uploadTask
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();

                            Log.d("ProfileImage", "업로드 성공, URL: " + downloadUrl);

                            Glide.with(this)
                                    .load(downloadUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .into(profileImage);

                            prefs.edit().putString("profile_image_url", downloadUrl).apply();
                        })
                )
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show();
                    Log.e("ProfileImage", "업로드 실패", e);
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "갤러리 접근 권한이 필요합니다", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
