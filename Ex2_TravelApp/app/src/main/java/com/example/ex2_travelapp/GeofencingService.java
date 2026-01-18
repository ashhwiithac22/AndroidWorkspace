package com.example.ex2_travelapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeofencingService extends Service {

    private static final String CHANNEL_ID = "geofencing_service";
    private static final int NOTIFICATION_ID = 3;
    private NotificationManager notificationManager;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    private int alertCount = 0;

    // Geofence locations with coordinates (for visualization)
    private final List<GeofenceLocation> geofences = new ArrayList<GeofenceLocation>() {{
        add(new GeofenceLocation("Eiffel Tower", "Paris, France", 48.8584, 2.2945, 500));
        add(new GeofenceLocation("Taj Mahal", "Agra, India", 27.1751, 78.0421, 300));
        add(new GeofenceLocation("Statue of Liberty", "New York, USA", 40.6892, -74.0445, 400));
        add(new GeofenceLocation("Great Wall", "China", 40.4319, 116.5704, 1000));
        add(new GeofenceLocation("Colosseum", "Rome, Italy", 41.8902, 12.4922, 250));
        add(new GeofenceLocation("Sydney Opera", "Sydney, Australia", -33.8568, 151.2153, 350));
    }};

    class GeofenceLocation {
        String name;
        String location;
        double lat;
        double lon;
        int radius; // Geofence radius in meters

        GeofenceLocation(String name, String location, double lat, double lon, int radius) {
            this.name = name;
            this.location = location;
            this.lat = lat;
            this.lon = lon;
            this.radius = radius;
        }
    }

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
            alertCount = 0;
            startForegroundService();
            startGeofencingAlerts();
        }
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Geofencing Service",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void startForegroundService() {
        // Create geofence visualization
        Bitmap largeIcon = createGeofenceBitmap();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("📍 Geofencing Active")
                .setContentText("Monitoring travel hotspots...")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setLargeIcon(largeIcon)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(NOTIFICATION_ID, notification);
        handler.post(() -> Toast.makeText(this, "📍 Geofencing started", Toast.LENGTH_SHORT).show());
    }

    // Create a bitmap showing geofence circles
    private Bitmap createGeofenceBitmap() {
        int size = 256;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        // Draw background
        canvas.drawColor(Color.parseColor("#2196F3"));

        // Draw concentric circles (geofence visualization)
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setAntiAlias(true);

        int centerX = size / 2;
        int centerY = size / 2;

        // Draw 3 concentric circles
        for (int i = 1; i <= 3; i++) {
            int radius = 40 * i;
            canvas.drawCircle(centerX, centerY, radius, paint);
        }

        // Draw center point
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centerX, centerY, 12, paint);

        return bitmap;
    }

    private void startGeofencingAlerts() {
        new Thread(() -> {
            while (isRunning && alertCount < 5) { // Limit to 5 alerts
                try {
                    Thread.sleep(12000); // Alert every 12 seconds

                    if (alertCount < geofences.size()) {
                        GeofenceLocation geofence = geofences.get(alertCount);
                        alertCount++;

                        handler.post(() -> sendGeofenceAlert(geofence));
                    }

                } catch (InterruptedException e) {
                    break;
                }
            }

            // After 5 alerts, stop service automatically
            if (alertCount >= 5) {
                handler.post(() -> {
                    Toast.makeText(this, "📍 Geofencing completed (5 alerts sent)",
                            Toast.LENGTH_LONG).show();
                    stopSelf();
                });
            }
        }).start();
    }

    private void sendGeofenceAlert(GeofenceLocation geofence) {
        // Create intent
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build notification with geofence details
        String message = String.format("📍 Within %dm of %s\nLat: %.4f, Lon: %.4f",
                geofence.radius, geofence.name, geofence.lat, geofence.lon);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("📍 Geofence Alert #" + alertCount)
                .setContentText("Near " + geofence.name)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message + "\n\n" + geofence.location))
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(android.R.drawable.ic_menu_mapmode, "View Map", pendingIntent)
                .addAction(android.R.drawable.ic_menu_info_details, "Details", pendingIntent)
                .build();

        notificationManager.notify(1000 + alertCount, notification);

        Toast.makeText(this,
                "📍 Geofence " + alertCount + ": " + geofence.name +
                        " (Radius: " + geofence.radius + "m)",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        handler.post(() -> Toast.makeText(this, "📍 Geofencing stopped", Toast.LENGTH_SHORT).show());
    }
}