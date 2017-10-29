package aenadon.wienerlinienalarm.activities.pickers;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.enums.VibrationMode;

public class VibrationPicker implements AlarmPicker {

    private VibrationMode pickedMode = VibrationMode.NONE;
    private VibrationMode[] vibrationModes;
    private TextView viewToUse;
    private AlertDialog vibrationPickerDialog;

    private static final String VIEW_RES_ID_KEY = "VIEW_RES_ID";
    private static final String PICKED_VIBRATION_MODE_KEY = "PICKED_VIBRATION_MODE_KEY";

    public VibrationPicker(final Context ctx, int viewResId) {
        this.viewToUse = (TextView)((Activity)ctx).findViewById(viewResId);
        this.vibrationModes = VibrationMode.values();

        final Vibrator vibrator = (Vibrator)ctx.getSystemService(Context.VIBRATOR_SERVICE);
        this.vibrationPickerDialog = new AlertDialog.Builder(ctx)
                .setTitle(R.string.alarm_choose_vibration_length)
                .setItems(VibrationMode.getMessageCodes(ctx), (dialog, which) -> {
                    pickedMode = vibrationModes[which];
                    if (vibrator.hasVibrator()) {
                        vibrator.vibrate(pickedMode.getDuration());
                    }
                    viewToUse.setText(pickedMode.getMessageCode());
                }).create();
    }

    public void show() {
        vibrationPickerDialog.show();
    }

    public VibrationMode getPickedMode() {
        return pickedMode;
    }

    public void setPickedMode(VibrationMode pickedMode) {
        this.pickedMode = pickedMode;
    }

    @Override
    public Bundle saveState() {
        Bundle saveBundle = new Bundle();
        if (viewToUse != null) {
            saveBundle.putInt(VIEW_RES_ID_KEY, viewToUse.getId());
        }
        saveBundle.putInt(PICKED_VIBRATION_MODE_KEY, pickedMode.ordinal());
        return saveBundle;
    }

    @Override
    public void restoreState(Context ctx, Bundle restoreBundle) {
        pickedMode = vibrationModes[restoreBundle.getInt(PICKED_VIBRATION_MODE_KEY)];

        int viewResId = restoreBundle.getInt(VIEW_RES_ID_KEY);
        viewToUse = (TextView)((Activity)ctx).findViewById(viewResId);
        viewToUse.setText(pickedMode.getMessageCode());
    }

    @Override
    public boolean hasError() {
        return false;
    }

    @Override
    public Integer getErrorStringId() {
        throw new UnsupportedOperationException("No error for Vibration picker");
    }
}
