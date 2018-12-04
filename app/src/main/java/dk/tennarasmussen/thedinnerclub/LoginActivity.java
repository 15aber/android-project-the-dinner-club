package dk.tennarasmussen.thedinnerclub;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dk.tennarasmussen.thedinnerclub.Model.User;

import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_USER;
import static dk.tennarasmussen.thedinnerclub.Constants.LOGIN_EMAIL;
import static dk.tennarasmussen.thedinnerclub.Constants.LOGIN_PASS;
import static dk.tennarasmussen.thedinnerclub.Constants.REGISTER_REQUEST;

public class LoginActivity extends AppCompatActivity {

    private String TAG = "LoginActivity";

    //Views
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvbtnRegister;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPass);
        btnLogin = findViewById(R.id.btnLoginLogin);
        tvbtnRegister = findViewById(R.id.btnLoginRegister);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null) {
                    //When logged in start service to get newest data
                    Intent firebaseServiceIntent = new Intent(LoginActivity.this.getApplicationContext(), FirebaseService.class);
                    startService(firebaseServiceIntent);

                    //When logged in go to createDinnerClubActivity
                    startActivity(new Intent(LoginActivity.this, CreateDinnerClubActivity.class));

                    finish();
                }
            }
        };

        tvbtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterUserDetails();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignIn();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        if (savedInstanceState != null) {
            etEmail.setText(savedInstanceState.getString(LOGIN_EMAIL));
            etPassword.setText(savedInstanceState.getString(LOGIN_PASS));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    private void RegisterUserDetails() {
        // Modified from: https://developer.android.com/guide/components/activities/intro-activities.html
        Intent intent = new Intent(this, RegisterActivity.class);
        if(!(etEmail.getText().toString().trim().isEmpty()) && android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.getText().toString().trim()).matches()) {
            intent.putExtra(LOGIN_EMAIL, etEmail.getText().toString().trim());
        }
        if(!(etPassword.getText().toString().trim().isEmpty())) {
            intent.putExtra(LOGIN_PASS, etPassword.getText().toString().trim());
        }
        startActivityForResult(intent, REGISTER_REQUEST);
    }

    public void onSignIn() {
        if(validateInput()) {
            final String email = etEmail.getText().toString().trim();
            final String password = etPassword.getText().toString().trim();

            //Modified from Firebase tool method
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.i(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                //updateUI(user);

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.i(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, getText(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                                if (task.getException().getClass() == FirebaseAuthInvalidUserException.class) {
                                    Log.i(TAG, "signInWithEmail: The user does not exist.", task.getException());
                                }
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
        super.onSaveInstanceState(outState);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REGISTER_REQUEST && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult: Register request returned RESULT_OK.");
        } else if (requestCode == REGISTER_REQUEST && resultCode != RESULT_OK){
            Toast.makeText(this, R.string.cancelled_string, Toast.LENGTH_SHORT).show();
        }
    }
}
