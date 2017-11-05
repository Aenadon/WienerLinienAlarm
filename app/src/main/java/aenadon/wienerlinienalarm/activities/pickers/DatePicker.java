package aenadon.wienerlinienalarm.activities.pickers;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import org.threeten.bp.LocalDate;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.utils.Keys;
import aenadon.wienerlinienalarm.utils.StringFormatter;

public class DatePicker extends DialogFragment implements AlarmPicker, DatePickerDialog.OnDateSetListener {

    private LocalDate pickedDate = null;
    private TextView viewToUse;

    private static final String VIEW_RES_ID_KEY = "VIEW_RES_ID";
    private static final String CHOSEN_DATE_KEY = "CHOSEN_TIME";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int viewResId = getArguments().getInt(Keys.Extra.VIEW_TO_USE);
        viewToUse = getActivity().findViewById(viewResId);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year = 0;
        int month = 0;
        int day = 0;
        if (pickedDate != null) {
            year = pickedDate.getYear();
            month = pickedDate.getMonthValue();
            day = pickedDate.getDayOfMonth();
        }

        // DatePickerDialog uses java.util.Calendar whose months start at 0
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), DatePicker.this, year, month - 1, day);
        // -1000 prevents crash if user selects current date and then opens datepicker again
        // because !("now" < "now")
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        return dialog;
    }

    public LocalDate getPickedDate() {
        return pickedDate;
    }

    public void setPickedDate(LocalDate pickedDate) {
        this.pickedDate = pickedDate;
    }

    @Override
    public Bundle saveState() {
        Bundle saveBundle = new Bundle();
        if (viewToUse != null) {
            saveBundle.putInt(VIEW_RES_ID_KEY, viewToUse.getId());
        }
        saveBundle.putIntArray(CHOSEN_DATE_KEY, localDateToIntArray(pickedDate));
        return saveBundle;
    }

    @Override
    public void restoreState(Context ctx, Bundle restoreBundle) {
        pickedDate = intArrayToLocalDate(restoreBundle.getIntArray(CHOSEN_DATE_KEY));

        int dateDisplayViewId = restoreBundle.getInt(VIEW_RES_ID_KEY);
        viewToUse = ((Activity) ctx).findViewById(dateDisplayViewId);
        if (viewToUse != null && pickedDate != null) {
            viewToUse.setText(StringFormatter.formatLocalDate(pickedDate));
        }
    }

    @Override
    public boolean hasError() {
        return pickedDate == null;
    }

    @Override
    public Integer getErrorStringId() {
        return R.string.missing_info_date;
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int day) {
        // DatePickerDialog uses java.util.Calendar whose months start at 0
        pickedDate = intArrayToLocalDate(new int[]{year, month + 1, day});
        viewToUse.setText(StringFormatter.formatLocalDate(pickedDate));
    }

    private LocalDate intArrayToLocalDate(int[] dateArray) {
        if (dateArray != null && dateArray.length == 3) {
            return LocalDate.of(dateArray[0], dateArray[1], dateArray[2]);
        }
        return null;
    }

    private int[] localDateToIntArray(LocalDate dateObject) {
        if (dateObject != null) {
            return new int[]{
                    dateObject.getYear(),
                    dateObject.getMonthValue(),
                    dateObject.getDayOfMonth()
            };
        }
        return null;
    }
}
