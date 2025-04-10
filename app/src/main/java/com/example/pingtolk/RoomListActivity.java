package com.example.pingtolk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.*;

import java.util.*;

public class RoomListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RoomListAdapter adapter;
    List<Map<String, Object>> roomList = new ArrayList<>();
    List<Map<String, Object>> filteredList = new ArrayList<>();
    Set<String> favoriteCodes = new HashSet<>();
    String nickname;
    SharedPreferences enterPrefs;

    ImageView btnBack, btnSettings;
    Button btnCreate;
    CheckBox checkFavoriteOnly;
    CheckBox checkEnteredOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        SharedPreferences prefs = getSharedPreferences("PingTalkPrefs", MODE_PRIVATE);
        nickname = prefs.getString("nickname", null);
        if (nickname == null || nickname.isEmpty()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 바인딩
        btnBack = findViewById(R.id.btnBack); // Reusing the same button for logout
        btnSettings = findViewById(R.id.btnSettings);
        btnCreate = findViewById(R.id.btnCreate);
        checkFavoriteOnly = findViewById(R.id.checkFavoriteOnly);
        checkEnteredOnly = findViewById(R.id.checkEnteredOnly);

        // 로그아웃
        btnBack.setOnClickListener(v -> {
            new AlertDialog.Builder(RoomListActivity.this)
                .setTitle("메인 화면으로 이동")
                .setMessage("메인 화면으로 이동하시겠습니까?")
                .setPositiveButton("이동", (dialog, which) -> {
                    prefs.edit().clear().apply();
                    Intent intent = new Intent(RoomListActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("취소", null)
                .show();
        });

        // 설정 이동
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(RoomListActivity.this, ProfileActivity.class);
            intent.putExtra("nickname", nickname);
            startActivity(intent);
        });

        // 새 방 만들기
        btnCreate.setOnClickListener(v -> {
            BottomSheetDialog dialog = new BottomSheetDialog(RoomListActivity.this);
            View view = getLayoutInflater().inflate(R.layout.dialog_create_room, null);
            dialog.setContentView(view);

            EditText inputTitle = view.findViewById(R.id.editRoomTitle);
            EditText inputPw = view.findViewById(R.id.editRoomPassword);
            Button btnCancel = view.findViewById(R.id.btnCancel);
            Button btnCreateRoom = view.findViewById(R.id.btnCreate);

            btnCancel.setOnClickListener(view1 -> dialog.dismiss());

            btnCreateRoom.setOnClickListener(view12 -> {
                String title = inputTitle.getText().toString().trim();
                String password = inputPw.getText().toString().trim();

                if (title.isEmpty() || password.length() < 4) {
                    Toast.makeText(RoomListActivity.this, "방 제목과 4자리 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                String newCode = title.replaceAll("\\s+", "_");  // 방 이름 기반 문서 ID
                Map<String, Object> roomData = new HashMap<>();
                roomData.put("created_by", nickname);
                roomData.put("created_at", new Date());
                roomData.put("password", password);
                roomData.put("title", title);
                roomData.put("last_access", new Date());

                FirebaseFirestore.getInstance().collection("rooms")
                        .document(newCode)
                        .set(roomData)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(RoomListActivity.this, "방이 생성되었습니다", Toast.LENGTH_SHORT).show();
                            loadRooms();
                            dialog.dismiss();
                            Intent intent = new Intent(RoomListActivity.this, ChatActivity.class);
                            intent.putExtra("familyCode", newCode);
                            intent.putExtra("nickname", nickname);
                            intent.putExtra("roomName", title);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(RoomListActivity.this, "방 생성 실패", Toast.LENGTH_SHORT).show());
            });

            dialog.show();
        });


        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerRooms);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        enterPrefs = getSharedPreferences("EnteredRooms", MODE_PRIVATE);
        adapter = new RoomListAdapter(filteredList, room -> {
            String familyCode = (String) room.get("code");
            String title = (String) room.get("title");
            String correctPassword = (String) room.get("password");

            if (enterPrefs.contains(familyCode)) {
                Intent intent = new Intent(RoomListActivity.this, ChatActivity.class);
                intent.putExtra("familyCode", familyCode);
                intent.putExtra("nickname", nickname);
                intent.putExtra("roomName", title);
                startActivity(intent);
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(RoomListActivity.this);
            builder.setTitle("비밀번호 입력");

            final EditText input = new EditText(RoomListActivity.this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            builder.setView(input);

            builder.setPositiveButton("입장", (dialog, which) -> {
                String enteredPassword = input.getText().toString();
                if (enteredPassword.equals(correctPassword)) {
                    enterPrefs.edit().putBoolean(familyCode, true).apply();
                    Intent intent = new Intent(RoomListActivity.this, ChatActivity.class);
                    intent.putExtra("familyCode", familyCode);
                    intent.putExtra("nickname", nickname);
                    intent.putExtra("roomName", title);
                    startActivity(intent);
                } else {
                    Toast.makeText(RoomListActivity.this, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());
            builder.show();
        }, nickname);
        recyclerView.setAdapter(adapter);

        // 필터/정렬 이벤트
        checkFavoriteOnly.setOnCheckedChangeListener((btn, checked) -> applyFilterAndSort());
        checkEnteredOnly.setOnCheckedChangeListener((btn, checked) -> applyFilterAndSort());

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
                    Set<String> loadedFavorites = new HashSet<>();
                    for (DocumentSnapshot doc : favSnap.getDocuments()) {
                        loadedFavorites.add(doc.getId());
                    }
                    favoriteCodes = loadedFavorites;

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
        // Removed redundant update of favoriteCodes; already updated in loadRooms()

        boolean onlyFavorite = checkFavoriteOnly.isChecked();
        boolean onlyEntered = checkEnteredOnly.isChecked();

        // 필터링
        filteredList.clear();
        Set<String> enteredRooms = enterPrefs.getAll().keySet();

        for (Map<String, Object> room : roomList) {
            String code = (String) room.get("code");
            boolean isFav = Boolean.TRUE.equals(room.get("isFavorite"));

            if ((!onlyEntered || enteredRooms.contains(code)) &&
                (!onlyFavorite || isFav)) {
                filteredList.add(room);
            }
        }

        adapter.notifyDataSetChanged();
    }
}
