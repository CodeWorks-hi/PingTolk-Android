package com.example.pingtolk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RoomListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RoomListAdapter adapter;
    List<Map<String, Object>> roomList = new ArrayList<>();
    String nickname;

    ImageView btnBack, btnShareRoom, btnSettings;
    Button btnCreate; //  방 만들기 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        nickname = getIntent().getStringExtra("nickname");

        // UI 바인딩
        btnBack = findViewById(R.id.btnBack);
        btnSettings = findViewById(R.id.btnSettings);
        btnCreate = findViewById(R.id.btnCreate); //  하단 버튼 연결

        // 뒤로가기
        btnBack.setOnClickListener(v -> finish());

        // 설정 화면 이동
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(RoomListActivity.this, ProfileActivity.class);
            intent.putExtra("nickname", nickname);
            startActivity(intent);
        });

        // 방 만들기 버튼 클릭
        btnCreate.setOnClickListener(v -> createNewRoom());

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerRooms);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoomListAdapter(roomList, room -> {
            String familyCode = (String) room.get("code");
            Intent intent = new Intent(RoomListActivity.this, ChatActivity.class);
            intent.putExtra("familyCode", familyCode);
            intent.putExtra("nickname", nickname);
            startActivity(intent);
        }, nickname);
        recyclerView.setAdapter(adapter);

        // 불러오기
        loadRooms();
    }

    /**
     *  방 목록 로드
     */
    private void loadRooms() {
        FirebaseFirestore.getInstance().collection("rooms")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    roomList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) {
                            data.put("code", doc.getId());
                            roomList.add(data);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "방 목록을 불러올 수 없습니다", Toast.LENGTH_SHORT).show());
    }

    /**
     * ➕ 새 방 만들기
     */
    private void createNewRoom() {
        // 다이얼로그로 방 제목 입력 받기
        EditText input = new EditText(this);
        input.setHint("방 이름을 입력하세요");

        new AlertDialog.Builder(this)
                .setTitle("새 방 만들기")
                .setView(input)
                .setPositiveButton("생성", (dialog, which) -> {
                    String roomTitle = input.getText().toString().trim();
                    if (roomTitle.isEmpty()) {
                        Toast.makeText(this, "방 이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String newCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
                    Map<String, Object> roomData = new HashMap<>();
                    roomData.put("created_by", nickname);
                    roomData.put("created_at", new Date());
                    roomData.put("password", "1234");
                    roomData.put("title", roomTitle); // 입력된 방 이름 저장

                    FirebaseFirestore.getInstance().collection("rooms")
                            .document(newCode)
                            .set(roomData)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "새 방 생성됨: " + roomTitle, Toast.LENGTH_SHORT).show();
                                loadRooms(); // 목록 새로고침
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "방 생성 실패", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("취소", null)
                .show();
    }

}
