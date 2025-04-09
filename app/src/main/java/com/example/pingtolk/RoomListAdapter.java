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

        // 방 코드 표시
        holder.textRoomCode.setText("Ping Room: " + code);
        holder.textCreatedBy.setText("생성자: " + room.get("created_by"));

        // 입장 버튼 클릭 처리
        holder.btnEnter.setOnClickListener(v -> listener.onRoomClick(room));

        // 즐겨찾기 상태 초기화
        boolean isFavorite = Boolean.TRUE.equals(room.get("isFavorite"));
        holder.btnFavorite.setImageResource(isFavorite ? R.drawable.star: R.drawable.star_gray);
        holder.btnFavorite.setTag(isFavorite);

        // 즐겨찾기 클릭 처리
        holder.btnFavorite.setOnClickListener(v -> {
            boolean nowFavorite = holder.btnFavorite.getTag() != null && (boolean) holder.btnFavorite.getTag();

            DocumentReference favRef = FirebaseFirestore.getInstance()
                    .collection("users").document(nickname)
                    .collection("favorites").document(code);

            if (nowFavorite) {
                favRef.delete();
                holder.btnFavorite.setImageResource(R.drawable.star_gray);
                holder.btnFavorite.setTag(false);
            } else {
                favRef.set(new HashMap<>());
                holder.btnFavorite.setImageResource(R.drawable.star_yellow);
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
