package aenadon.wienerlinienalarm.activities.pickers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.activities.StationPickerActivity;
import aenadon.wienerlinienalarm.utils.Keys;

public class StationSteigPicker implements AlarmPicker {
    private String displayName;
    private String steigId;
    private TextView viewToUse;

    private Context ctx;

    private static final String VIEW_RES_ID_KEY = "VIEW_RES_ID";
    private static final String STEIG_ID_KEY = "STEIG_ID";
    private static final String DISPLAY_NAME_KEY = "DISPLAY_NAME";

    public StationSteigPicker(Context ctx, int viewResId) {
        this.ctx = ctx;
        this.viewToUse = ((Activity)ctx).findViewById(viewResId);
    }

    public void show() {
        ((Activity)ctx).startActivityForResult(new Intent(ctx, StationPickerActivity.class), Keys.RequestCode.SELECT_STEIG);
    }

    public void setPickedSteig(Intent data) {
        String stationName = data.getStringExtra(Keys.Extra.SELECTED_STATION_NAME);
        String lineNameAndDirection = data.getStringExtra(Keys.Extra.LINE_NAME_AND_DIRECTION);

        displayName = stationName + "\n" + lineNameAndDirection;
        steigId = data.getStringExtra(Keys.Extra.SELECTED_STEIG_ID);
        viewToUse.setText(displayName);
    }

    public String getPickedSteig() {
        return steigId;
    }

    public void setPickedSteig(String pickedSteig) {
        this.steigId = pickedSteig;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public Bundle saveState() {
        Bundle saveBundle = new Bundle();
        saveBundle.putString(DISPLAY_NAME_KEY, displayName);
        saveBundle.putString(STEIG_ID_KEY, steigId);
        if (viewToUse != null) {
            saveBundle.putInt(VIEW_RES_ID_KEY, viewToUse.getId());
        }
        return saveBundle;
    }

    @Override
    public void restoreState(Context ctx, Bundle restoreBundle) {
        int viewResId = restoreBundle.getInt(VIEW_RES_ID_KEY);
        viewToUse = ((Activity)ctx).findViewById(viewResId);

        displayName = restoreBundle.getString(DISPLAY_NAME_KEY);
        steigId = restoreBundle.getString(STEIG_ID_KEY);

        if (viewToUse != null && displayName != null) {
            viewToUse.setText(displayName);
        }
    }

    @Override
    public boolean hasError() {
        return steigId == null;
    }

    @Override
    public Integer getErrorStringId() {
        return R.string.missing_info_station;
    }
}
