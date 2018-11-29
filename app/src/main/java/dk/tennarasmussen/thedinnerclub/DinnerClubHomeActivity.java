package dk.tennarasmussen.thedinnerclub;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import static dk.tennarasmussen.thedinnerclub.EmailEncoder.encodeUserEmail;

public class DinnerClubHomeActivity extends AppCompatActivity {

    private static final String TAG = "DinnerClubHomeActivity";

    private Button btnLogout;
    private Button btnAddMember;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseService mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dinner_club_home);

        mAuth = FirebaseAuth.getInstance();

        btnLogout = findViewById(R.id.btnHomeLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
            }
        });

        btnAddMember = findViewById(R.id.btnHomeAddMember);
        btnAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inviteMember();
            }
        });



        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(DinnerClubHomeActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to LocalService
        Intent intent = new Intent(this, FirebaseService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBound = false;
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
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
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
}
