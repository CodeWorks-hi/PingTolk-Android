package com.example.pingtolk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    // 생성자 (RoomViewHolder 내부에서 잘못 정의된 것 해결)
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

        holder.textRoomCode.setText(title);

        Object createdBy = room.get("created_by");
        holder.textCreatedBy.setText("생성자: " + (createdBy != null ? createdBy.toString() : ""));

        holder.btnEnter.setOnClickListener(v -> listener.onRoomClick(room));

        boolean isFavorite = Boolean.TRUE.equals(room.get("isFavorite"));
        holder.btnFavorite.setImageResource(isFavorite ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        holder.btnFavorite.setTag(isFavorite);

        boolean hasNewMessage = Boolean.TRUE.equals(room.get("hasNewMessage"));
        holder.alertView.setVisibility(hasNewMessage ? View.VISIBLE : View.GONE);

        holder.btnFavorite.setOnClickListener(v -> {
            boolean nowFavorite = holder.btnFavorite.getTag() != null && (boolean) holder.btnFavorite.getTag();

            DocumentReference favRef = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(nickname)
                    .collection("favorites")
                    .document(code);

            if (nowFavorite) {
                favRef.delete().addOnSuccessListener(aVoid -> {
                    holder.btnFavorite.setImageResource(R.drawable.ic_star_outline);
                    holder.btnFavorite.setTag(false);
                    Toast.makeText(v.getContext(), "즐겨찾기 해제됨", Toast.LENGTH_SHORT).show();
                });
            } else {
                favRef.set(new HashMap<>()).addOnSuccessListener(aVoid -> {
                    holder.btnFavorite.setImageResource(R.drawable.ic_star_filled);
                    holder.btnFavorite.setTag(true);
                    Toast.makeText(v.getContext(), "즐겨찾기에 추가됨", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView textRoomCode, textCreatedBy, alertView;
        Button btnEnter;
        ImageView btnFavorite;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            textRoomCode = itemView.findViewById(R.id.textRoomCode);
            textCreatedBy = itemView.findViewById(R.id.textCreatedBy);
            btnEnter = itemView.findViewById(R.id.btnEnterRoom);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            alertView = itemView.findViewById(R.id.textNewChatAlert);
        }
    }
}
