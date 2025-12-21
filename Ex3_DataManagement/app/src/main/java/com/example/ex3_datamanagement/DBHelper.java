package com.example.ex3_datamanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    // Database Information
    private static final String DATABASE_NAME = "EmployeeDatabase.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "employees";

    // Column Names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_DEPARTMENT = "department";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create employees table
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_EMAIL + " TEXT NOT NULL, " +
                COLUMN_PHONE + " TEXT NOT NULL, " +
                COLUMN_DEPARTMENT + " TEXT NOT NULL)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // INSERT - Add new employee
    public long insertEmployee(String name, String email, String phone, String department) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_DEPARTMENT, department);

        long result = db.insert(TABLE_NAME, null, values);
        db.close();
        return result;
    }

    // GET ALL - Retrieve all employees
    public ArrayList<Employee> getAllEmployees() {
        ArrayList<Employee> employeeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                Employee employee = new Employee();
                employee.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                employee.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                employee.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
                employee.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)));
                employee.setDepartment(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEPARTMENT)));
                employeeList.add(employee);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return employeeList;
    }

    // UPDATE - Modify employee data
    public boolean updateEmployee(int id, String name, String email, String phone, String department) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_DEPARTMENT, department);

        int result = db.update(TABLE_NAME, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    // DELETE - Remove employee
    public boolean deleteEmployee(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME, COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }

    // SEARCH - Find employees by name, email, or department
    public ArrayList<Employee> searchEmployees(String query) {
        ArrayList<Employee> employeeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String searchQuery = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + COLUMN_NAME + " LIKE ? OR " +
                COLUMN_EMAIL + " LIKE ? OR " +
                COLUMN_DEPARTMENT + " LIKE ? " +
                "ORDER BY id DESC";

        Cursor cursor = db.rawQuery(searchQuery,
                new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"});

        if (cursor.moveToFirst()) {
            do {
                Employee employee = new Employee();
                employee.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                employee.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                employee.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
                employee.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)));
                employee.setDepartment(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DEPARTMENT)));
                employeeList.add(employee);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return employeeList;
    }
}