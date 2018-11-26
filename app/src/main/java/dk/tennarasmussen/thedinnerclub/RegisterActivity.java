package dk.tennarasmussen.thedinnerclub;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dk.tennarasmussen.thedinnerclub.Model.User;

import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_USER;
import static dk.tennarasmussen.thedinnerclub.Constants.LOGIN_EMAIL;
import static dk.tennarasmussen.thedinnerclub.Constants.LOGIN_PASS;
import static dk.tennarasmussen.thedinnerclub.Constants.REGISTER_CITY;
import static dk.tennarasmussen.thedinnerclub.Constants.REGISTER_NAME;
import static dk.tennarasmussen.thedinnerclub.Constants.REGISTER_PHONE;
import static dk.tennarasmussen.thedinnerclub.Constants.REGISTER_STREET;
import static dk.tennarasmussen.thedinnerclub.Constants.REGISTER_ZIP;

public class RegisterActivity extends AppCompatActivity {

    private String TAG = "RegisterActivity";

    private EditText etEmail;
    private EditText etPassword;
    private EditText etName;
    private EditText etPhone;
    private EditText etStreet;
    private EditText etZip;
    private EditText etCity;
    private Button btnRegister;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseService mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPass);
        etName = findViewById(R.id.etRegisterName);
        etPhone = findViewById(R.id.etRegisterPhone);
        etStreet = findViewById(R.id.etRegisterStreet);
        etZip = findViewById(R.id.etRegisterZip);
        etCity = findViewById(R.id.etRegisterCity);
        btnRegister = findViewById(R.id.btnRegisterRegister);

        if (savedInstanceState != null) {
            etEmail.setText(savedInstanceState.getString(LOGIN_EMAIL));
            etPassword.setText(savedInstanceState.getString(LOGIN_PASS));
            etName.setText(savedInstanceState.getString(REGISTER_NAME));
            etPhone.setText(savedInstanceState.getString(REGISTER_PHONE));
            etStreet.setText(savedInstanceState.getString(REGISTER_STREET));
            etZip.setText(savedInstanceState.getString(REGISTER_ZIP));
            etCity.setText(savedInstanceState.getString(REGISTER_CITY));

        } else {
            final Intent data = getIntent();
            etEmail.setText(data.getStringExtra(LOGIN_EMAIL));
            etPassword.setText(data.getStringExtra(LOGIN_EMAIL));
        }

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRegister();
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null) {
                    //When logged in start service to get newest data
                    Intent firebaseServiceIntent = new Intent(RegisterActivity.this.getApplicationContext(), FirebaseService.class);
                    startService(firebaseServiceIntent);

                    //When logged in goto createDinnerClubActivity
                    startActivity(new Intent(RegisterActivity.this, CreateDinnerClubActivity.class));
                }
            }
        };

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

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

    //Register new user
    public void onRegister() {
        if(validateInput()) {
            final String email = etEmail.getText().toString().trim();
            final String password = etPassword.getText().toString().trim();

            //Modified from Firebase tool method
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.i(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(RegisterActivity.this, "Register successful", Toast.LENGTH_SHORT).show();
                                if(mBound){
                                    mService.writeNewUser(user.getUid(),
                                            etName.getText().toString(),
                                            etStreet.getText().toString(),
                                            etZip.getText().toString(),
                                            etCity.getText().toString(),
                                            Long.parseLong(etPhone.getText().toString()),
                                            user.getEmail());
                                } else {
                                    Log.i(TAG, "Couldn't add user to database, since not bound to service.");
                                }

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.i(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }

                            // ...
                        }
                    });
        }
    }

    public boolean validateInput() {
        boolean valid = true;
        //If email input is null or empty
        if (etEmail.getText().toString().trim().isEmpty()) {
            etEmail.setError(getString(R.string.email_hint) + " " + getString(R.string.required_string));
            valid = false;
        } else if(!(android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches())) {  //if email input is not a valid email
            etEmail.setError(getString(R.string.email_hint) + " " + getString(R.string.not_valid_string));
            valid = false;
        }
        if (etPassword.getText().toString().trim().isEmpty()) {
            etPassword.setError(getString(R.string.password_hint) + " " + getString(R.string.required_string));
            valid = false;
        }
        return valid;
    }

    // Modified from: https://developer.android.com/guide/components/activities/activity-lifecycle.html
    //Save inputs for configuration changes
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(LOGIN_EMAIL, etEmail.getText().toString());
        outState.putString(LOGIN_PASS, etPassword.getText().toString());
        outState.putString(REGISTER_NAME, etName.getText().toString());
        outState.putString(REGISTER_PHONE, etPhone.getText().toString());
        outState.putString(REGISTER_STREET, etStreet.getText().toString());
        outState.putString(REGISTER_ZIP, etZip.getText().toString());
        outState.putString(REGISTER_CITY, etCity.getText().toString());
        super.onSaveInstanceState(outState);
    }
}
