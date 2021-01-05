package com.example.timelynoteapp.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.timelynoteapp.MainActivity;
import com.example.timelynoteapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class Register extends AppCompatActivity {
    EditText rUserName,rUserEmail,rUserPass,rUserConfPass;
    Button register;
    TextView loginAct;
    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getSupportActionBar().setTitle("Create Timely account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rUserName = findViewById(R.id.userName);
        rUserEmail = findViewById(R.id.userEmail);
        rUserPass = findViewById(R.id.password);
        rUserConfPass = findViewById(R.id.passwordConfirm);

        register = findViewById(R.id.createAccount);
        loginAct = findViewById(R.id.login);
        progressBar = findViewById(R.id.progressBar4);

        firebaseAuth = FirebaseAuth.getInstance();

        loginAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userName = rUserName.getText().toString();
                final String userEmail = rUserEmail.getText().toString();
                final String userPass = rUserPass.getText().toString();
                final String userConfPass = rUserConfPass.getText().toString();

                if(userEmail.isEmpty() || userName.isEmpty() || userPass.isEmpty() || userConfPass.isEmpty()){
                    Toast.makeText(Register.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!userPass.equals(userConfPass)){
                    rUserConfPass.setError("Password do not match");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                AuthCredential credential = EmailAuthProvider.getCredential(userEmail,userPass);
                firebaseAuth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {

                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(Register.this, "Notes are synced", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));

                        FirebaseUser usr = firebaseAuth.getCurrentUser();
                        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder ()
                                .setDisplayName(userName)
                                .build();
                        usr.updateProfile(request);

                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        if(!(isEmailValid(userEmail))) {
                            Toast.makeText(Register.this, "Email Format is incorrect!", Toast.LENGTH_SHORT).show();
                        } else if(userPass.length() < 6) {
                            Toast.makeText(Register.this, "Password length must be 6 characters!", Toast.LENGTH_SHORT).show();
                        } else if(!(userConfPass.equals(userPass))) {
                            Toast.makeText(Register.this, "The Confirm password does not match!", Toast.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
        return super.onOptionsItemSelected(item);
    }
}