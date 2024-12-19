package com.example.finalexam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "enrollment.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
    private static final String TABLE_STUDENTS = "students";
    private static final String TABLE_SUBJECTS = "subjects";
    private static final String TABLE_ENROLLMENTS = "enrollments";

    // Common columns
    private static final String COLUMN_ID = "id";

    // Student table columns
    private static final String COLUMN_STUDENT_NAME = "name";
    private static final String COLUMN_STUDENT_EMAIL = "email";
    private static final String COLUMN_STUDENT_PASSWORD = "password";

    // Subject table columns
    private static final String COLUMN_SUBJECT_CODE = "code";
    private static final String COLUMN_SUBJECT_NAME = "name";
    private static final String COLUMN_CREDITS = "credits";

    // Enrollment table columns
    private static final String COLUMN_STUDENT_ID = "student_id";
    private static final String COLUMN_SUBJECT_ID = "subject_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create students table
        String CREATE_STUDENTS_TABLE = "CREATE TABLE " + TABLE_STUDENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_STUDENT_NAME + " TEXT,"
                + COLUMN_STUDENT_EMAIL + " TEXT UNIQUE,"
                + COLUMN_STUDENT_PASSWORD + " TEXT" + ")";

        // Create subjects table
        String CREATE_SUBJECTS_TABLE = "CREATE TABLE " + TABLE_SUBJECTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SUBJECT_CODE + " TEXT UNIQUE,"
                + COLUMN_SUBJECT_NAME + " TEXT,"
                + COLUMN_CREDITS + " INTEGER" + ")";

        // Create enrollments table
        String CREATE_ENROLLMENTS_TABLE = "CREATE TABLE " + TABLE_ENROLLMENTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_STUDENT_ID + " INTEGER,"
                + COLUMN_SUBJECT_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_STUDENT_ID + ") REFERENCES " + TABLE_STUDENTS + "(" + COLUMN_ID + "),"
                + "FOREIGN KEY(" + COLUMN_SUBJECT_ID + ") REFERENCES " + TABLE_SUBJECTS + "(" + COLUMN_ID + ")" + ")";

        db.execSQL(CREATE_STUDENTS_TABLE);
        db.execSQL(CREATE_SUBJECTS_TABLE);
        db.execSQL(CREATE_ENROLLMENTS_TABLE);

        // Insert sample subjects
        insertSampleSubjects(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENROLLMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBJECTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
        onCreate(db);
    }

    private void insertSampleSubjects(SQLiteDatabase db) {
        String[][] subjects = {
                {"CS101", "Introduction to Programming", "3"},
                {"CS102", "Data Structures", "4"},
                {"CS201", "Database Systems", "3"},
                {"CS202", "Web Development", "4"},
                {"CS301", "Software Engineering", "4"},
                {"CS302", "Mobile Development", "4"},
                {"CS401", "Artificial Intelligence", "4"},
                {"CS402", "Computer Networks", "3"}
        };

        for (String[] subject : subjects) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_SUBJECT_CODE, subject[0]);
            values.put(COLUMN_SUBJECT_NAME, subject[1]);
            values.put(COLUMN_CREDITS, Integer.parseInt(subject[2]));
            db.insert(TABLE_SUBJECTS, null, values);
        }
    }

    // Student operations
    public long registerStudent(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STUDENT_NAME, name);
        values.put(COLUMN_STUDENT_EMAIL, email);
        values.put(COLUMN_STUDENT_PASSWORD, password);
        return db.insert(TABLE_STUDENTS, null, values);
    }

    public boolean checkLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_STUDENT_EMAIL + "=? AND " + COLUMN_STUDENT_PASSWORD + "=?";
        String[] selectionArgs = {email, password};
        Cursor cursor = db.query(TABLE_STUDENTS, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public int getStudentId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_STUDENT_EMAIL + "=?";
        String[] selectionArgs = {email};
        Cursor cursor = db.query(TABLE_STUDENTS, columns, selection, selectionArgs, null, null, null);
        int id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        }
        cursor.close();
        return id;
    }

    // Subject and enrollment operations
    public Cursor getAllSubjects() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_SUBJECTS, null, null, null, null, null, null);
    }

    public boolean enrollSubject(int studentId, int subjectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STUDENT_ID, studentId);
        values.put(COLUMN_SUBJECT_ID, subjectId);
        return db.insert(TABLE_ENROLLMENTS, null, values) != -1;
    }

    public int getTotalCredits(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + TABLE_SUBJECTS + "." + COLUMN_CREDITS + ") FROM " + TABLE_ENROLLMENTS +
                " JOIN " + TABLE_SUBJECTS + " ON " + TABLE_ENROLLMENTS + "." + COLUMN_SUBJECT_ID +
                " = " + TABLE_SUBJECTS + "." + COLUMN_ID +
                " WHERE " + TABLE_ENROLLMENTS + "." + COLUMN_STUDENT_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(studentId)});
        int totalCredits = 0;
        if (cursor.moveToFirst()) {
            totalCredits = cursor.getInt(0);
        }
        cursor.close();
        return totalCredits;
    }

    public Cursor getEnrolledSubjects(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + TABLE_SUBJECTS + "." + COLUMN_SUBJECT_CODE + ", " +
                TABLE_SUBJECTS + "." + COLUMN_SUBJECT_NAME + ", " +
                TABLE_SUBJECTS + "." + COLUMN_CREDITS +
                " FROM " + TABLE_ENROLLMENTS +
                " JOIN " + TABLE_SUBJECTS + " ON " + TABLE_ENROLLMENTS + "." + COLUMN_SUBJECT_ID +
                " = " + TABLE_SUBJECTS + "." + COLUMN_ID +
                " WHERE " + TABLE_ENROLLMENTS + "." + COLUMN_STUDENT_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(studentId)});
    }
}
