package aenadon.wienerlinienalarm.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import aenadon.wienerlinienalarm.BuildConfig;


public class AlarmReceiver extends BroadcastReceiver {

    private static String API_KEY = BuildConfig.API_KEY;

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
