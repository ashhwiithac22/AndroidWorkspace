package com.example.ex2_travelapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private Button btnStartLocation, btnStopLocation, btnNotification;
    private Button btnStartMedia, btnStopMedia, btnStartGeo, btnStopGeo;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize buttons
        btnStartLocation = findViewById(R.id.btnStartLocation);
        btnStopLocation = findViewById(R.id.btnStopLocation);
        btnNotification = findViewById(R.id.btnNotification);
        btnStartMedia = findViewById(R.id.btnStartMedia);
        btnStopMedia = findViewById(R.id.btnStopMedia);
        btnStartGeo = findViewById(R.id.btnStartGeo);
        btnStopGeo = findViewById(R.id.btnStopGeo);

        // Request permissions
        requestPermissions();

        // Button listeners
        btnStartLocation.setOnClickListener(v -> startService(new Intent(this, LocationService.class)));
        btnStopLocation.setOnClickListener(v -> stopService(new Intent(this, LocationService.class)));

        btnNotification.setOnClickListener(v -> sendSingleNotification());

        btnStartMedia.setOnClickListener(v -> startService(new Intent(this, MediaService.class)));
        btnStopMedia.setOnClickListener(v -> stopService(new Intent(this, MediaService.class)));

        btnStartGeo.setOnClickListener(v -> startService(new Intent(this, GeofencingService.class)));
        btnStopGeo.setOnClickListener(v -> stopService(new Intent(this, GeofencingService.class)));
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
            };

            boolean allGranted = true;
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void sendSingleNotification() {
        Intent serviceIntent = new Intent(this, NotificationService.class);
        startService(serviceIntent);
        Toast.makeText(this, "Sending one notification...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop all services when app closes
        stopService(new Intent(this, LocationService.class));
        stopService(new Intent(this, MediaService.class));
        stopService(new Intent(this, GeofencingService.class));
    }
}