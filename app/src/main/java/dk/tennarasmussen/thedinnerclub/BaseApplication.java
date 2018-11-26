package dk.tennarasmussen.thedinnerclub;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

//Application class modified from https://www.youtube.com/watch?v=FbpD5RZtbCc
public class BaseApplication extends Application {
    public static final String CHANNEL_ID = "firebaseServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel firebaseServiceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Firebase Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(firebaseServiceChannel);
        }
    }
}
