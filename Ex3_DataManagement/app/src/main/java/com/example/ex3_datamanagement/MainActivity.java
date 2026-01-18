package com.example.ex3_datamanagement;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // UI Elements
    private EditText etName, etEmail, etPhone, etDepartment, etSearch;
    private LinearLayout dataContainer;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        dbHelper = new DBHelper(this);

        // Initialize UI elements
        initializeViews();

        // Set button listeners
        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnViewAll = findViewById(R.id.btnViewAll);
        Button btnSearch = findViewById(R.id.btnSearch);
        Button btnClearSearch = findViewById(R.id.btnClearSearch);

        btnAdd.setOnClickListener(v -> addEmployee());
        btnViewAll.setOnClickListener(v -> loadAllData());
        btnSearch.setOnClickListener(v -> searchEmployees());
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            loadAllData();
        });

        // Load data on app start
        loadAllData();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etDepartment = findViewById(R.id.etDepartment);
        etSearch = findViewById(R.id.etSearch);
        dataContainer = findViewById(R.id.dataContainer);
    }

    // 1. INSERT Operation
    private void addEmployee() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();

        // Validation
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || department.isEmpty()) {
            showToast("Please fill all fields");
            return;
        }

        // Insert into database
        long result = dbHelper.insertEmployee(name, email, phone, department);

        if (result != -1) {
            showToast("Employee added successfully");
            clearInputFields();
            loadAllData(); // Refresh list
        } else {
            showToast("Failed to add employee");
        }
    }

    // 2. VIEW Operation
    private void loadAllData() {
        dataContainer.removeAllViews();

        ArrayList<Employee> employees = dbHelper.getAllEmployees();

        if (employees.isEmpty()) {
            showEmptyMessage("📭 No employees found. Add your first employee!");
            return;
        }

        for (Employee emp : employees) {
            createEmployeeCard(emp);
        }
    }

    // 3. SEARCH Operation
    private void searchEmployees() {
        String query = etSearch.getText().toString().trim();

        if (query.isEmpty()) {
            loadAllData();
            return;
        }

        dataContainer.removeAllViews();
        ArrayList<Employee> employees = dbHelper.searchEmployees(query);

        if (employees.isEmpty()) {
            showEmptyMessage("🔍 No matching employees found");
            return;
        }

        for (Employee emp : employees) {
            createEmployeeCard(emp);
        }
    }

    // 4. EDIT Operation
    private void editEmployee(Employee employee) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✏️ Edit Employee");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit, null);
        EditText etEditName = dialogView.findViewById(R.id.etEditName);
        EditText etEditEmail = dialogView.findViewById(R.id.etEditEmail);
        EditText etEditPhone = dialogView.findViewById(R.id.etEditPhone);
        EditText etEditDepartment = dialogView.findViewById(R.id.etEditDepartment);

        // Pre-fill current data
        etEditName.setText(employee.getName());
        etEditEmail.setText(employee.getEmail());
        etEditPhone.setText(employee.getPhone());
        etEditDepartment.setText(employee.getDepartment());

        builder.setView(dialogView);
        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = etEditName.getText().toString().trim();
            String newEmail = etEditEmail.getText().toString().trim();
            String newPhone = etEditPhone.getText().toString().trim();
            String newDept = etEditDepartment.getText().toString().trim();

            if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty() || newDept.isEmpty()) {
                showToast("All fields are required");
                return;
            }

            boolean updated = dbHelper.updateEmployee(
                    employee.getId(),
                    newName,
                    newEmail,
                    newPhone,
                    newDept
            );

            if (updated) {
                showToast("✅ Employee updated successfully");
                loadAllData();
            } else {
                showToast("❌ Update failed");
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // 5. DELETE Operation
    private void deleteEmployee(Employee employee) {
        new AlertDialog.Builder(this)
                .setTitle("🗑️ Confirm Delete")
                .setMessage("Are you sure you want to delete " + employee.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean deleted = dbHelper.deleteEmployee(employee.getId());

                    if (deleted) {
                        showToast("✅ Employee deleted successfully");
                        loadAllData();
                    } else {
                        showToast("❌ Delete failed");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Create beautiful employee card UI
    private void createEmployeeCard(Employee employee) {
        // Create CardView
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(16, 8, 16, 16);
        card.setLayoutParams(cardParams);
        card.setCardElevation(8);
        card.setRadius(16);
        card.setCardBackgroundColor(getResources().getColor(R.color.card_background));

        // Card content layout
        LinearLayout cardLayout = new LinearLayout(this);
        cardLayout.setOrientation(LinearLayout.VERTICAL);
        cardLayout.setPadding(24, 24, 24, 24);

        // Employee Name (Large and colored)
        TextView tvName = new TextView(this);
        tvName.setText(employee.getName());
        tvName.setTextSize(20);
        tvName.setTextColor(getResources().getColor(R.color.primary));
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        cardLayout.addView(tvName);

        // Employee Details with icons
        addDetailWithIcon(cardLayout, "📧", employee.getEmail());
        addDetailWithIcon(cardLayout, "📱", employee.getPhone());
        addDetailWithIcon(cardLayout, "🏢", employee.getDepartment());

        // Action buttons
        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        btnLayout.setPadding(0, 16, 0, 0);

        // Edit Button
        Button btnEdit = new Button(this);
        btnEdit.setText("✏️ Edit");
        btnEdit.setBackgroundColor(getResources().getColor(R.color.edit_color));
        btnEdit.setTextColor(getResources().getColor(R.color.white));
        btnEdit.setPadding(24, 12, 24, 12);
        btnEdit.setOnClickListener(v -> editEmployee(employee));

        // Delete Button
        Button btnDelete = new Button(this);
        btnDelete.setText("🗑️ Delete");
        btnDelete.setBackgroundColor(getResources().getColor(R.color.delete_color));
        btnDelete.setTextColor(getResources().getColor(R.color.white));
        btnDelete.setPadding(24, 12, 24, 12);
        btnDelete.setOnClickListener(v -> deleteEmployee(employee));

        // Set button layout params
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        btnParams.setMargins(4, 0, 4, 0);
        btnEdit.setLayoutParams(btnParams);
        btnDelete.setLayoutParams(btnParams);

        btnLayout.addView(btnEdit);
        btnLayout.addView(btnDelete);
        cardLayout.addView(btnLayout);

        card.addView(cardLayout);
        dataContainer.addView(card);
    }

    private void addDetailWithIcon(LinearLayout layout, String icon, String text) {
        TextView tv = new TextView(this);
        tv.setText(icon + " " + text);
        tv.setTextSize(15);
        tv.setTextColor(getResources().getColor(R.color.text_secondary));
        tv.setPadding(0, 6, 0, 0);
        layout.addView(tv);
    }

    private void showEmptyMessage(String message) {
        TextView tvEmpty = new TextView(this);
        tvEmpty.setText(message);
        tvEmpty.setTextSize(16);
        tvEmpty.setTextColor(getResources().getColor(R.color.text_secondary));
        tvEmpty.setPadding(0, 40, 0, 40);
        tvEmpty.setGravity(View.TEXT_ALIGNMENT_CENTER);
        dataContainer.addView(tvEmpty);
    }

    private void clearInputFields() {
        etName.setText("");
        etEmail.setText("");
        etPhone.setText("");
        etDepartment.setText("");
        etName.requestFocus();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}