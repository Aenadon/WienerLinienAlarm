package aenadon.wienerlinienalarm.activities.pickers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import aenadon.wienerlinienalarm.utils.Keys;
import aenadon.wienerlinienalarm.utils.StringDisplay;

public class RingtonePicker implements AlarmPicker {

    private String pickedRingtone;
    private String previousRingtone;
    private TextView viewToUse;

    private Context ctx;

    private static final String VIEW_RES_ID_KEY = "VIEW_RES_ID";
    private static final String PICKED_RINGTONE_KEY = "PICKED_RINGTONE_KEY";

    public RingtonePicker(Context ctx, int viewResId) {
        this.ctx = ctx;

        viewToUse = (TextView)((Activity)ctx).findViewById(viewResId);
    }

    public void show() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                .putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        if (pickedRingtone == null && previousRingtone != null) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(previousRingtone));
        } else if (pickedRingtone != null) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(pickedRingtone));
        }

        ((Activity)ctx).startActivityForResult(intent, Keys.RequestCode.SELECT_RINGTONE);
    }

    public String getPickedRingtone() {
        return pickedRingtone;
    }

    public void setPickedRingtone(String pickedRingtone) {
        this.pickedRingtone = pickedRingtone;
    }

    public void setPickedRingtone(Context ctx, Intent data) {
        Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
        pickedRingtone = (uri != null) ? uri.toString() : null;
        viewToUse.setText(StringDisplay.getRingtone(ctx, pickedRingtone));
    }

    @Override
    public Bundle saveState() {
        Bundle saveBundle = new Bundle();
        if (viewToUse != null) {
            saveBundle.putInt(VIEW_RES_ID_KEY, viewToUse.getId());
        }
        saveBundle.putString(PICKED_RINGTONE_KEY, pickedRingtone);
        return saveBundle;
    }

    @Override
    public void restoreState(Context ctx, Bundle restoreBundle) {
        int viewResId = restoreBundle.getInt(VIEW_RES_ID_KEY);
        viewToUse = (TextView) ((Activity) ctx).findViewById(viewResId);

        pickedRingtone = restoreBundle.getString(PICKED_RINGTONE_KEY);
        if (pickedRingtone != null) viewToUse.setText(StringDisplay.getRingtone(ctx, pickedRingtone));
    }

    @Override
    public boolean hasError() {
        return false;
    }

    @Override
    public Integer getErrorStringId() {
        throw new UnsupportedOperationException("No error for Ringtone picker");
    }
}
