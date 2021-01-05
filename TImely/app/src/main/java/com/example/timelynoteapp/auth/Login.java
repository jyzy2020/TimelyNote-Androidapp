package com.example.timelynoteapp.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.timelynoteapp.MainActivity;
import com.example.timelynoteapp.R;
import com.example.timelynoteapp.Splash;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    EditText lEmail, lPassword;
    Button loginNow;
    TextView forgetPass,createAcc;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fireStore;
    FirebaseUser user;
    ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("Login to Timely");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lEmail = findViewById(R.id.lemail);
        lPassword = findViewById(R.id.lPassword);
        loginNow = findViewById(R.id.loginBtn);
        spinner = findViewById(R.id.progressBar3);

        createAcc = findViewById(R.id.createAccount);
        user = FirebaseAuth.getInstance().getCurrentUser();

        firebaseAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();

        showWarning ();

        loginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mEmail = lEmail.getText().toString();
                String mPassword = lPassword.getText().toString();

                if(mEmail.isEmpty() || mPassword.isEmpty()){
                    Toast.makeText(Login.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

//                if(firebaseAuth.getCurrentUser().isAnonymous()){
//                    FirebaseUser user = firebaseAuth.getCurrentUser();
//
//                    fireStore.collection("notes").document(user.getUid()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Toast.makeText(Login.this, "All temp notes are deleted", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                    user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Toast.makeText(Login.this, "Temp user deleted", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }

                spinner.setVisibility(View.VISIBLE);

                firebaseAuth.signInWithEmailAndPassword(mEmail,mPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {

                    @Override
                    public void onSuccess(AuthResult authResult) {

                        Toast.makeText(Login.this, "Log in successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Login.this, "Log in failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        spinner.setVisibility(View.GONE);
                    }
                });
            }
        });

        createAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Register.class));
            }
        });
    }

    private void showWarning() {
        AlertDialog.Builder warning = new AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("Syncing Existing Account will delete all the temp notes. Create new account to save them")
                .setPositiveButton("Save notes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), Register.class));
                        finish();
                    }
                }).setNegativeButton("Its Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        warning.show();
    }
}