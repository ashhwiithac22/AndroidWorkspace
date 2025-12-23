package com.example.ex5_clientservercommunication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find buttons by ID
        Button btnFlight = findViewById(R.id.btnFlight);
        @SuppressLint("MissingInflatedId") Button btnHotel = findViewById(R.id.btnHotel);
        Button btnTrain = findViewById(R.id.btnTrain);
        Button btnBus = findViewById(R.id.btnBus);
        Button btnTest = findViewById(R.id.btnTest);
        Button btnAbout = findViewById(R.id.btnAbout);

        // Set click listeners
        btnFlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBooking("Flights", "https://www.makemytrip.com/flights/");
            }
        });

        btnHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBooking("Hotels", "https://www.makemytrip.com/hotels/");
            }
        });

        btnTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBooking("Trains", "https://www.irctc.co.in/nget/train-search");
            }
        });

        btnBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBooking("Buses", "https://www.redbus.in/");
            }
        });

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "✅ Connection Test Successful!\nInternet is working properly.",
                        Toast.LENGTH_LONG).show();
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String info = "Client-Server Communication App\n" +
                        "Version 1.0\n\n" +
                        "Features:\n" +
                        "• Flight Booking\n" +
                        "• Hotel Booking\n" +
                        "• Train Booking\n" +
                        "• Bus Booking\n\n" +
                        "Using Android WebView for communication.";
                Toast.makeText(MainActivity.this, info, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openBooking(String service, String url) {
        Intent intent = new Intent(MainActivity.this, BookingActivity.class);
        intent.putExtra("SERVICE", service);
        intent.putExtra("URL", url);
        startActivity(intent);
    }
}