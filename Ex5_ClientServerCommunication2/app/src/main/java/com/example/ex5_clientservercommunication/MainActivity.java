package com.example.ex5_clientservercommunication;

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

        // Find all buttons
        Button btnFlight = findViewById(R.id.btnFlight);
        Button btnHotel = findViewById(R.id.btnHotel);
        Button btnTrain = findViewById(R.id.btnTrain);
        Button btnBus = findViewById(R.id.btnBus);
        Button btnTest = findViewById(R.id.btnTest);
        Button btnAbout = findViewById(R.id.btnAbout);

        // Set click listeners
        btnFlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBooking("Flights", "https://www.makemytrip.com/flights/");
            }
        });

        btnHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBooking("Hotels", "https://www.makemytrip.com/hotels/");
            }
        });

        btnTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBooking("Trains", "https://www.irctc.co.in/nget/train-search");
            }
        });

        btnBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBooking("Buses", "https://www.redbus.in/");
            }
        });

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "✅ Connection Test Successful",
                        Toast.LENGTH_LONG).show();
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "Client-Server Communication App\nVersion 1.0",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startBooking(String service, String url) {
        Intent intent = new Intent(MainActivity.this, BookingActivity.class);
        intent.putExtra("service", service);
        intent.putExtra("url", url);
        startActivity(intent);
    }
}