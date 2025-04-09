package com.example.pingtolk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.RoomViewHolder> {

    public interface OnRoomClickListener {
        void onRoomClick(Map<String, Object> room);
    }

    private final List<Map<String, Object>> rooms;
    private final OnRoomClickListener listener;
    private final String nickname;

    public RoomListAdapter(List<Map<String, Object>> rooms, OnRoomClickListener listener, String nickname) {
        this.rooms = rooms;
        this.listener = listener;
        this.nickname = nickname;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Map<String, Object> room = rooms.get(position);
        String code = (String) room.get("code");
        String title = room.get("title") != null ? (String) room.get("title") : code;

        // 🔹 방 제목 또는 코드 표시
        holder.textRoomCode.setText("Ping Room: " + title);

        // 🔹 생성자 표시
        Object createdBy = room.get("created_by");
        holder.textCreatedBy.setText("생성자: " + (createdBy != null ? createdBy.toString() : ""));

        // 🔹 입장 버튼 클릭 이벤트
        holder.btnEnter.setOnClickListener(v -> listener.onRoomClick(room));

        // 🔹 즐겨찾기 상태 설정
        boolean isFavorite = Boolean.TRUE.equals(room.get("isFavorite"));
        holder.btnFavorite.setImageResource(isFavorite ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        holder.btnFavorite.setTag(isFavorite);

        // 🔹 즐겨찾기 토글 처리
        holder.btnFavorite.setOnClickListener(v -> {
            boolean nowFavorite = holder.btnFavorite.getTag() != null && (boolean) holder.btnFavorite.getTag();

            DocumentReference favRef = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(nickname)
                    .collection("favorites")
                    .document(code);

            if (nowFavorite) {
                favRef.delete();
                holder.btnFavorite.setImageResource(R.drawable.ic_star_outline);
                holder.btnFavorite.setTag(false);
            } else {
                favRef.set(new HashMap<>());
                holder.btnFavorite.setImageResource(R.drawable.ic_star_filled);
                holder.btnFavorite.setTag(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView textRoomCode, textCreatedBy;
        Button btnEnter;
        ImageView btnFavorite;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            textRoomCode = itemView.findViewById(R.id.textRoomCode);
            textCreatedBy = itemView.findViewById(R.id.textCreatedBy);
            btnEnter = itemView.findViewById(R.id.btnEnterRoom);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
