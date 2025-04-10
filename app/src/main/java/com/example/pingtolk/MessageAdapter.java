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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final Context context;
    private final ArrayList<Message> messageList;
    private final String currentUser;

    public MessageAdapter(Context context, ArrayList<Message> messageList, String currentUser) {
        this.context = context;
        this.messageList = messageList;
        this.currentUser = currentUser;
    }

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
        holder.textTime.setText(getTime(msg.getTimestamp()));

        String imageUrl = msg.getImageUrl();
        String text = msg.getText();

        boolean isImage = imageUrl != null && !imageUrl.isEmpty();

        if (isImage) {
            if (holder.imageView != null) {
                holder.imageView.setVisibility(View.VISIBLE);
                holder.textMessage.setVisibility(View.GONE);

                Glide.with(context)
                        .load(msg.getImageUrl())
                        .transform(new CenterCrop(), new RoundedCorners(30))
                        .into(holder.imageView);
            }
        } else {
            holder.textMessage.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
            holder.textMessage.setText(text);
        }

        // 프로필 이미지 표시 (상대방)
        if (getItemViewType(position) == 1 && holder.imageProfile != null) {
            String imageUrlProfile = msg.getProfileImageUrl();
            if (imageUrlProfile != null && !imageUrlProfile.isEmpty()) {
                Glide.with(context)
                        .load(imageUrlProfile)
                        .circleCrop()
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
        ImageView imageProfile, imageView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            if (itemView.findViewById(R.id.textSender) != null)
                textSender = itemView.findViewById(R.id.textSender);
            if (itemView.findViewById(R.id.textMessage) != null)
                textMessage = itemView.findViewById(R.id.textMessage);
            if (itemView.findViewById(R.id.textTime) != null)
                textTime = itemView.findViewById(R.id.textTime);
            if (itemView.findViewById(R.id.textDate) != null)
                textDate = itemView.findViewById(R.id.textDate);
            if (itemView.findViewById(R.id.imageProfile) != null)
                imageProfile = itemView.findViewById(R.id.imageProfile);
            if (itemView.findViewById(R.id.imageMessage) != null)
                imageView = itemView.findViewById(R.id.imageMessage);
        }
    }
}
