package com.example.pingtolk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        nickname = getIntent().getStringExtra("nickname");

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
                .addOnFailureListener(e -> Toast.makeText(this, "방 목록을 불러올 수 없습니다", Toast.LENGTH_SHORT).show());
    }
}
