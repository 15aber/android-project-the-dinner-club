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
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import dk.tennarasmussen.thedinnerclub.Model.DinnerClub;
import dk.tennarasmussen.thedinnerclub.Model.User;

import static dk.tennarasmussen.thedinnerclub.Constants.BROADCAST_USER_UPDATED;
import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_DINNER_CLUB;
import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_DINNER_CLUBS;
import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_USER;
import static dk.tennarasmussen.thedinnerclub.EmailEncoder.encodeUserEmail;

public class CreateDinnerClubActivity extends AppCompatActivity {


    private String TAG = "CreateDinnerClubActivity";

    //variables
    private String mDCName;
    User curUser;

    //Views
    private Button btnLogOut;
    private Button btnCreateClub;
    private TextView tvLoadingUser;
    private TextView tvLoadingDC;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private FirebaseService mService;
    private boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_dinner_club);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(CreateDinnerClubActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        btnLogOut = findViewById(R.id.btn_logout);
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
            }
        });

        tvLoadingUser = findViewById(R.id.tv_loading_user);
        tvLoadingDC = findViewById(R.id.tv_loading_dc);
        tvLoadingDC.setVisibility(View.GONE);

        btnCreateClub = findViewById(R.id.btn_cdc_create);
        btnCreateClub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDinnerClub();
            }
        });
        btnCreateClub.setVisibility(View.GONE);


    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Bind to LocalService
        Intent intent = new Intent(this.getApplicationContext(), FirebaseService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_USER_UPDATED);
        LocalBroadcastManager.getInstance(this).registerReceiver(onBackgroundServiceResult,filter);
    }

    public void createDinnerClub(){

        //Alert dialog code modified from https://stackoverflow.com/questions/10903754/input-text-dialog-android
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(R.string.create_dc_dialog_title);

        // Set up the input
        final EditText input = new EditText(this);
        input.setTextColor(Color.BLACK);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!input.getText().toString().trim().isEmpty()) {
                    mDCName = input.getText().toString();
                    createDinnerClubInDB(mDCName);
                    btnCreateClub.setVisibility(View.GONE);
                    tvLoadingDC.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(CreateDinnerClubActivity.this, R.string.no_input_error_string, Toast.LENGTH_SHORT).show();
                }

            }
        });

        builder.show();
    }

    private void createDinnerClubInDB(String dcName) {
        //Create new dinner club in firebase db with current user as member
        FirebaseUser curUser = mAuth.getCurrentUser();
        String key = mDatabase.child(FB_DB_USER).child(encodeUserEmail(curUser.getEmail())).child(FB_DB_DINNER_CLUB).push().getKey();
        DinnerClub dinnerClub = new DinnerClub(key, mDCName);
        dinnerClub.members.put(encodeUserEmail(curUser.getEmail()), true);
        Map<String, Object> clubValues = dinnerClub.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + FB_DB_USER + "/" + encodeUserEmail(curUser.getEmail()) + "/" + FB_DB_DINNER_CLUB, key);
        childUpdates.put("/" + FB_DB_DINNER_CLUBS + "/" + key, clubValues);

        mDatabase.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(CreateDinnerClubActivity.this, "Creating Dinner Club Success!!", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Creating dinner club success!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        tvLoadingDC.setVisibility(View.GONE);
                        btnCreateClub.setVisibility(View.VISIBLE);
                        Toast.makeText(CreateDinnerClubActivity.this, R.string.create_failure_string , Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Creating dinner club failure " + e.toString());
                    }
                });
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
                curUser = mService.getCurrentUser();
                //If user has a dinner club, go to home activity
                if(curUser != null && curUser.getDinnerClub() != null) {
                    startActivity(new Intent(CreateDinnerClubActivity.this, DinnerClubHomeActivity.class));
                    finish();
                }
                if(curUser != null && curUser.getDinnerClub() == null) {
                    tvLoadingUser.setVisibility(View.GONE);
                    btnCreateClub.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();

        unbindService(mConnection);
        mBound = false;
    }
}
