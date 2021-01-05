package com.example.timelynoteapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.timelynoteapp.auth.Login;
import com.example.timelynoteapp.auth.Register;
import com.example.timelynoteapp.model.Adapter;
import com.example.timelynoteapp.model.Note;
import com.example.timelynoteapp.note.AddNotes;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.sql.Time;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class EventManager extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener{

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView nav_view;
    FirebaseFirestore fireStore;
    FirebaseUser user;
    FirebaseAuth firebaseAuth;


    // set up channel for notification
    public static final String CHANNEL_ID = "timelyappcapstone2020";

    EditText title, location, description;
    TextView startDate, remindTime, endDate;
    Button addEvent;
    long tempDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_manager);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startDate = findViewById(R.id.etStartDate);

        fireStore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        nav_view = findViewById(R.id.nav_view);
        nav_view.setNavigationItemSelectedListener(this);
        drawerLayout = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        View headerView = nav_view.getHeaderView(0);
        TextView username = headerView.findViewById(R.id.userDisplayName);
        TextView userEmail = headerView.findViewById(R.id.userDisplayEmail);

        if(user.isAnonymous()) {
            userEmail.setVisibility(View.GONE);
            username.setText("Temporary user");
        } else {
            userEmail.setText(user.getEmail());
            username.setText(user.getDisplayName());
        }

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

        // for event management
        title = findViewById(R.id.etTitle);
        location = findViewById(R.id.etLocation);
        description = findViewById(R.id.etDescription);
        addEvent = findViewById(R.id.addEvent);
        remindTime = findViewById(R.id.etTime);

        final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        final Calendar now = Calendar.getInstance();
        startDate.setText(dateFormat.format(now.getTimeInMillis()));

        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!title.getText().toString().isEmpty() && !location.getText().toString().isEmpty() && !description.getText().toString().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setData(CalendarContract.Events.CONTENT_URI);
                    intent.putExtra(CalendarContract.Events.TITLE, title.getText().toString());
                    intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location.getText().toString());
                    intent.putExtra(CalendarContract.Events.DESCRIPTION, description.getText().toString());
                    intent.putExtra(CalendarContract.Events.HAS_ALARM, 1);
                    intent.putExtra(CalendarContract.Events.ALL_DAY,0);

                    // start date
                    intent.putExtra(CalendarContract.Events.DTSTART, now.getTimeInMillis());

                    // due date
                    intent.putExtra(CalendarContract.Events.DTEND, tempDate);

                    try{
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(EventManager.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // shows date picker dialog
    public void ShowDatePicker (View view) {
        DialogFragment datePicker = new DatePickerFragment();
        datePicker.show(getSupportFragmentManager(), "date picker");
    }

    // when the system received a time
    // @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        Calendar d = Calendar.getInstance();
        d.set(Calendar.YEAR, year);
        d.set(Calendar.MONTH, month);
        d.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        long currentDateString = d.getTimeInMillis();
        // String currentDateString = DateFormat.getDateInstance().format(d.getTime());
        // String currentDateString = d.getTimeInMillis();

        // remindDate = d.toString();
        final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        endDate = findViewById(R.id.etEndDate);
        endDate.setText(dateFormat.format(currentDateString));
        tempDate = currentDateString;
    }

    // formats date
    private String getDate(int dayOfMonth, int month, int year) {
        Date date = new Date(dayOfMonth, month, year);//seconds by default set to zero
        Format formatter;
        formatter = new SimpleDateFormat("yyyy/MM/dd");
        return formatter.format(date);
    }

    // shows time picker dialog
    public void ShowTimePicker(View view) {
        DialogFragment timePicker = new TimePickerFragment();
        timePicker.show(getSupportFragmentManager(), "time picker");
    }

    // sets time chosen by end user to edit text field
    // @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        remindTime = findViewById(R.id.etTime);
        remindTime.setText(getTime(hourOfDay, minute));
    }

    // formats time to x:xx AM/PM
    private String getTime(int hr,int min) {
        // Time tme = new Time(hr,min,0);//seconds by default set to zero
        // Format formatter;
        // formatter = new SimpleDateFormat("h:mm a");
        // return formatter.format(tme);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        Time tme = new Time(hr, min, 0);
        return sdf.format(tme);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()){
            case  R.id.notes:
                startActivity(new Intent(this, MainActivity.class));
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
                recreate();
                break;

            case R.id.ViewCalendar:
                startActivity(new Intent(this, CalendarView.class));
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


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}