package com.example.pingtolk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    TextView textRoomName, textRoomCode;
    EditText editMessage;
    ImageButton btnSend;
    ImageView btnBack;
    RecyclerView recyclerMessages;

    FirebaseFirestore db;
    CollectionReference chatRef;
    MessageAdapter adapter;
    ArrayList<Message> messageList = new ArrayList<>();

    String familyCode, nickname;
    String profileUrl; // 사용자 프로필 이미지 URL

    private String lastDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        familyCode = getIntent().getStringExtra("familyCode");
        nickname = getIntent().getStringExtra("nickname");

        // SharedPreferences에서 프로필 이미지 URL 가져오기
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        profileUrl = prefs.getString("profile_image_url", null);

        // UI 연결
        textRoomName = findViewById(R.id.textRoomName);
        textRoomCode = findViewById(R.id.textRoomCode);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        recyclerMessages = findViewById(R.id.recyclerMessages);

        textRoomName.setText(R.string.room_name);
        textRoomCode.setText(familyCode);

        db = FirebaseFirestore.getInstance();
        chatRef = db.collection("rooms").document(familyCode).collection("messages");

        // 채팅 입장 메시지 자동 전송
        Message welcomeMsg = new Message("SYSTEM", nickname + "님이 입장하셨습니다", System.currentTimeMillis());
        welcomeMsg.setProfileImageUrl(null); // 시스템 메시지이므로 프로필 없음
        chatRef.add(welcomeMsg);

        // RecyclerView 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerMessages.setLayoutManager(layoutManager);

        adapter = new MessageAdapter(this, messageList, nickname);
        recyclerMessages.setAdapter(adapter);

        // 실시간 메시지 수신
        listenForMessages();

        // 엔터 입력 시 메시지 전송
        editMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                btnSend.performClick();
                return true;
            }
            return false;
        });

        // 전송 버튼 클릭 시 메시지 전송
        btnSend.setOnClickListener(v -> {
            String text = editMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                Message msg = new Message(nickname, text, System.currentTimeMillis());
                msg.setProfileImageUrl(profileUrl); // 프로필 이미지 URL 추가
                chatRef.add(msg);
                editMessage.setText("");
            }
        });

        // 뒤로가기 버튼 클릭
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, RoomListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
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
