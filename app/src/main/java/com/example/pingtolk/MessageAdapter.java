package com.example.pingtolk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * 채팅 메시지를 리스트 형태로 화면에 표시하기 위한 RecyclerView 어댑터 클래스
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final Context context;
    private final ArrayList<Message> messageList;  // 메시지 리스트
    private final String currentUser;              // 현재 사용자(닉네임)

    public MessageAdapter(Context context, ArrayList<Message> messageList, String currentUser) {
        this.context = context;
        this.messageList = messageList;
        this.currentUser = currentUser;
    }

    /**
     * 각 아이템이 어떤 종류(보낸 사람, 받은 사람, 날짜 구분선)인지 구분하는 메서드
     */
    @Override
    public int getItemViewType(int position) {
        Message msg = messageList.get(position);
        if (msg.isDateSeparator()) return 2;                        // 날짜 구분선
        return msg.getSender().equals(currentUser) ? 0 : 1;         // 0: 내가 보낸 메시지, 1: 상대방 메시지
    }

    /**
     * ViewHolder를 생성하여 View를 inflate하는 메서드
     * 각 타입(viewType)에 따라 다른 레이아웃을 적용
     */
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(context).inflate(R.layout.message_item_me, parent, false);
        } else if (viewType == 1) {
            view = LayoutInflater.from(context).inflate(R.layout.message_item_other, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.message_item_date, parent, false); // 날짜용
        }
        return new MessageViewHolder(view);
    }

    /**
     * 메시지 데이터를 ViewHolder에 바인딩하는 메서드
     */
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messageList.get(position);

        if (msg.isDateSeparator()) {
            // 날짜 구분선 메시지
            holder.textDate.setText(getDate(msg.getTimestamp()));
            return;
        }

        // 일반 메시지의 경우 (보낸 사람, 메시지, 시간 출력)
        holder.textSender.setText(msg.getSender());
        holder.textMessage.setText(msg.getText());
        holder.textTime.setText(getTime(msg.getTimestamp()));
    }

    /**
     * 전체 메시지 수 리턴
     */
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    /**
     * 타임스탬프 → 시간 문자열로 포맷 (예: 오후 03:12)
     */
    private String getTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("a hh:mm", Locale.KOREA);
        return sdf.format(new Date(timestamp));
    }

    /**
     * 타임스탬프 → 날짜 문자열로 포맷 (예: 2025년 04월 09일)
     */
    private String getDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);
        return sdf.format(new Date(timestamp));
    }

    /**
     * 각 메시지 아이템에 대응하는 ViewHolder 정의
     */
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textSender, textMessage, textTime, textDate;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            // View가 있는 경우에만 연결 (안전한 분기 처리)
            if (itemView.findViewById(R.id.textDate) != null) {
                textDate = itemView.findViewById(R.id.textDate);
            }
            if (itemView.findViewById(R.id.textSender) != null) {
                textSender = itemView.findViewById(R.id.textSender);
            }
            if (itemView.findViewById(R.id.textMessage) != null) {
                textMessage = itemView.findViewById(R.id.textMessage);
            }
            if (itemView.findViewById(R.id.textTime) != null) {
                textTime = itemView.findViewById(R.id.textTime);
            }
        }
    }
}
