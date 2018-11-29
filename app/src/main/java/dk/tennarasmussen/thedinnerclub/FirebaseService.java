package dk.tennarasmussen.thedinnerclub;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import dk.tennarasmussen.thedinnerclub.Model.ClubInvitation;
import dk.tennarasmussen.thedinnerclub.Model.DinnerClub;
import dk.tennarasmussen.thedinnerclub.Model.User;

import static dk.tennarasmussen.thedinnerclub.BaseApplication.CHANNEL_ID;
import static dk.tennarasmussen.thedinnerclub.Constants.BROADCAST_USER_UPDATED;
import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_CLUB_INVITATION;
import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_CLUB_INVITATIONS;
import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_DINNER_CLUB;
import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_DINNER_CLUBS;
import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_USER;
import static dk.tennarasmussen.thedinnerclub.Constants.LOGIN_REQUEST;
import static dk.tennarasmussen.thedinnerclub.Constants.NOTIFY_ID;
import static dk.tennarasmussen.thedinnerclub.EmailEncoder.decodeUserEmail;
import static dk.tennarasmussen.thedinnerclub.EmailEncoder.encodeUserEmail;

public class FirebaseService extends Service {
    public FirebaseService() {
    }

    private String TAG = "FirebaseService";

    private User currentUser;
    private DinnerClub curUserDinnerClub;

    private final IBinder mBinder = new LocalBinder();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "FirebaseService onCreate()");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null) {
                    Log.i(TAG, "Current user is: " + firebaseAuth.getCurrentUser().getEmail());
                    //Toast.makeText(FirebaseService.this, "Current user is: " + firebaseAuth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.i(TAG, "User logged out. Service will stop self.");
                    stopSelf();
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "FirebaseService onStartCommand()");

        //Notification code modified from https://www.youtube.com/watch?v=FbpD5RZtbCc
        Intent notificationIntent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, LOGIN_REQUEST, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_text))
                .setSmallIcon(R.drawable.ic_people_outline_black_24dp)
                .setTicker(getText(R.string.notification_text))
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFY_ID, notification);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth.addAuthStateListener(mAuthListener);
        dbLoadCurrentUser();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "OnDestroy. Byebye Service.");
    }

    public void inviteMember(String emailId) {
        String id = encodeUserEmail(emailId);
        dbIfFriendExistsSendInvitation(id);
    }

    class LocalBinder extends Binder {
        FirebaseService getService() {
            // Return this instance of LocalService so clients can call public methods
            return FirebaseService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void writeNewUser(String userId, String name, String streetName, String zipCode, String city, long phone, String email) {
        User user = new User(name, streetName, zipCode, city, phone, email);

        Log.i(TAG, "Saving user in firestore database.");
        mDatabase.child(FB_DB_USER).child(encodeUserEmail(email)).setValue(user);
    }

    public boolean userHasDinnerClub() {
        return false;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private void dbLoadCurrentUser() {
        //Modified from https://firebase.google.com/docs/database/android/read-and-write
        mDatabase.child(FB_DB_USER).child(encodeUserEmail(mAuth.getCurrentUser().getEmail())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                Log.i(TAG, "Successfully loaded user from db.");
                Toast.makeText(FirebaseService.this, "Loaded user: " + currentUser.getName(), Toast.LENGTH_SHORT).show();

                dbLoadDinnerClubOfCurUser();

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(BROADCAST_USER_UPDATED);
                LocalBroadcastManager.getInstance(FirebaseService.this).sendBroadcast(broadcastIntent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "load User:onCancelled", databaseError.toException());
            }
        });
    }

    private void dbLoadDinnerClubOfCurUser() {
        if (currentUser.getDinnerClub() != null) {
            //Modified from https://firebase.google.com/docs/database/android/read-and-write
            mDatabase.child(FB_DB_DINNER_CLUBS).child(currentUser.getDinnerClub()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    curUserDinnerClub = dataSnapshot.getValue(DinnerClub.class);
                    Log.i(TAG, "Successfully loaded dinner club from db.");
                    Toast.makeText(FirebaseService.this, "Loaded dinner club: " + curUserDinnerClub.clubName, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.i(TAG, "load Dinner Club:onCancelled", databaseError.toException());
                }
            });
        } else {
            Log.i(TAG, "Current user does not have a dinner club.");
        }
    }

    private void dbIfFriendExistsSendInvitation(final String emailId) {
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(decodeUserEmail(emailId)).matches()) {
            //Modified from https://firebase.google.com/docs/database/android/read-and-write
            final String friendId = encodeUserEmail(emailId);
            mDatabase.child(FB_DB_USER).child(friendId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //If the user exists
                    if(dataSnapshot.exists()) {
                        User friend = dataSnapshot.getValue(User.class);
                        Log.i(TAG, "Successfully loaded friend " + friend.getName() + " from db.");
                        if(friend.getDinnerClub() == null || friend.getDinnerClub().isEmpty()) {
                            Log.i(TAG, friend.getName() + " has no dinnerclub. Sending invitation.");
                            dbSendMemberInvitation(friend);

                        } else {
                            Log.i(TAG, friend.getName() + " already has a dinnerclub. No invitation sent.");
                            Toast.makeText(FirebaseService.this, friend.getName() + getText(R.string.has_club_inv_not_sent_string).toString(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.i(TAG, decodeUserEmail(emailId) + " is not a Dinner Club user. No invitation sent.");
                        Toast.makeText(FirebaseService.this, decodeUserEmail(emailId) + getText(R.string.user_not_exist).toString(), Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.i(TAG, "load friend:onCancelled", databaseError.toException());
                }
            });
        } else {
            Log.i(TAG, "The email is not a valid email.");
        }
    }

    private void dbSendMemberInvitation(final User friend) {
        //Create new dinner club invitation in firebase db
        if(currentUser!=null && curUserDinnerClub!=null) {
            String key = mDatabase.child(FB_DB_USER).child(encodeUserEmail(friend.getEmail())).child(FB_DB_CLUB_INVITATION).push().getKey();

            ClubInvitation clubInvitation = new ClubInvitation(
                    encodeUserEmail(friend.getEmail()),
                    encodeUserEmail(currentUser.getEmail()),
                    curUserDinnerClub.clubId,
                    currentUser.getName(),
                    curUserDinnerClub.clubName);

            curUserDinnerClub.members.put(encodeUserEmail(friend.getEmail()), false);

            Map<String, Object> clubValues = curUserDinnerClub.toMap();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/" + FB_DB_USER + "/" + encodeUserEmail(friend.getEmail()) + "/" + FB_DB_CLUB_INVITATION, key);
            childUpdates.put("/" + FB_DB_CLUB_INVITATIONS + "/" + key, clubInvitation);
            childUpdates.put("/" + FB_DB_DINNER_CLUBS + "/" + curUserDinnerClub.clubId, clubValues);

            mDatabase.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(FirebaseService.this, getText(R.string.invitation_sent_string).toString() + friend.getName(), Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Sending dinner club invitation success!");
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(FirebaseService.this, getText(R.string.invitation_not_sent_string).toString() , Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "Creating dinner club invitation failure " + e.toString());
                        }
                    });
        }

    }
}
