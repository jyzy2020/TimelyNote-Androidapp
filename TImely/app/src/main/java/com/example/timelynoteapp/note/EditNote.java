package com.example.timelynoteapp.note;

import android.content.Intent;
import android.os.Bundle;

import com.example.timelynoteapp.MainActivity;
import com.example.timelynoteapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class EditNote extends AppCompatActivity {
    Intent data;
    EditText editNoteTitle, editNoteContent;
    FirebaseFirestore fireStore;
    ProgressBar spinner;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fireStore = fireStore.getInstance();
        spinner =  findViewById(R.id.progressBar2);
        user = FirebaseAuth.getInstance().getCurrentUser();

        data = getIntent();

        editNoteTitle = findViewById(R.id.editNoteTitle);
        editNoteContent = findViewById(R.id.editNoteContent);

        String noteTitle = data.getStringExtra("title");
        String noteContent = data.getStringExtra("content");

        editNoteTitle.setText(noteTitle);
        editNoteContent.setText(noteContent);

        FloatingActionButton fab = findViewById(R.id.saveEditedNote);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nTitle = editNoteTitle.getText().toString();
                String nContent = editNoteContent.getText().toString();

                if(nTitle.isEmpty() || nContent.isEmpty()){
                    Toast.makeText(EditNote.this, "Cannot save note with empty field", Toast.LENGTH_SHORT).show();
                    return;
                }

                spinner.setVisibility(View.VISIBLE);

                DocumentReference docref = fireStore.collection("notes").document(user.getUid()).collection("myNotes").document(data.getStringExtra("noteId"));
                Map<String, Object> notes = new HashMap<>();
                notes.put("title",nTitle);
                notes.put("content",nContent);

                docref.update(notes).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EditNote.this, "Note edited", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditNote.this, "Error saving, try again", Toast.LENGTH_SHORT).show();
                        spinner.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
    }
}