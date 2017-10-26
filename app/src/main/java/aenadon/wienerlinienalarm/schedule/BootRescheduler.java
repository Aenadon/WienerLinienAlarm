package aenadon.wienerlinienalarm.schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import trikita.log.Log;

public class BootRescheduler extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (intentAction == null || !intentAction.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e("Intent action not matching " + Intent.ACTION_BOOT_COMPLETED + ", terminating");
            return;
        }


    }
}
