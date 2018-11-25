package dk.tennarasmussen.thedinnerclub;

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

public class LoginActivity extends AppCompatActivity {

    private String TAG = "LoginActivity";

    //Values
    private String mEmail, mPassword;

    //Views
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvbtnRegister;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPass);
        btnLogin = findViewById(R.id.btnLoginLogin);
        tvbtnRegister = findViewById(R.id.btnLoginRegister);

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

            //Method from Firebase tool
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.i(TAG, "createUserWithEmail:success");
                                //FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(LoginActivity.this, "Register successful", Toast.LENGTH_SHORT).show();
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

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                //updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
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
}
