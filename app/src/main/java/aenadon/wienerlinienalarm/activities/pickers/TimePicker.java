package aenadon.wienerlinienalarm.activities.pickers;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import org.threeten.bp.LocalTime;

import java.util.Calendar;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.utils.Keys;
import aenadon.wienerlinienalarm.utils.StringDisplay;


public class TimePicker extends DialogFragment implements AlarmPicker, TimePickerDialog.OnTimeSetListener {

    private int[] pickedTime = null;

    private TextView viewToUse;
    private boolean firstRun = true;

    private static final String viewResIdKey = "VIEW_RES_ID";
    private static final String chosenTimeKey = "CHOSEN_TIME";
    private static final String firstRunKey = "FIRST_RUN";

    public void show() {
        super.show(getActivity().getFragmentManager(), "TimePicker");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int viewResId = getArguments().getInt(Keys.Extra.VIEW_TO_USE);
        viewToUse = (TextView) getActivity().findViewById(viewResId);
        int[] prevTime = getArguments().getIntArray(Keys.Extra.PREV_TIME);

        int hour, minute;
        if (pickedTime != null) {
            hour = pickedTime[0];
            minute = pickedTime[1];
        } else if (prevTime != null) {
            hour = prevTime[0];
            minute = prevTime[1];
        } else {
            Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        return new TimePickerDialog(getActivity(), this, hour, minute, true);
    }

    public LocalTime getPickedTime() {
        if (pickedTime != null) {
            return LocalTime.of(pickedTime[0], pickedTime[1]);
        }
        return null;
    }

    @Override
    public Bundle saveState() {
        Bundle saveBundle = new Bundle();
        if (viewToUse != null) {
            saveBundle.putInt(viewResIdKey, viewToUse.getId());
        }
        saveBundle.putIntArray(chosenTimeKey, pickedTime);
        saveBundle.putBoolean(firstRunKey, firstRun);
        return saveBundle;
    }

    @Override
    public void restoreState(Context ctx, Bundle restoreBundle) {
        pickedTime = restoreBundle.getIntArray(chosenTimeKey);
        firstRun = restoreBundle.getBoolean(firstRunKey);

        int viewResId = restoreBundle.getInt(viewResIdKey);
        viewToUse = (TextView) ((Activity) ctx).findViewById(viewResId);
        if (pickedTime != null) viewToUse.setText(StringDisplay.getTime(pickedTime));
    }

    @Override
    public boolean hasError() {
        return pickedTime == null;
    }

    @Override
    public Integer getErrorStringId() {
        return R.string.missing_info_time;
    }

    @Override
    public void onTimeSet(android.widget.TimePicker view, int hourOfDay, int minute) {
        pickedTime = new int[]{hourOfDay, minute};
        viewToUse.setText(StringDisplay.getTime(pickedTime));
        firstRun = false;
    }
}
