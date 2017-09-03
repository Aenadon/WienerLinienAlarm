package aenadon.wienerlinienalarm;

import android.app.Application;

import io.realm.Realm;


public class WienerLinienAlarm extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(getApplicationContext());
    }

}
