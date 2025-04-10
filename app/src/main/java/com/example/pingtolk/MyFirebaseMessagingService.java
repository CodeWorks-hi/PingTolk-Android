package com.example.pingtolk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Firebase 클라우드 메시징을 처리하는 서비스 클래스
 * - 새로운 FCM 토큰 수신
 * - 수신된 메시지를 알림으로 표시
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // 알림 채널 ID
    private static final String CHANNEL_ID = "pingtalk_channel";

    /**
     * 새로 발급된 FCM 토큰을 수신하는 메서드
     * 앱을 처음 설치하거나, 기존 토큰이 만료된 경우 자동 호출된다
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM", "새로운 FCM 토큰: " + token);

        // 필요시 서버나 Firestore에 토큰 저장 가능
        // 예: db.collection("tokens").document(token).set(...)
    }

    /**
     * 실제로 메시지를 수신했을 때 호출되는 메서드
     * 백그라운드, 포그라운드 상태에서 메시지를 처리
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // 메시지 출처 로그 출력
        Log.d("FCM", "From: " + remoteMessage.getFrom());

        // 메시지 내용이 알림 형식일 경우 알림으로 표시
        if (remoteMessage.getNotification() != null) {
            Log.d("FCM", "Notification Message Body: " + remoteMessage.getNotification().getBody());

            // 알림 표시 함수 호출
            showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        }
    }

    /**
     * 실제로 알림을 생성하고 시스템에 표시하는 함수
     */
    private void showNotification(String title, String message) {
        // Android 8.0 이상에서는 알림 채널이 반드시 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "PingTalk 알림", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Android 13 이상은 알림 권한을 사용자에게 요청해야 하므로 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("FCM", "알림 권한이 없어 알림이 표시되지 않음");
                return;
            }
        }

        // 알림 구성 및 표시
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat_logo) // 앱 로고 또는 아이콘
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setNumber(1); // 숫자 뱃지 표시 (지원하는 런처에서만 표시됨)

        NotificationManagerCompat.from(this).notify(1, builder.build());
    }
}
