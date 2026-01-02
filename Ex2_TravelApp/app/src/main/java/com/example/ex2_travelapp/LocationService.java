package com.example.ex2_travelapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LocationService extends Service {

    private static final String CHANNEL_ID = "location_service";
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    private int locationIndex = 0;

    // List of travel destinations with distances
    private final List<String> destinations = new ArrayList<String>() {{
        add("🏛️ Louvre Museum - 1.2km");
        add("🗼 Eiffel Tower - 2.5km");
        add("⛪ Notre Dame - 0.8km");
        add("🛍️ Champs-Élysées - 3.1km");
        add("🎨 Montmartre - 4.2km");
        add("🚢 Seine River - 0.5km");
        add("🏰 Versailles - 18km");
        add("🍷 Bordeaux Vineyards - 500km");
    }};

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            isRunning = true;
            startForegroundService();
            startLocationCycle();
        }
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void startForegroundService() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("📍 Travel Guide")
                .setContentText("Finding nearby attractions...")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
        startForeground(NOTIFICATION_ID, notification);

        handler.post(() -> Toast.makeText(this, "📍 Location service started", Toast.LENGTH_SHORT).show());
    }

    private void startLocationCycle() {
        new Thread(() -> {
            while (isRunning) {
                try {
                    Thread.sleep(8000); // Show new location every 8 seconds

                    // Cycle through all destinations
                    String destination = destinations.get(locationIndex % destinations.size());
                    locationIndex++;

                    // Update on UI thread
                    handler.post(() -> {
                        updateNotification("Nearby: " + destination);
                        Toast.makeText(LocationService.this, "📍 Found: " + destination,
                                Toast.LENGTH_SHORT).show();
                    });

                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    private void updateNotification(String text) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("📍 Travel Guide")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        handler.post(() -> Toast.makeText(this, "📍 Location service stopped", Toast.LENGTH_SHORT).show());
    }
}