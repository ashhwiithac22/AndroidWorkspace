package com.example.ex1_simplegui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    LinearLayout rootLayout;
    EditText usernameEditText, passwordEditText;
    Button bgColorButton, fontSizeButton;
    boolean colorFlag = true;
    boolean fontFlag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Must be first

        rootLayout = findViewById(R.id.rootLayout);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        bgColorButton = findViewById(R.id.bgColorButton);
        fontSizeButton = findViewById(R.id.fontSizeButton);

        bgColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (colorFlag) {
                    rootLayout.setBackgroundColor(Color.YELLOW);
                } else {
                    rootLayout.setBackgroundColor(Color.CYAN);
                }
                colorFlag = !colorFlag;
            }
        });

        fontSizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fontFlag) {
                    usernameEditText.setTextSize(24);
                    passwordEditText.setTextSize(24);
                } else {
                    usernameEditText.setTextSize(16);
                    passwordEditText.setTextSize(16);
                }
                fontFlag = !fontFlag;
            }
        });
    }
}
