package com.example.ex2_travelapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class MediaService extends Service {

    private static final String CHANNEL_ID = "media_service";
    private static final int NOTIFICATION_ID = 2;
    private MediaPlayer mediaPlayer;
    private NotificationManager notificationManager;
    private boolean isPlaying = false;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isPlaying) {
            startMediaPlayback();
        }
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Media Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void startMediaPlayback() {
        try {
            // Create notification sound
            int soundResource = getResources().getIdentifier(
                    "notification", "raw", getPackageName());

            if (soundResource != 0) {
                mediaPlayer = MediaPlayer.create(this, soundResource);
            } else {
                // Use system default notification sound
                mediaPlayer = MediaPlayer.create(this,
                        android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
            }

            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            isPlaying = true;

            startForegroundService();
            Toast.makeText(this, "🎵 Playing travel audio guide...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            // If sound fails, just show notification
            isPlaying = true;
            startForegroundService();
            Toast.makeText(this, "🎵 Audio guide started (simulated)", Toast.LENGTH_SHORT).show();
        }
    }

    private void startForegroundService() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("🎵 Travel Audio Guide")
                .setContentText("Playing: Paris Travel Highlights")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPlaying = false;
        Toast.makeText(this, "Audio stopped", Toast.LENGTH_SHORT).show();
    }
}