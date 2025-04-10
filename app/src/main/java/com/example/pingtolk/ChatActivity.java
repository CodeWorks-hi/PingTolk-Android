package com.example.pingtolk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_PICK = 2001;

    TextView textRoomName, textRoomCode;
    EditText editMessage;
    ImageButton btnSend, btnImage;
    ImageView btnBack;
    RecyclerView recyclerMessages;

    FirebaseFirestore db;
    CollectionReference chatRef;
    MessageAdapter adapter;
    ArrayList<Message> messageList = new ArrayList<>();

    String familyCode, nickname;
    String profileUrl;

    private String lastDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        familyCode = getIntent().getStringExtra("familyCode");
        nickname = getIntent().getStringExtra("nickname");

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        profileUrl = prefs.getString("profile_image_url", null);

        // UI 연결
        textRoomName = findViewById(R.id.textRoomName);
        textRoomCode = findViewById(R.id.textRoomCode);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        btnImage = findViewById(R.id.btnImage);
        btnBack = findViewById(R.id.btnBack);
        recyclerMessages = findViewById(R.id.recyclerMessages);

        textRoomName.setText(R.string.room_name);
        textRoomCode.setText(familyCode);

        db = FirebaseFirestore.getInstance();
        chatRef = db.collection("rooms").document(familyCode).collection("messages");

        // 입장 메시지
        Message welcomeMsg = new Message("SYSTEM", nickname + "님이 입장하셨습니다", System.currentTimeMillis());
        welcomeMsg.setProfileImageUrl(null);
        welcomeMsg.setType("text");
        chatRef.add(welcomeMsg);

        // 메시지 리스트
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerMessages.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(this, messageList, nickname);
        recyclerMessages.setAdapter(adapter);

        listenForMessages();

        // 텍스트 메시지 전송
        btnSend.setOnClickListener(v -> {
            String text = editMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                Message msg = new Message(nickname, text, System.currentTimeMillis());
                msg.setProfileImageUrl(profileUrl);
                msg.setType("text");
                chatRef.add(msg);
                editMessage.setText("");
            }
        });

        // 엔터키 전송
        editMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                btnSend.performClick();
                return true;
            }
            return false;
        });

        // 이미지 전송
        btnImage.setOnClickListener(v -> openGallery());

        // 뒤로가기
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, RoomListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            new Handler(getMainLooper()).postDelayed(this::finish, 100);
        });
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
            Uri imageUri = data.getData();
            if (imageUri != null) {
                uploadImageToFirebase(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        StorageReference ref = FirebaseStorage.getInstance()
                .getReference("chat_images/" + UUID.randomUUID());

        ref.putFile(imageUri)
                .addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    // 이미지 메시지 전송
                    Message msg = new Message();
                    msg.setSender(nickname);
                    msg.setTimestamp(System.currentTimeMillis());
                    msg.setProfileImageUrl(profileUrl);
                    msg.setType("image");
                    msg.setImageUrl(uri.toString());
                    msg.setText(""); // 텍스트는 비워둠
                    chatRef.add(msg);
                }))
                .addOnFailureListener(e ->
                        Log.e("ChatActivity", "이미지 업로드 실패", e));
    }

    private void listenForMessages() {
        chatRef.orderBy("timestamp")
                .addSnapshotListener((QuerySnapshot value, com.google.firebase.firestore.FirebaseFirestoreException error) -> {
                    if (value == null || error != null) return;

                    for (DocumentChange change : value.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            Message msg = change.getDocument().toObject(Message.class);

                            // 날짜 구분선 추가
                            String msgDate = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                                    .format(new Date(msg.getTimestamp()));
                            if (!msgDate.equals(lastDate)) {
                                messageList.add(Message.createDateSeparator(msg.getTimestamp()));
                                lastDate = msgDate;
                            }

                            messageList.add(msg);
                            adapter.notifyItemInserted(messageList.size() - 1);
                            recyclerMessages.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }
}
