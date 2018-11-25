package dk.tennarasmussen.thedinnerclub;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import dk.tennarasmussen.thedinnerclub.Model.User;

import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_USER;

public class LoginActivity extends AppCompatActivity {

    private String TAG = "LoginActivity";

    //Values
    private String mEmail, mPassword;

    //Views
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvbtnRegister;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

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
                    startActivity(new Intent(LoginActivity.this, CreateDinnerClubActivity.class));
                }
            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvbtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRegister();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignIn();
            }
        });

        mAuth = FirebaseAuth.getInstance();


    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
        Toast.makeText(this, "Already In", Toast.LENGTH_SHORT).show();
    }

    //Register new user
    public void onRegister() {
        if(validateInput()) {
            final String email = etEmail.getText().toString().trim();
            final String password = etPassword.getText().toString().trim();

            //Modified from Firebase tool method
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.i(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(LoginActivity.this, "Register successful", Toast.LENGTH_SHORT).show();
                                writeNewUser(user.getUid(), "Tenna Rasmussen", "Abevej 64", "8000", "Zootopia", 12345678, user.getEmail());
                                //updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.i(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }

                            // ...
                        }
                    });
        }
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
                                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                //updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.i(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }

                            // ...
                        }
                    });
        }
    }

    private void writeNewUser(String userId, String name, String streetName, String zipCode, String city, long phone, String email) {
        User user = new User(name, streetName, zipCode, city, phone, email);

        mDatabase.child(FB_DB_USER).child(userId).setValue(user);
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
}
