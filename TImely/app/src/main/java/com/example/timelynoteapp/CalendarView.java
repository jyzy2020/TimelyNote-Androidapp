package com.example.timelynoteapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.timelynoteapp.auth.Login;
import com.example.timelynoteapp.auth.Register;
import com.example.timelynoteapp.note.AddNotes;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CalendarView extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private WebView webView;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    FirebaseFirestore fireStore;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        webView=(WebView)findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("https://calendar.google.com/calendar/");

        WebSettings webSettings=webView.getSettings();
        webView.getSettings().setUseWideViewPort(true);
        webView.setInitialScale(1);
        webView.setPadding(0, 0, 0, 0);
        webSettings.setJavaScriptEnabled(true);

        fireStore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        drawerLayout = findViewById(R.id.drawer);
        nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

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
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case R.id.notes:
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                break;

            case R.id.addNote:
                startActivity(new Intent(this, AddNotes.class));
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
                recreate();
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
}