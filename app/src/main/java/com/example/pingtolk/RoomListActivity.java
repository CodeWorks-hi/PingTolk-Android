package com.example.pingtolk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoomListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RoomListAdapter adapter;
    List<Map<String, Object>> roomList = new ArrayList<>();
    String nickname;

    ImageView btnBack, btnShare; // ✅ 뒤로가기 & 공유 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        nickname = getIntent().getStringExtra("nickname");

        // ✅ 뒤로가기 및 공유 버튼 연결
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnShareRoom = findViewById(R.id.btnShareRoom);

        // ✅ 뒤로가기 버튼 클릭
        btnBack.setOnClickListener(v -> finish());

        // ✅ 공유 버튼 클릭
        btnShareRoom.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "PingTalk 채팅방 초대");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    nickname + "님이 PingTalk에 참여 중입니다.\n\n" +
                            "앱 설치하기: https://play.google.com/store/apps/details?id=com.example.pingtolk");
            startActivity(Intent.createChooser(shareIntent, "공유할 앱 선택"));
        });

        // ✅ RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerRooms);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RoomListAdapter(roomList, room -> {
            String familyCode = (String) room.get("code");
            Intent intent = new Intent(RoomListActivity.this, ChatActivity.class);
            intent.putExtra("familyCode", familyCode);
            intent.putExtra("nickname", nickname);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // ✅ 방 목록 로드
        loadRooms();
    }

    private void loadRooms() {
        FirebaseFirestore.getInstance().collection("rooms")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    roomList.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) {
                            data.put("code", doc.getId()); // 문서 ID를 code로
                            roomList.add(data);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "방 목록을 불러올 수 없습니다", Toast.LENGTH_SHORT).show());
    }
}
