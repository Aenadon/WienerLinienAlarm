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

    private LocalTime pickedTime = null;

    private TextView viewToUse;

    private static final String VIEW_RES_ID_KEY = "VIEW_RES_ID";
    private static final String CHOSEN_TIME_KEY = "CHOSEN_TIME";

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
            hour = pickedTime.getHour();
            minute = pickedTime.getMinute();
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
        return pickedTime;
    }

    @Override
    public Bundle saveState() {
        Bundle saveBundle = new Bundle();
        if (viewToUse != null) {
            saveBundle.putInt(VIEW_RES_ID_KEY, viewToUse.getId());
        }
        saveBundle.putIntArray(CHOSEN_TIME_KEY, localTimeToIntArray(pickedTime));
        return saveBundle;
    }

    @Override
    public void restoreState(Context ctx, Bundle restoreBundle) {
        int[] pickedTimeArray = restoreBundle.getIntArray(CHOSEN_TIME_KEY);
        pickedTime = intArrayToLocalTime(pickedTimeArray);

        int viewResId = restoreBundle.getInt(VIEW_RES_ID_KEY);
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
        pickedTime = LocalTime.of(hourOfDay, minute);
        viewToUse.setText(StringDisplay.getTime(pickedTime));
    }

    private LocalTime intArrayToLocalTime(int[] timeArray) {
        if (timeArray != null && timeArray.length == 2) {
            return LocalTime.of(timeArray[0], timeArray[1]);
        }
        return null;
    }

    private int[] localTimeToIntArray(LocalTime timeObject) {
        if (timeObject != null) {
            return new int[]{timeObject.getHour(), timeObject.getMinute()};
        }
        return null;
    }
}
