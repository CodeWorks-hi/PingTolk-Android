package com.example.pingtolk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final Context context;
    private final ArrayList<Message> messageList;
    private final String currentUser;

    public MessageAdapter(Context context, ArrayList<Message> messageList, String currentUser) {
        this.context = context;
        this.messageList = messageList;
        this.currentUser = currentUser;
    }

    // 뷰타입 결정 (내 메시지 = 0, 상대방 메시지 = 1, 날짜 구분선 = 2)
    @Override
    public int getItemViewType(int position) {
        Message msg = messageList.get(position);
        if (msg.isDateSeparator()) return 2;
        return msg.getSender().equals(currentUser) ? 0 : 1;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(context).inflate(R.layout.message_item_me, parent, false);
        } else if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.message_item_other, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.message_item_date, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messageList.get(position);

        if (msg.isDateSeparator()) {
            holder.textDate.setText(getDate(msg.getTimestamp()));
            return;
        }

        holder.textSender.setText(msg.getSender());
        holder.textMessage.setText(msg.getText());
        holder.textTime.setText(getTime(msg.getTimestamp()));

        // 상대방 메시지에만 프로필 이미지 표시
        if (getItemViewType(position) == 1 && holder.imageProfile != null) {
            String imageUrl = msg.getProfileImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_profile)
                        .into(holder.imageProfile);
            } else {
                holder.imageProfile.setImageResource(R.drawable.ic_profile);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private String getTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("a hh:mm", Locale.KOREA);
        return sdf.format(new Date(timestamp));
    }

    private String getDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);
        return sdf.format(new Date(timestamp));
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textSender, textMessage, textTime, textDate;
        ImageView imageProfile;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            // 안전한 뷰 연결
            if (itemView.findViewById(R.id.textSender) != null) {
                textSender = itemView.findViewById(R.id.textSender);
            }
            if (itemView.findViewById(R.id.textMessage) != null) {
                textMessage = itemView.findViewById(R.id.textMessage);
            }
            if (itemView.findViewById(R.id.textTime) != null) {
                textTime = itemView.findViewById(R.id.textTime);
            }
            if (itemView.findViewById(R.id.textDate) != null) {
                textDate = itemView.findViewById(R.id.textDate);
            }
            if (itemView.findViewById(R.id.imageProfile) != null) {
                imageProfile = itemView.findViewById(R.id.imageProfile);
            }
        }
    }
}
