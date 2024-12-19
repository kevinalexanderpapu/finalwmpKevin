package com.example.finalexam;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class EnrollmentActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private int studentId;
    private static final int MAX_CREDITS = 24;
    private TextView totalCreditsText;
    private ArrayList<String> subjectsList;
    private ArrayList<Integer> subjectIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enrollment);

        dbHelper = new DatabaseHelper(this);
        String studentEmail = getIntent().getStringExtra("student_email");
        studentId = dbHelper.getStudentId(studentEmail);

        totalCreditsText = findViewById(R.id.totalCreditsText);
        ListView subjectsListView = findViewById(R.id.subjectsListView);
        ListView enrolledListView = findViewById(R.id.enrolledListView);

        loadSubjects(subjectsListView);
        updateEnrolledSubjects(enrolledListView);

        subjectsListView.setOnItemClickListener((parent, view, position, id) -> {
            handleSubjectSelection(position);
            updateEnrolledSubjects(enrolledListView);
        });
    }

    private void loadSubjects(ListView listView) {
        subjectsList = new ArrayList<>();
        subjectIds = new ArrayList<>();
        Cursor cursor = dbHelper.getAllSubjects();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String code = cursor.getString(1);
            String name = cursor.getString(2);
            int credits = cursor.getInt(3);
            subjectsList.add(String.format("%s - %s (%d credits)", code, name, credits));
            subjectIds.add(id);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, subjectsList);
        listView.setAdapter(adapter);
    }

    private void handleSubjectSelection(int position) {
        int totalCredits = dbHelper.getTotalCredits(studentId);
        Cursor cursor = dbHelper.getAllSubjects();
        cursor.moveToPosition(position);
        int credits = cursor.getInt(3);
        cursor.close();

        if (totalCredits + credits > MAX_CREDITS) {
            Toast.makeText(this, "Exceeds maximum credits (24)", Toast.LENGTH_SHORT).show();
            return;
        }

        int subjectId = subjectIds.get(position);
        if (dbHelper.enrollSubject(studentId, subjectId)) {
            Toast.makeText(this, "Subject enrolled successfully", Toast.LENGTH_SHORT).show();
            updateTotalCredits();
        } else {
            Toast.makeText(this, "Failed to enroll subject", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEnrolledSubjects(ListView listView) {
        ArrayList<String> enrolledList = new ArrayList<>();
        Cursor cursor = dbHelper.getEnrolledSubjects(studentId);

        while (cursor.moveToNext()) {
            String code = cursor.getString(0);
            String name = cursor.getString(1);
            int credits = cursor.getInt(2);
            enrolledList.add(String.format("%s - %s (%d credits)", code, name, credits));
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, enrolledList);
        listView.setAdapter(adapter);
        updateTotalCredits();
    }

    private void updateTotalCredits() {
        int totalCredits = dbHelper.getTotalCredits(studentId);
        totalCreditsText.setText(String.format("Total Credits: %d/%d", totalCredits, MAX_CREDITS));
    }
}
