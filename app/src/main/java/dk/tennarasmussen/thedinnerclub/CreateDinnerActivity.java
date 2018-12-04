package dk.tennarasmussen.thedinnerclub;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import dk.tennarasmussen.thedinnerclub.Model.DinnerClub;
import dk.tennarasmussen.thedinnerclub.Model.User;

import static dk.tennarasmussen.thedinnerclub.Constants.DINNER_DATETIME;
import static dk.tennarasmussen.thedinnerclub.Constants.DINNER_MESSAGE;
import static dk.tennarasmussen.thedinnerclub.Constants.LOGIN_EMAIL;
import static dk.tennarasmussen.thedinnerclub.Constants.LOGIN_PASS;
import static dk.tennarasmussen.thedinnerclub.Constants.REGISTER_CITY;
import static dk.tennarasmussen.thedinnerclub.Constants.REGISTER_NAME;
import static dk.tennarasmussen.thedinnerclub.Constants.REGISTER_PHONE;
import static dk.tennarasmussen.thedinnerclub.Constants.REGISTER_STREET;
import static dk.tennarasmussen.thedinnerclub.Constants.REGISTER_ZIP;

public class CreateDinnerActivity extends AppCompatActivity {

    private static final String TAG = "CreateDinnerActivity";

    //Variables
    User curUser;
    DinnerClub mDinnerClub;

    //Views
    CustomDateTimePicker custom;
    EditText etDateTime;
    ImageButton btn_calendar;
    Button btnCancel;
    Button btnCreate;
    EditText etMessage;

    FirebaseService mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_dinner);

        etDateTime = (findViewById(R.id.etCDDateTimePicker));
        btn_calendar = findViewById(R.id.ivCalendar);

        //Datetime picker from https://stackoverflow.com/a/38604615
        custom = new CustomDateTimePicker(this,
                new CustomDateTimePicker.ICustomDateTimeListener() {

                    @Override
                    public void onSet(Dialog dialog, Calendar calendarSelected,
                                      Date dateSelected, int year, String monthFullName,
                                      String monthShortName, int monthNumber, int date,
                                      String weekDayFullName, String weekDayShortName,
                                      int hour24, int hour12, int min, int sec,
                                      String AM_PM) {
                        //                        ((TextInputEditText) findViewById(R.id.edtEventDateTime))
                        etDateTime.setText("");
                        etDateTime.setText(year
                                + "-" + (monthNumber + 1) + "-" + calendarSelected.get(Calendar.DAY_OF_MONTH)
                                + " " + hour24 + ":" + min);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
        /**
         * Pass Directly current time format it will return AM and PM if you set
         * false
         */
        custom.set24HourFormat(true);
        /**
         * Pass Directly current data and time to show when it pop up
         */
        custom.setDate(Calendar.getInstance());
        etDateTime.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        custom.showDialog();
                    }
                });
        btn_calendar.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        custom.showDialog();
                    }
                });

        btnCancel = findViewById(R.id.btnCDCancel);
        btnCancel.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent replyIntent = new Intent();
                        setResult(RESULT_CANCELED, replyIntent);
                        finish();
                    }
                }
        );

        btnCreate = findViewById(R.id.btnCDCreate);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDinner();
            }
        });

        etMessage = findViewById(R.id.etCDMessage);

        if (savedInstanceState != null) {
            etDateTime.setText(savedInstanceState.getString(DINNER_DATETIME));
            etMessage.setText(savedInstanceState.getString(DINNER_MESSAGE));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to LocalService
        Intent intent = new Intent(this.getApplicationContext(), FirebaseService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
    }

    //Modified from https://developer.android.com/guide/components/bound-services#java
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            FirebaseService.LocalBinder binder = (FirebaseService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            curUser = mService.getCurrentUser();
            mDinnerClub = mService.getCurUserDinnerClub();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void createDinner() {
        if (!validateInput()) {
            return;
        }
        long timeStamp;

        //Convert to timestamp as https://qr.ae/TUtIwJ
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            Date parsedDate = dateFormat.parse(etDateTime.getText().toString().trim());
            timeStamp = parsedDate.getTime();
        } catch(Exception e) { //this generic but you can control nother types of exception
            Log.i(TAG, "Couldn't convert date and time string to Timestamp" + e.toString());
            timeStamp = 0;
        }
        if(timeStamp != 0) {
            Toast.makeText(this, "Input Validated, Dinner can be created", Toast.LENGTH_SHORT).show();

            if(mBound) {
                mService.createDinnner(timeStamp, etMessage.getText().toString().trim());
                Intent replyIntent = new Intent();
                setResult(RESULT_OK, replyIntent);
                finish();
            } else {
                Log.i(TAG, "Error, Dinner not created, not bound to Service.");
                Toast.makeText(this, R.string.create_error_string, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean validateInput() {
        boolean valid = true;
        long timeStamp;

        //Convert to timestamp as https://qr.ae/TUtIwJ
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            Date parsedDate = dateFormat.parse(etDateTime.getText().toString().trim());
            timeStamp = parsedDate.getTime();
        } catch(Exception e) { //this generic but you can control nother types of exception
            Log.i(TAG, "Couldn't convert date and time string to Timestamp" + e.toString());
            timeStamp = 0;
        }
        //If date input is null or empty
        if (etDateTime.getText().toString().trim().isEmpty()) {
            etDateTime.setError(getString(R.string.date_time_hint) + " " + getString(R.string.required_string));
            valid = false;
        } else if(timeStamp == 0) {  //if email input is not a valid email
            etDateTime.setError(getString(R.string.date_time_hint) + " " + getString(R.string.not_valid_string));
            valid = false;
        }
        if (etMessage.getText().toString().trim().isEmpty()) {
            etMessage.setError(getString(R.string.message_hint) + " " + getString(R.string.required_string));
            valid = false;
        }
        return valid;
    }

    // Modified from: https://developer.android.com/guide/components/activities/activity-lifecycle.html
    //Save inputs for configuration changes
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(DINNER_DATETIME, etDateTime.getText().toString());
        outState.putString(DINNER_MESSAGE, etMessage.getText().toString());
        super.onSaveInstanceState(outState);
    }
}
