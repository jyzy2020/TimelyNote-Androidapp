package com.example.timelynoteapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.timelynoteapp.auth.Login;
import com.example.timelynoteapp.auth.Register;
import com.example.timelynoteapp.model.Adapter;
import com.example.timelynoteapp.model.Note;
import com.example.timelynoteapp.note.AddNotes;
import com.example.timelynoteapp.note.EditNote;
import com.example.timelynoteapp.note.NoteDetails;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firestore.v1.FirestoreGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    RecyclerView noteList;
    Adapter adapter;
    FirebaseFirestore fireStore;
    FirestoreRecyclerAdapter<Note,NoteViewHolder> noteAdapter;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;

    // set up channel for notification
    public static final String CHANNEL_ID = "timelyappcapstone2020";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fireStore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        Query query = fireStore.collection("notes").document(user.getUid()).collection("myNotes").orderBy("title", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Note> allNotes = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query,Note.class)
                .build();

        noteAdapter = new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, final int i, @NonNull final Note note) {
                noteViewHolder.noteTitle.setText(note.getTitle());
                noteViewHolder.noteContent.setText(note.getContent());
                // final int code = getRandomColor();
                // noteViewHolder.rCardView.setCardBackgroundColor(noteViewHolder.view.getResources().getColor(code,null));
                final String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();

                noteViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(v.getContext(), NoteDetails.class);
                        i.putExtra("title", note.getTitle());
                        i.putExtra("content", note.getContent());
                        // i.putExtra("code", code);
                        i.putExtra("noteId", docId);
                        v.getContext().startActivity(i);
                    }
                });

                ImageView menuIcon = noteViewHolder.view.findViewById(R.id.menuIcon);
                menuIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        final String docId = noteAdapter.getSnapshots().getSnapshot(i).getId();
                        PopupMenu menu = new PopupMenu(v.getContext(),v);
                        menu.setGravity(Gravity.END);
                        menu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Intent i = new Intent(v.getContext(), EditNote.class);
                                i.putExtra("title",note.getTitle());
                                i.putExtra("content",note.getContent());
                                i.putExtra("noteId", docId);
                                startActivity(i);
                                return false;
                            }
                        });

                        menu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                DocumentReference docRef = fireStore.collection("notes").document(user.getUid()).collection("myNotes").document(docId);
                                docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, "Error in deleting", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });

                        menu.show();
                    }
                });
            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout,parent,false);
                return new NoteViewHolder(view);
            }
        };

        noteList = findViewById(R.id.rec_view);

        drawerLayout = findViewById(R.id.drawer);
        nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        noteList.setLayoutManager(new StaggeredGridLayoutManager(1,StaggeredGridLayoutManager.VERTICAL));
        noteList.setAdapter(noteAdapter);

        View headerView = nav_view.getHeaderView(0);
        TextView username = headerView.findViewById(R.id.userDisplayName);
        TextView userEmail = headerView.findViewById(R.id.userDisplayEmail);

        if(user.isAnonymous()){
            userEmail.setVisibility(View.GONE);
            username.setText("Temporary user");
        }else{
            userEmail.setText(user.getEmail());
            username.setText(user.getDisplayName());
        }

        FloatingActionButton fab = findViewById(R.id.addNoteFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), AddNotes.class));
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
            }
        });

        // responsible for instantiating firebase cloud messaging
        // firebase cloud messaging
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if(task.isSuccessful()) {

                            String token = task.getResult().getToken();
                            Log.d("TAG", "Our token: " + token);
                        }
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case R.id.notes:
                recreate();
                break;
            case R.id.addNote:
                startActivity(new Intent(this,AddNotes.class));
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                break;

            case R.id.sync:
                if (user.isAnonymous()) {
                    startActivity(new Intent(this, Login.class));
                    overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                }
                else {
                    Toast.makeText(this, "Account already connected", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.EventManager:
                startActivity(new Intent(this, EventManager.class));
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                break;

            case R.id.ViewCalendar:
               startActivity(new Intent(this, CalendarView.class));
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                break;

            case R.id.logout:
                checkUser ();
                break;

            default:
                Toast.makeText(this,"Coming soon",Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void checkUser (){
        if(user.isAnonymous()){
            displayAlert();
        }else{
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(),Splash.class));
            overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
        }
    }

    private void displayAlert() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("Logging out as a Temporary User will delete all your current notes")
                .setPositiveButton("Sync Note", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), Register.class));
                        finish();
                    }
                }).setNegativeButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startActivity(new Intent(getApplicationContext(),Splash.class));
                                finish();
                            }
                        });
                    }
                });

        warning.show();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView noteTitle, noteContent;
        View view;
        CardView rCardView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.titles);
            noteContent = itemView.findViewById(R.id.content);
            rCardView = itemView.findViewById(R.id.noteCard);
            view = itemView;
        }
    }

    /*{
        private int getRandomColor () {
            List <Integer> colorCode = new ArrayList<>();
            colorCode.add(R.color.blue);
            colorCode.add(R.color.yellow);
            colorCode.add(R.color.skyblue);
            colorCode.add(R.color.lightPurple);
            colorCode.add(R.color.lightGreen);
            colorCode.add(R.color.gray);
            colorCode.add(R.color.pink);
            colorCode.add(R.color.red);
            colorCode.add(R.color.greenlight);
            colorCode.add(R.color.notgreen);

            Random randomColor = new Random();
            int number = randomColor.nextInt(colorCode.size());
            return colorCode.get (number);
        }
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (noteAdapter != null){
            noteAdapter.stopListening();
        }
    }
}