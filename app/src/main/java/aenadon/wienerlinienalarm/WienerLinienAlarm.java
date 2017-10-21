package aenadon.wienerlinienalarm;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

import io.realm.Realm;


public class WienerLinienAlarm extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(getApplicationContext());
        AndroidThreeTen.init(getApplicationContext());
    }

}
