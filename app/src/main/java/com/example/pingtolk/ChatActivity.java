package com.example.pingtolk;

import android.content.Intent;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * 가족 단위 채팅방 화면
 */
public class ChatActivity extends AppCompatActivity {

    // UI 컴포넌트
    TextView textRoomName, textRoomCode;
    EditText editMessage;
    ImageButton btnSend;
    ImageView btnShareRoom; //  공유 버튼
    RecyclerView recyclerMessages;

    // Firebase
    FirebaseFirestore db;
    CollectionReference chatRef;

    // 메시지 관련
    MessageAdapter adapter;
    ArrayList<Message> messageList = new ArrayList<>();
    String familyCode, nickname;
    private String lastDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 전달받은 데이터
        familyCode = getIntent().getStringExtra("familyCode");
        nickname = getIntent().getStringExtra("nickname");

        // UI 연결
        textRoomName = findViewById(R.id.textRoomName);
        textRoomCode = findViewById(R.id.textRoomCode);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        btnShareRoom = findViewById(R.id.btnShareRoom); //  공유 버튼
        recyclerMessages = findViewById(R.id.recyclerMessages);

        textRoomCode.setText(familyCode);
        textRoomName.setText("Ping Room");

        // Firestore 연결
        db = FirebaseFirestore.getInstance();
        chatRef = db.collection("rooms").document(familyCode).collection("messages");

        // 입장 메시지 저장
        Message welcomeMsg = new Message("SYSTEM", nickname + "님이 입장하셨습니다", System.currentTimeMillis());
        chatRef.add(welcomeMsg);

        // RecyclerView 설정
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerMessages.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(this, messageList, nickname);
        recyclerMessages.setAdapter(adapter);

        // 실시간 수신
        listenForMessages();

        // 엔터키로 전송
        editMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                            event.getAction() == KeyEvent.ACTION_DOWN)) {
                btnSend.performClick();
                return true;
            }
            return false;
        });

        // 전송 버튼
        btnSend.setOnClickListener(v -> {
            String text = editMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                Message msg = new Message(nickname, text, System.currentTimeMillis());
                chatRef.add(msg);
                editMessage.setText("");
            }
        });

        // 🔙 뒤로가기
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // 📤 공유 버튼
        btnShareRoom.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "PingTalk 채팅방 초대");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    nickname + "님이 PingTalk 채팅방에 참여 중입니다!\n\n" +
                            " 방 코드: " + familyCode + "\n\n" +
                            "앱 설치: https://play.google.com/store/apps/details?id=com.example.pingtolk");

            startActivity(Intent.createChooser(shareIntent, "공유할 앱 선택"));
        });
    }

    /**
     * Firestore 실시간 메시지 수신
     */
    private void listenForMessages() {
        chatRef.orderBy("timestamp")
                .addSnapshotListener((value, error) -> {
                    if (value == null || error != null) return;

                    for (DocumentChange change : value.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            Message msg = change.getDocument().toObject(Message.class);

                            // 날짜 구분
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
