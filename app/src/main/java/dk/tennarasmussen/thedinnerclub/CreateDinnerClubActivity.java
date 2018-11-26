package dk.tennarasmussen.thedinnerclub;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_DINNER_CLUB;
import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_DINNER_CLUBS;
import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_USER;

public class CreateDinnerClubActivity extends AppCompatActivity {


    //Views
    private Button btnLogOut;
    private Button btnCreateClub;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDatabase;

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

        btnCreateClub = findViewById(R.id.btn_cdc_create);
        btnCreateClub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDinnerClub();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void createDinnerClub(){
        FirebaseUser curUser = mAuth.getCurrentUser();
        String key = mDatabase.child(FB_DB_USER).child(curUser.getUid()).child(FB_DB_DINNER_CLUB).push().getKey();
        DinnerClub dinnerClub = new DinnerClub(key, "Dyrene i Zoo");
        dinnerClub.members.put(curUser.getUid(), true);
        Map<String, Object> clubValues = dinnerClub.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + FB_DB_USER + "/" + curUser.getUid() + "/" + FB_DB_DINNER_CLUB, key);
        childUpdates.put("/" + FB_DB_DINNER_CLUBS + "/" + key, clubValues);

        mDatabase.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(CreateDinnerClubActivity.this, "Creating Dinner Club Success!!", Toast.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CreateDinnerClubActivity.this, "Creating Dinner Club Failure", Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
