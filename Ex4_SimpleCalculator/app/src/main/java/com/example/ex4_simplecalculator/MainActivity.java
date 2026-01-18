package com.example.ex4_simplecalculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Declare UI components
    private EditText etNumber1, etNumber2;
    private TextView tvResult;
    private Button btnAdd, btnSubtract, btnMultiply, btnDivide, btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        initializeViews();

        // Set click listeners for buttons
        setButtonListeners();
    }

    private void initializeViews() {
        etNumber1 = findViewById(R.id.etNumber1);
        etNumber2 = findViewById(R.id.etNumber2);
        tvResult = findViewById(R.id.tvResult);
        btnAdd = findViewById(R.id.btnAdd);
        btnSubtract = findViewById(R.id.btnSubtract);
        btnMultiply = findViewById(R.id.btnMultiply);
        btnDivide = findViewById(R.id.btnDivide);
        btnClear = findViewById(R.id.btnClear);
    }

    private void setButtonListeners() {
        // Addition button
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performAddition();
            }
        });

        // Subtraction button
        btnSubtract.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSubtraction();
            }
        });

        // Multiplication button
        btnMultiply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performMultiplication();
            }
        });

        // Division button
        btnDivide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performDivision();
            }
        });

        // Clear button
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAll();
            }
        });
    }

    // Addition operation
    private void performAddition() {
        if (validateInputs()) {
            try {
                double num1 = Double.parseDouble(etNumber1.getText().toString());
                double num2 = Double.parseDouble(etNumber2.getText().toString());
                double result = num1 + num2;
                displayResult("+", result);
            } catch (NumberFormatException e) {
                showError("Invalid number format");
            }
        }
    }

    // Subtraction operation
    private void performSubtraction() {
        if (validateInputs()) {
            try {
                double num1 = Double.parseDouble(etNumber1.getText().toString());
                double num2 = Double.parseDouble(etNumber2.getText().toString());
                double result = num1 - num2;
                displayResult("-", result);
            } catch (NumberFormatException e) {
                showError("Invalid number format");
            }
        }
    }

    // Multiplication operation
    private void performMultiplication() {
        if (validateInputs()) {
            try {
                double num1 = Double.parseDouble(etNumber1.getText().toString());
                double num2 = Double.parseDouble(etNumber2.getText().toString());
                double result = num1 * num2;
                displayResult("×", result);
            } catch (NumberFormatException e) {
                showError("Invalid number format");
            }
        }
    }

    // Division operation
    private void performDivision() {
        if (validateInputs()) {
            try {
                double num1 = Double.parseDouble(etNumber1.getText().toString());
                double num2 = Double.parseDouble(etNumber2.getText().toString());

                if (num2 == 0) {
                    showError("Cannot divide by zero");
                    return;
                }

                double result = num1 / num2;
                displayResult("÷", result);
            } catch (NumberFormatException e) {
                showError("Invalid number format");
            }
        }
    }

    // Validate input fields
    private boolean validateInputs() {
        String num1 = etNumber1.getText().toString().trim();
        String num2 = etNumber2.getText().toString().trim();

        if (num1.isEmpty() || num2.isEmpty()) {
            showError("Please enter both numbers");
            return false;
        }

        return true;
    }

    // Display result
    private void displayResult(String operator, double result) {
        String num1 = etNumber1.getText().toString();
        String num2 = etNumber2.getText().toString();

        // Format result
        String resultText;
        if (result == (int) result) {
            resultText = String.valueOf((int) result);
        } else {
            // Limit to 4 decimal places
            resultText = String.format("%.4f", result).replaceAll("0*$", "").replaceAll("\\.$", "");
        }

        String displayText = num1 + " " + operator + " " + num2 + " = " + resultText;
        tvResult.setText(displayText);
        tvResult.setTextColor(getResources().getColor(R.color.result_success));
    }

    // Clear all fields
    private void clearAll() {
        etNumber1.setText("");
        etNumber2.setText("");
        tvResult.setText("Result will appear here");
        tvResult.setTextColor(getResources().getColor(R.color.text_hint));
        etNumber1.requestFocus();
    }

    // Show error message
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        tvResult.setText("Error: " + message);
        tvResult.setTextColor(getResources().getColor(R.color.result_error));
    }
}