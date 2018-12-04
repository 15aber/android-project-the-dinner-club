package dk.tennarasmussen.thedinnerclub;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;

import dk.tennarasmussen.thedinnerclub.Model.Dinner;
import dk.tennarasmussen.thedinnerclub.Model.DinnerClub;
import dk.tennarasmussen.thedinnerclub.Model.User;

import static dk.tennarasmussen.thedinnerclub.Constants.BROADCAST_DINNERS_UPDATED;
import static dk.tennarasmussen.thedinnerclub.Constants.NEW_DINNER_REQUEST;
import static dk.tennarasmussen.thedinnerclub.EmailEncoder.encodeUserEmail;

public class DinnerClubHomeActivity extends AppCompatActivity {

    private static final String TAG = "DinnerClubHomeActivity";

    //Variables
    private User mUser;
    private DinnerClub mDinnerClub;
    private ArrayList<Dinner> mDinners = new ArrayList<>();
    private DinnersAdapter adapter;

    //Views
    private Button btnLogout;
    private Button btnAddMember;
    private Button btnCreateDinner;
    private TextView tvClubName;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseService mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dinner_club_home);
        Log.i(TAG, "onCreate: Started");

        mDinners.add(new Dinner(System.currentTimeMillis(), "Maja", "Let's eat!!"));
        mDinners.add(new Dinner(System.currentTimeMillis(), "Nanna", "Let's eat some more!!"));
        mDinners.add(new Dinner(System.currentTimeMillis(), "Simone", "Let's eat!!"));
        mDinners.add(new Dinner(System.currentTimeMillis(), "Andreas", "Let's eat!!"));
        mDinners.add(new Dinner(System.currentTimeMillis(), "Tenna", "Let's eat!!"));
        mDinners.add(new Dinner(System.currentTimeMillis(), "Josefine", "Let's eat!!"));
        mDinners.add(new Dinner(System.currentTimeMillis(), "Maja", "Let's eat some more!!!!"));
        mDinners.add(new Dinner(System.currentTimeMillis(), "Nanna", "Let's eat some more!!!!"));
        mDinners.add(new Dinner(System.currentTimeMillis(), "Simone", "Let's eat some more!!!!"));
        mDinners.add(new Dinner(System.currentTimeMillis(), "Andreas", "Let's eat some more!!!!"));
        mDinners.add(new Dinner(System.currentTimeMillis(), "Tenna", "Let's eat some more!!!!"));
        mDinners.add(new Dinner(System.currentTimeMillis(), "Josefine", "Let's eat some more!!!!"));

        initRecyclerView();

        mAuth = FirebaseAuth.getInstance();

        btnLogout = findViewById(R.id.btnHomeLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
            }
        });

        btnCreateDinner = findViewById(R.id.btnHomeCreateDinner);
        btnCreateDinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDinner();
            }
        });

        btnAddMember = findViewById(R.id.btnHomeAddMember);
        btnAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inviteMember();
            }
        });

        tvClubName = findViewById(R.id.tvHomeClubName);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(DinnerClubHomeActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        // Bind to LocalService
        Intent intent = new Intent(this, FirebaseService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_DINNERS_UPDATED);
        LocalBroadcastManager.getInstance(this).registerReceiver(onBackgroundServiceResult,filter);
    }

    private void initRecyclerView() {
        Log.i(TAG, "initRecyclerView: init recyclerview");
        RecyclerView recyclerView = findViewById(R.id.homeRecyclerView);
        adapter = new DinnersAdapter(mDinners, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void createDinner() {
        // Modified from: https://developer.android.com/guide/components/activities/intro-activities.html
        Intent intent = new Intent(this, CreateDinnerActivity.class);

        startActivityForResult(intent, NEW_DINNER_REQUEST);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        mBound = false;
        LocalBroadcastManager.getInstance(DinnerClubHomeActivity.this).unregisterReceiver(onBackgroundServiceResult);
    }


//From https://developer.android.com/guide/components/bound-services
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            FirebaseService.LocalBinder binder = (FirebaseService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            mDinnerClub = mService.getCurUserDinnerClub();
            if (mDinnerClub!=null) {
                tvClubName.setText(mDinnerClub.clubName);
            }

            mUser = mService.getCurrentUser();
            mDinnerClub = mService.getCurUserDinnerClub();
            mDinners = mService.getDinners();
            if (mDinners != null) {
                adapter.setmDinnerList(mDinners);
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    //define broadcast receiver for (local) broadcasts.
    private BroadcastReceiver onBackgroundServiceResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Broadcast received from service");
            if(mBound) {
                if(intent.getAction().equals(BROADCAST_DINNERS_UPDATED)) {
                    mUser = mService.getCurrentUser();
                    mDinnerClub = mService.getCurUserDinnerClub();
                    mDinners = mService.getDinners();
                    adapter.setmDinnerList(mDinners);
                }

            }
        }
    };

    private void inviteMember() {
        //Alert dialog code modified from https://stackoverflow.com/questions/10903754/input-text-dialog-android
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(R.string.invite_member_title);

        // Set up the input
        final EditText input = new EditText(this);
        input.setTextColor(Color.BLACK);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.invite, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!input.getText().toString().trim().isEmpty()) {
                    if(android.util.Patterns.EMAIL_ADDRESS.matcher(input.getText().toString()).matches()) {
                        Log.i(TAG, "Add member: Email is valid.");
                        createInvitationInDB(input.getText().toString());
                    } else {
                        Log.i(TAG, "Add member: Email is NOT valid.");
                        Toast.makeText(DinnerClubHomeActivity.this, R.string.not_valid_email_string, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.i(TAG, "Add member: No email was provided.");
                    Toast.makeText(DinnerClubHomeActivity.this, R.string.no_input_email_string, Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.show();
    }

    private void createInvitationInDB(String email) {
        String emailId = encodeUserEmail(email);
        if (mBound) {
            mService.inviteMember(emailId);
        } else {
            Log.i(TAG, "CreateInvitationInDB: Error. Not bound to service.");
            Toast.makeText(DinnerClubHomeActivity.this, R.string.connection_error_string, Toast.LENGTH_SHORT).show();
        }
    }



    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_DINNER_REQUEST && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult: New dinner request returned RESULT_OK.");
        } else if (requestCode == NEW_DINNER_REQUEST && resultCode != RESULT_OK){
            Toast.makeText(this, R.string.cancelled_string, Toast.LENGTH_SHORT).show();
        }
    }
}
