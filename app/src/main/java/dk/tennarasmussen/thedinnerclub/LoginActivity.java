package dk.tennarasmussen.thedinnerclub;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    String Log = "LoginActivity";

    //Views
    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvbtnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPass);
        btnLogin = findViewById(R.id.btnLoginLogin);
        tvbtnRegister = findViewById(R.id.btnLoginRegister);


    }
}
