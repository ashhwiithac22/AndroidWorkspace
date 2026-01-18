package com.example.ex5_clientservercommunication;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText name, destination, date;
    Button book;
    WebView webView;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.name);
        destination = findViewById(R.id.destination);
        date = findViewById(R.id.date);
        book = findViewById(R.id.book);
        webView = findViewById(R.id.webview);

        dbHelper = new DBHelper(this);

        // Client–Server communication using WebView
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://www.example.com");

        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SQLiteDatabase db = dbHelper.getWritableDatabase();

                db.execSQL(
                        "INSERT INTO bookings (name, destination, date) VALUES (?, ?, ?)",
                        new Object[]{
                                name.getText().toString(),
                                destination.getText().toString(),
                                date.getText().toString()
                        }
                );
            }
        });
    }
}
