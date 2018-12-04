package dk.tennarasmussen.thedinnerclub;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import dk.tennarasmussen.thedinnerclub.Model.Dinner;

import static dk.tennarasmussen.thedinnerclub.Constants.BROADCAST_DINNERS_UPDATED;
import static dk.tennarasmussen.thedinnerclub.Constants.DINNER_LIST_POSITION;

public class DinnerDetailsActivity extends AppCompatActivity {

    private static final String TAG = "DinnerDetailsActivity";

    int position;
    ArrayList<Dinner> mDinners = new ArrayList<>();
    Dinner mDinner;

    FirebaseService mService;
    boolean mBound = false;

    //Views
    ImageView imImage;
    TextView tvTitle;
    TextView tvHost;
    TextView tvDate;
    TextView tvAddress;
    TextView tvPhone;
    TextView tvEmail;
    TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dinner_details);

        Log.i(TAG, "onCreate: started");

        if(getIntent().hasExtra(DINNER_LIST_POSITION)) {
            position = getIntent().getIntExtra(DINNER_LIST_POSITION, 0);
        } else {
            finish();
        }

        // Bind to LocalService
        Intent intent = new Intent(this, FirebaseService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_DINNERS_UPDATED);
        LocalBroadcastManager.getInstance(this).registerReceiver(onBackgroundServiceResult,filter);

        //Find views
        imImage = findViewById(R.id.ivDinnerDetailsImage);
        tvTitle = findViewById(R.id.tvDinnerDetailsTitle);
        tvHost = findViewById(R.id.tvDinnerDetailsHost);
        tvDate = findViewById(R.id.tvDinnerDetailsDate);
        tvAddress = findViewById(R.id.tvDinnerDetailsAddress);
        tvPhone = findViewById(R.id.tvDinnerDetailsPhone);
        tvEmail = findViewById(R.id.tvDinnerDetailsEmail);
        tvMessage = findViewById(R.id.tvDinnerDetailsMessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        mBound = false;
        LocalBroadcastManager.getInstance(DinnerDetailsActivity.this).unregisterReceiver(onBackgroundServiceResult);
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

            mDinners = mService.getDinners();
            if (mDinners!=null) {
                mDinner = mDinners.get(position);
                setValues();
            }
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
                if(intent.getAction().equals(BROADCAST_DINNERS_UPDATED)) {

                    mDinners = mService.getDinners();
                    if (mDinners!=null) {
                        mDinner = mDinners.get(position);
                        setValues();
                    }
                }
            }
        }
    };

    private void setValues() {
        Log.i(TAG, "setValues: Setting Values");
        //Set image
        if(mDinner.getImageURL() != null && !(mDinner.getImageURL().isEmpty()))
        {
            Glide.with(this)
                    .asBitmap()
                    .load(mDinner.getImageURL())
                    .into(imImage);
        } else {
            Glide.with(this)
                    .asBitmap()
                    .load("http://www.dinktoons.com/wp-content/uploads/2011/06/dinosaur-veggie.jpg")
                    .into(imImage);
        }

        tvTitle.setText(getText(R.string.dinner_string).toString());
        tvHost.setText(getText(R.string.host).toString() + " " + mDinner.getHost().getName());

        Date date = new Date(mDinner.getDateTime());
        SimpleDateFormat simpleDate =  new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
        String strDt = simpleDate.format(date);
        tvDate.setText(getText(R.string.date_string).toString() + " " + strDt);

        if (!(mDinner.getHost().getStreetName().isEmpty()) || !(mDinner.getHost().getZipCode().isEmpty()) || !(mDinner.getHost().getCity().isEmpty())) {
            tvAddress.setText(getText(R.string.address_text).toString() + " " + mDinner.getHost().getStreetName() + ", " + mDinner.getHost().getZipCode() + " " + mDinner.getHost().getCity());
        } else {
            tvAddress.setText(getText(R.string.address_text).toString() + " -");
        }

        if (mDinner.getHost().getPhone()!=0) {
            tvPhone.setText(getText(R.string.phone_string).toString() + " " + mDinner.getHost().getPhone());
        } else {
            tvPhone.setText(getText(R.string.phone_string).toString() + " -");
        }

        if (!mDinner.getHost().getEmail().isEmpty()) {
            tvEmail.setText(getText(R.string.email_string).toString() + " " + mDinner.getHost().getEmail());
        } else {
            tvEmail.setText(getText(R.string.email_string).toString() + " -");
        }

        tvMessage.setText(mDinner.getComment());
    }
}
