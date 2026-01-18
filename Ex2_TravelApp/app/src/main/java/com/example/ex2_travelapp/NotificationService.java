package com.example.ex2_travelapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.Random;

public class NotificationService extends Service {

    private static final String CHANNEL_ID = "travel_notifications";
    private NotificationManager notificationManager;
    private Random random = new Random();

    private final String[] offers = {
            "✈️ 50% OFF Bali Packages! Book now!",
            "🏖️ Maldives Special: 7 days at $999!",
            "🗽 NYC Tour: Broadway shows included!",
            "🗼 Paris Getaway: Book now, pay later!",
            "⛰️ Swiss Alps Adventure - 30% OFF!",
            "🏰 Europe Tour: 5 countries in 10 days!",
            "🎭 Tokyo Experience - Limited spots!",
            "🕌 Dubai Luxury: Burj Khalifa views!"
    };

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendTravelNotification();
        stopSelf();
        return START_NOT_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Travel Offers",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendTravelNotification() {
        String offer = offers[random.nextInt(offers.length)];

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("✈️ Travel Offer!")
                .setContentText(offer)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}