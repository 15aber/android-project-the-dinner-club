package dk.tennarasmussen.thedinnerclub;

import android.app.Dialog;
import android.content.Intent;
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

public class CreateDinnerActivity extends AppCompatActivity {

    private static final String TAG = "CreateDinnerActivity";

    //Views
    CustomDateTimePicker custom;
    EditText etDateTime;
    ImageButton btn_calendar;
    Button btnCancel;
    Button btnCreate;
    EditText etMessage;

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
    }

    private void createDinner() {
        if (!validateInput()) {
            return;
        }
        Timestamp timeStamp;

        //Convert to timestamp as https://qr.ae/TUtIwJ
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            Date parsedDate = dateFormat.parse(etDateTime.getText().toString().trim());
            timeStamp = new java.sql.Timestamp(parsedDate.getTime());
        } catch(Exception e) { //this generic but you can control nother types of exception
            Log.i(TAG, "Couldn't convert date and time string to Timestamp" + e.toString());
            timeStamp = null;
        }
        if(timeStamp != null) {
            Toast.makeText(this, "Input Validated, Dinner can be created", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean validateInput() {
        boolean valid = true;
        Timestamp timeStamp;

        //Convert to timestamp as https://qr.ae/TUtIwJ
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            Date parsedDate = dateFormat.parse(etDateTime.getText().toString().trim());
            timeStamp = new java.sql.Timestamp(parsedDate.getTime());
        } catch(Exception e) { //this generic but you can control nother types of exception
            Log.i(TAG, "Couldn't convert date and time string to Timestamp" + e.toString());
            timeStamp = null;
        }
        //If date input is null or empty
        if (etDateTime.getText().toString().trim().isEmpty()) {
            etDateTime.setError(getString(R.string.date_time_hint) + " " + getString(R.string.required_string));
            valid = false;
        } else if(timeStamp == null) {  //if email input is not a valid email
            etDateTime.setError(getString(R.string.date_time_hint) + " " + getString(R.string.not_valid_string));
            valid = false;
        }
        if (etMessage.getText().toString().trim().isEmpty()) {
            etMessage.setError(getString(R.string.message_hint) + " " + getString(R.string.required_string));
            valid = false;
        }
        return valid;
    }
}
