package dk.tennarasmussen.thedinnerclub;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import dk.tennarasmussen.thedinnerclub.Model.ClubInvitation;
import dk.tennarasmussen.thedinnerclub.Model.Dinner;
import dk.tennarasmussen.thedinnerclub.Model.DinnerClub;
import dk.tennarasmussen.thedinnerclub.Model.User;

import static dk.tennarasmussen.thedinnerclub.BaseApplication.CHANNEL_ID;
import static dk.tennarasmussen.thedinnerclub.Constants.BROADCAST_DINNERS_UPDATED;
import static dk.tennarasmussen.thedinnerclub.Constants.BROADCAST_LOADED_DC_INVITATION;
import static dk.tennarasmussen.thedinnerclub.Constants.BROADCAST_USER_UPDATED;
import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_CLUB_INVITATION;
import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_CLUB_INVITATIONS;
import static dk.tennarasmussen.thedinnerclub.Constants.FB_DB_DINNERS;
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
    private ClubInvitation curUserDCInv;
    private ArrayList<Dinner> mDinners = new ArrayList<>();

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

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth.addAuthStateListener(mAuthListener);
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
        if (mAuth.getCurrentUser()!=null) {
            new dbLoadCurrentUser().execute();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "OnDestroy. Byebye Service.");
    }

    public void inviteMember(String emailId) {
        String id = encodeUserEmail(emailId);
        new dbIfFriendExistsSendInvitation(id).execute();
    }

    public void createDinner(long timeStamp, String message, String imageURL) {
        new dbCreateDinner(timeStamp, message, imageURL).execute();
    }

    class LocalBinder extends Binder {
        FirebaseService getService() {
            // Return this instance of LocalService so clients can call public methods
            return FirebaseService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance().getReference();
        }
        return mBinder;
    }

    private class dbWriteNewUser extends AsyncTask<Void, Void, Void> {

        private User user;

        public dbWriteNewUser(User user) {
            this.user = user;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mDatabase!= null) {
                Log.i(TAG, "Saving user in firestore database.");
                mDatabase.child(FB_DB_USER).child(encodeUserEmail(user.getEmail())).setValue(user).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //If userdata cannot be saved in realtime db, delete firebase user.
                        Log.i(TAG, "User data couldn't be saved to db, firebase user is deleted.");
                        mAuth.getCurrentUser().delete();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Firebase user is created and user data stored in database successfully.");
                        new dbLoadCurrentUser().execute();
                    }
                });
            } else {
                //If userdata cannot be saved in realtime db, delete firebase user.
                Log.i(TAG, "User data couldn't be saved to db, firebase user is deleted.");
                mAuth.getCurrentUser().delete();
            }
            return null;
        }
    }


    public void writeNewUser(String userId, String name, String streetName, String zipCode, String city, long phone, String email) {
        User user = new User(name, streetName, zipCode, city, phone, email);

        new dbWriteNewUser(user).execute();
    }

    public void acceptDinnerClubInvitation() {
        Log.i(TAG, "Accept dinner club invitation.");
        new dbAcceptAndRemoveDinnerClubInvitation().execute();
    }

    public void rejectDinnerClubInvitation() {
        Log.i(TAG, "Reject dinner club invitation.");
        new dbDeclineRemoveDinnerClubInvitation().execute();
    }

    public boolean userHasDinnerClub() {
        return false;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public ClubInvitation getClubInvitation() { return curUserDCInv; }

    public DinnerClub getCurUserDinnerClub() {
        return curUserDinnerClub;
    }

    public ArrayList<Dinner> getDinners() {
        return mDinners;
    }

    private class dbLoadCurrentUser extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            //Modified from https://firebase.google.com/docs/database/android/read-and-write
            if(mAuth.getCurrentUser()!= null) {
                mDatabase.child(FB_DB_USER).child(encodeUserEmail(mAuth.getCurrentUser().getEmail())).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        currentUser = dataSnapshot.getValue(User.class);
                        if (currentUser!=null) {
                            Log.i(TAG, "Successfully loaded user from db.");
                            Toast.makeText(FirebaseService.this, getText(R.string.loaded_user) + currentUser.getName(), Toast.LENGTH_SHORT).show();

                            new dbLoadDinnerClubOfCurUser().execute();
                            new dbLoadCurUserDCInvitation().execute();

                            Intent broadcastIntent = new Intent();
                            broadcastIntent.setAction(BROADCAST_USER_UPDATED);
                            LocalBroadcastManager.getInstance(FirebaseService.this).sendBroadcast(broadcastIntent);
                        } else {
                            //User doesn't exist in database
                            Log.i(TAG, "dbLoadCurrentUser: User, " + mAuth.getCurrentUser().getEmail() + ", doesn't exist in database.");
                            mAuth.getCurrentUser().delete();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i(TAG, "load User:onCancelled", databaseError.toException());
                    }
                });
            }
            return null;
        }
    }

    private class dbLoadDinnerClubOfCurUser extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (currentUser.getDinnerClub() != null) {
                //Modified from https://firebase.google.com/docs/database/android/read-and-write
                mDatabase.child(FB_DB_DINNER_CLUBS).child(currentUser.getDinnerClub()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        curUserDinnerClub = dataSnapshot.getValue(DinnerClub.class);
                        Log.i(TAG, "Successfully loaded dinner club from db.");
                        new dbLoadDinners().execute();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i(TAG, "load Dinner Club:onCancelled", databaseError.toException());
                    }
                });
            } else {
                Log.i(TAG, "Current user does not have a dinner club.");
            }
            return null;
        }
    }

    private class dbLoadDinners extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (curUserDinnerClub != null) {
                //Modified from https://firebase.google.com/docs/database/android/read-and-write
                mDatabase.child(FB_DB_DINNERS).child(currentUser.getDinnerClub()).orderByChild("dateTime").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot!=null) {
                            Log.i(TAG, "Successfully loaded dinners from db.");
                            ArrayList<Dinner> dinnersTemp = new ArrayList<>();
                            for (DataSnapshot dinnerSnapShot : dataSnapshot.getChildren()) {
                                Log.i(TAG, "Loaded dinner: " + dinnerSnapShot);
                                dinnersTemp.add(dinnerSnapShot.getValue(Dinner.class));
                            }
                            mDinners = dinnersTemp;

                            Intent broadcastIntent = new Intent();
                            broadcastIntent.setAction(BROADCAST_DINNERS_UPDATED);
                            LocalBroadcastManager.getInstance(FirebaseService.this).sendBroadcast(broadcastIntent);

                        } else {
                            Log.i(TAG, "onDataChange: No dinners in dinnerclub");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i(TAG, "load Dinners:onCancelled", databaseError.toException());
                    }
                });
            }
            return null;
        }
    }


    private class dbLoadCurUserDCInvitation extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (currentUser.getDinnerClub() == null && currentUser.getClubInvitation() != null) {
                //Modified from https://firebase.google.com/docs/database/android/read-and-write
                mDatabase.child(FB_DB_CLUB_INVITATIONS).child(currentUser.getClubInvitation()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        curUserDCInv = dataSnapshot.getValue(ClubInvitation.class);
                        Log.i(TAG, "Successfully loaded dinner club invitation from db.");

                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(BROADCAST_LOADED_DC_INVITATION);
                        LocalBroadcastManager.getInstance(FirebaseService.this).sendBroadcast(broadcastIntent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i(TAG, "load Dinner Club invitation:onCancelled", databaseError.toException());
                    }
                });
            } else {
                Log.i(TAG, "Current user does not have a dinner club invitation.");
            }
            return null;
        }
    }


    private class dbIfFriendExistsSendInvitation extends AsyncTask<Void, Void, Void> {

        private String emailId;

        public dbIfFriendExistsSendInvitation(String emailId) {
            this.emailId = emailId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
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
                                new dbSendMemberInvitation(friend).execute();

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
            return null;
        }
    }


    private class dbSendMemberInvitation extends AsyncTask<Void, Void, Void> {

        private User friend;

        public dbSendMemberInvitation(User friend) {
            this.friend = friend;
        }

        @Override
        protected Void doInBackground(Void... voids) {
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
            return null;
        }
    }


    private class dbDeclineRemoveDinnerClubInvitation extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if(currentUser!=null && curUserDCInv!=null) {
                //delete dinner club invitation and all references
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/" + FB_DB_USER + "/" + encodeUserEmail(currentUser.getEmail()) + "/" + FB_DB_CLUB_INVITATION, null);
                childUpdates.put("/" + FB_DB_CLUB_INVITATIONS + "/" + currentUser.getClubInvitation(), null);
                childUpdates.put("/" + FB_DB_DINNER_CLUBS + "/" + curUserDCInv.dinnerClubId + "/" + "members" + "/" + curUserDCInv.recipientId, null);

                mDatabase.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Deleting dinner club invitation success!");
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(FirebaseService.this, getText(R.string.invitation_not_deleted_string).toString() , Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "Deleting dinner club invitation failure " + e.toString());
                            }
                        });
            }
            return null;
        }
    }

    private class dbAcceptAndRemoveDinnerClubInvitation extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if(currentUser!=null && curUserDCInv!=null) {
                //Add user to dinner club and then delete dinner club invitation and all references
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/" + FB_DB_USER + "/" + encodeUserEmail(currentUser.getEmail()) + "/" + FB_DB_DINNER_CLUB, curUserDCInv.dinnerClubId);
                childUpdates.put("/" + FB_DB_DINNER_CLUBS + "/" + curUserDCInv.dinnerClubId + "/" + "members" + "/" + curUserDCInv.recipientId, true);
                childUpdates.put("/" + FB_DB_USER + "/" + encodeUserEmail(currentUser.getEmail()) + "/" + FB_DB_CLUB_INVITATION, null);
                childUpdates.put("/" + FB_DB_CLUB_INVITATIONS + "/" + currentUser.getClubInvitation(), null);

                mDatabase.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Accepting dinner club invitation success!");
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(FirebaseService.this, getText(R.string.invitation_not_deleted_string).toString() , Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "Deleting dinner club invitation failure " + e.toString());
                            }
                        });
            }
            return null;
        }
    }

    private class dbCreateDinner extends AsyncTask<Void, Void, Void> {

        private long timeStamp;
        private String message;
        private String imageURL;

        public dbCreateDinner(long timeStamp, String message, String imageURL) {
            this.timeStamp = timeStamp;
            this.message = message;
            this.imageURL = imageURL;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if(currentUser!=null && curUserDinnerClub!=null) {
                String key = mDatabase.child(FB_DB_DINNERS).child(currentUser.getDinnerClub()).push().getKey();

                //Add user to dinner club and then delete dinner club invitation and all references
                Dinner dinner = new Dinner(timeStamp, currentUser, message, imageURL);
                Map<String, Boolean> guests = curUserDinnerClub.members;
                guests.remove(encodeUserEmail(currentUser.getEmail()));
                dinner.guests = guests;
                Map<String, Object> dinnerValues = dinner.toMap();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/" + FB_DB_DINNERS + "/" + currentUser.getDinnerClub() + "/" + key, dinnerValues);

                mDatabase.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Creating dinner success!");
                        Toast.makeText(FirebaseService.this, getText(R.string.dinner_created_string).toString() , Toast.LENGTH_SHORT).show();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(FirebaseService.this, getText(R.string.dinner_not_created_string).toString() , Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "Creating dinner failure " + e.toString());
                            }
                        });
            }
            return null;
        }
    }
}
