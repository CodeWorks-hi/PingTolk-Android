package com.example.pingtolk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.*;

import java.util.*;

public class RoomListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RoomListAdapter adapter;
    List<Map<String, Object>> roomList = new ArrayList<>();
    List<Map<String, Object>> filteredList = new ArrayList<>();
    Set<String> favoriteCodes = new HashSet<>();
    String nickname;

    ImageView btnBack, btnSettings;
    Button btnCreate;
    CheckBox checkFavoriteOnly;
    Spinner spinnerSort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        nickname = getIntent().getStringExtra("nickname");

        // 바인딩
        btnBack = findViewById(R.id.btnBack);
        btnSettings = findViewById(R.id.btnSettings);
        btnCreate = findViewById(R.id.btnCreate);
        checkFavoriteOnly = findViewById(R.id.checkFavoriteOnly);
        spinnerSort = findViewById(R.id.spinnerSort);

        // 뒤로가기
        btnBack.setOnClickListener(v -> finish());

        // 설정 이동
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(RoomListActivity.this, ProfileActivity.class);
            intent.putExtra("nickname", nickname);
            startActivity(intent);
        });

        // 새 방 만들기
        btnCreate.setOnClickListener(v -> createNewRoom());

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerRooms);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoomListAdapter(filteredList, room -> {
            String familyCode = (String) room.get("code");
            Intent intent = new Intent(RoomListActivity.this, ChatActivity.class);
            intent.putExtra("familyCode", familyCode);
            intent.putExtra("nickname", nickname);
            startActivity(intent);
        }, nickname);
        recyclerView.setAdapter(adapter);

        // 필터/정렬 이벤트
        checkFavoriteOnly.setOnCheckedChangeListener((btn, checked) -> applyFilterAndSort());
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                applyFilterAndSort();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadRooms();
    }

    /**
     * 방 목록 + 즐겨찾기 정보 불러오기
     */
    private void loadRooms() {
        FirebaseFirestore.getInstance().collection("users")
                .document(nickname)
                .collection("favorites")
                .get()
                .addOnSuccessListener(favSnap -> {
                    favoriteCodes.clear();
                    for (DocumentSnapshot doc : favSnap.getDocuments()) {
                        favoriteCodes.add(doc.getId());
                    }

                    FirebaseFirestore.getInstance().collection("rooms")
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                roomList.clear();
                                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                    Map<String, Object> data = doc.getData();
                                    if (data != null) {
                                        String code = doc.getId();
                                        data.put("code", code);
                                        data.put("isFavorite", favoriteCodes.contains(code));
                                        roomList.add(data);
                                    }
                                }
                                applyFilterAndSort();
                            });
                });
    }

    /**
     * 필터링 및 정렬 적용
     */
    private void applyFilterAndSort() {
        boolean onlyFavorite = checkFavoriteOnly.isChecked();
        String sortOption = spinnerSort.getSelectedItem().toString();

        // 필터링
        filteredList.clear();
        for (Map<String, Object> room : roomList) {
            boolean isFav = Boolean.TRUE.equals(room.get("isFavorite"));
            if (!onlyFavorite || isFav) {
                filteredList.add(room);
            }
        }

        // 정렬
        filteredList.sort((a, b) -> {
            if ("방 코드 순".equals(sortOption)) {
                return String.valueOf(a.get("code")).compareToIgnoreCase(String.valueOf(b.get("code")));
            } else if ("최근 접속 순".equals(sortOption)) {
                Date aTime = a.get("last_access") instanceof Date ? (Date) a.get("last_access") : new Date(0);
                Date bTime = b.get("last_access") instanceof Date ? (Date) b.get("last_access") : new Date(0);
                return bTime.compareTo(aTime); // 최신 먼저
            }
            return 0;
        });

        adapter.notifyDataSetChanged();
    }

    /**
     * 새 방 만들기
     */
    private void createNewRoom() {
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
                    roomData.put("title", roomTitle);
                    roomData.put("last_access", new Date());

                    FirebaseFirestore.getInstance().collection("rooms")
                            .document(newCode)
                            .set(roomData)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "새 방 생성됨: " + roomTitle, Toast.LENGTH_SHORT).show();
                                loadRooms();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "방 생성 실패", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
