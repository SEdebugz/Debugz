package com.example.debugz.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.debugz.R;
import com.example.debugz.models.Student;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView rvStudents;
    private EditText etSearchStudent;
    private StudentAdapter adapter;
    private List<Student> allStudents = new ArrayList<>();
    private List<Student> filteredStudents = new ArrayList<>();
    private String currentStudentId = "demo_student_123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        db = FirebaseFirestore.getInstance();
        rvStudents = findViewById(R.id.rvStudents);
        etSearchStudent = findViewById(R.id.etSearchStudent);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StudentAdapter(filteredStudents, targetStudent -> {
            db.collection("students").document(currentStudentId)
                    .update("friendIds", FieldValue.arrayUnion(targetStudent.getStudentId()))
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added " + targetStudent.getName() + "!", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to add friend", Toast.LENGTH_SHORT).show());
        });

        rvStudents.setAdapter(adapter);

        etSearchStudent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fetchAllStudents();
    }

    private void fetchAllStudents() {
        db.collection("students").get().addOnSuccessListener(queryDocumentSnapshots -> {
            allStudents.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Student student = doc.toObject(Student.class);
                student.setStudentId(doc.getId());
                if (!student.getStudentId().equals(currentStudentId)) {
                    allStudents.add(student);
                }
            }
            filter("");
        });
    }

    private void filter(String query) {
        filteredStudents.clear();
        if (query.isEmpty()) {
            filteredStudents.addAll(allStudents);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Student student : allStudents) {
                if (student.getName().toLowerCase().contains(lowerQuery)) {
                    filteredStudents.add(student);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}