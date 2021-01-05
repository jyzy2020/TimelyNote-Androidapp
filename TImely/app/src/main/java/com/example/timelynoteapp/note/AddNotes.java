package com.example.timelynoteapp.note;

import android.os.Bundle;

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

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class AddNotes extends AppCompatActivity {
    FirebaseFirestore fireStore;
    EditText noteTitle, noteContent;
    ProgressBar progressBarSave;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notes);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fireStore = FirebaseFirestore.getInstance();
        noteTitle = findViewById(R.id.addNoteTitle);
        noteContent = findViewById(R.id.addNoteContent);
        progressBarSave = findViewById(R.id.progressBar);

        user = FirebaseAuth.getInstance().getCurrentUser();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nTitle = noteTitle.getText().toString();
                String nContent = noteContent.getText().toString();

                if(nTitle.isEmpty() || nContent.isEmpty()){
                    Toast.makeText(AddNotes.this, "Cannot save note with empty field", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBarSave.setVisibility(View.VISIBLE);

                DocumentReference docref = fireStore.collection("notes").document(user.getUid()).collection("myNotes").document();
                Map<String, Object> notes = new HashMap<>();
                notes.put("title",nTitle);
                notes.put("content",nContent);

                docref.set(notes).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddNotes.this, "Note added", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddNotes.this, "Error saving, try again", Toast.LENGTH_SHORT).show();
                        progressBarSave.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.close_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.close){
            Toast.makeText(this, "Message not saved", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}