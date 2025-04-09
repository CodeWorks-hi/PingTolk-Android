package com.example.pingtolk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.RoomViewHolder> {

    public interface OnRoomClickListener {
        void onRoomClick(Map<String, Object> room);
    }

    private final List<Map<String, Object>> rooms;
    private final OnRoomClickListener listener;

    public RoomListAdapter(List<Map<String, Object>> rooms, OnRoomClickListener listener) {
        this.rooms = rooms;
        this.listener = listener;
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
        holder.textRoomCode.setText("가족 코드: " + room.get("code"));
        holder.textCreatedBy.setText("생성자: " + room.get("created_by"));
        holder.btnEnter.setOnClickListener(v -> listener.onRoomClick(room));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView textRoomCode, textCreatedBy;
        Button btnEnter;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            textRoomCode = itemView.findViewById(R.id.textRoomCode);
            textCreatedBy = itemView.findViewById(R.id.textCreatedBy);
            btnEnter = itemView.findViewById(R.id.btnEnterRoom);
        }
    }
}
