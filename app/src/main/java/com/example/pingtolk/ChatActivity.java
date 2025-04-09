package com.example.pingtolk;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * 가족 단위 채팅방 화면
 * - Firestore 실시간 채팅 구현
 * - 메시지 전송, 수신
 * - 날짜 구분선 표시
 */
public class ChatActivity extends AppCompatActivity {

    // UI 컴포넌트
    TextView textRoomName, textRoomCode;
    EditText editMessage;
    ImageButton btnSend;
    RecyclerView recyclerMessages;

    // Firebase
    FirebaseFirestore db;
    CollectionReference chatRef;

    // 메시지 관련
    MessageAdapter adapter;
    ArrayList<Message> messageList = new ArrayList<>();
    String familyCode, nickname;
    private String lastDate = ""; // 날짜 구분용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat); // 레이아웃 연결

        // MainActivity로부터 전달된 가족코드/닉네임 받아오기
        familyCode = getIntent().getStringExtra("familyCode");
        nickname = getIntent().getStringExtra("nickname");

        // UI 연결
        textRoomName = findViewById(R.id.textRoomName);
        textRoomCode = findViewById(R.id.textRoomCode);
        editMessage = findViewById(R.id.editMessage);
        btnSend = findViewById(R.id.btnSend);
        recyclerMessages = findViewById(R.id.recyclerMessages);

        textRoomCode.setText(familyCode);
        textRoomName.setText("Ping Room"); // 고정 또는 추후 Firestore에서 가져올 수 있음

        // Firestore 채팅 컬렉션 참조 설정
        db = FirebaseFirestore.getInstance();
        chatRef = db.collection("rooms").document(familyCode).collection("messages");

        // 입장 메시지 Firestore에 저장 (시스템 메시지)
        Message welcomeMsg = new Message("SYSTEM", nickname + "님이 입장하셨습니다", System.currentTimeMillis());
        chatRef.add(welcomeMsg);

        // RecyclerView 설정 (항상 아래쪽부터 보여줌)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerMessages.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(this, messageList, nickname);
        recyclerMessages.setAdapter(adapter);

        // 메시지 수신 대기 (실시간 업데이트)
        listenForMessages();

        // 키보드의 엔터키 입력 → 메시지 전송
        editMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                btnSend.performClick(); // 전송 버튼과 동일하게 처리
                return true;
            }
            return false;
        });

        // 전송 버튼 클릭 → Firestore에 메시지 저장
        btnSend.setOnClickListener(v -> {
            String text = editMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                Message msg = new Message(nickname, text, System.currentTimeMillis());
                chatRef.add(msg);
                editMessage.setText(""); // 전송 후 입력창 초기화
            }
        });

        // 뒤로가기 버튼 클릭 시 현재 액티비티 종료
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish(); // 현재 액티비티 종료 → MainActivity로 이동
        });
    }

    /**
     * Firestore 실시간 수신 처리
     * - 새로운 메시지가 들어오면 messageList에 추가
     * - 날짜가 바뀌면 날짜 구분 메시지 삽입
     */
    private void listenForMessages() {
        chatRef.orderBy("timestamp")
                .addSnapshotListener((value, error) -> {
                    if (value == null || error != null) return;

                    for (DocumentChange change : value.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            Message msg = change.getDocument().toObject(Message.class);

                            // 날짜가 다르면 날짜 구분선 추가
                            String msgDate = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                                    .format(new Date(msg.getTimestamp()));
                            if (!msgDate.equals(lastDate)) {
                                messageList.add(Message.createDateSeparator(msg.getTimestamp()));
                                lastDate = msgDate;
                            }

                            messageList.add(msg);
                            adapter.notifyItemInserted(messageList.size() - 1);
                            recyclerMessages.scrollToPosition(messageList.size() - 1); // 가장 아래로 이동
                        }
                    }
                });
    }
}
