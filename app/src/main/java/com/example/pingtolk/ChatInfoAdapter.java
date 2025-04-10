package com.example.pingtolk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatInfoAdapter extends RecyclerView.Adapter<ChatInfoAdapter.ViewHolder> {

    private final List<String> memberList;

    public ChatInfoAdapter(List<String> memberList) {
        this.memberList = memberList;
    }

    @NonNull
    @Override
    public ChatInfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatInfoAdapter.ViewHolder holder, int position) {
        String name = memberList.get(position);
        holder.textName.setText(name);
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
        }
    }
}
