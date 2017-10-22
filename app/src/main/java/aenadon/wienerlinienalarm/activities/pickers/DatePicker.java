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
import aenadon.wienerlinienalarm.utils.StringDisplay;

public class DatePicker extends DialogFragment implements AlarmPicker, DatePickerDialog.OnDateSetListener {

    private LocalDate pickedDate = null;
    private TextView viewToUse;

    private static final String VIEW_RES_ID_KEY = "VIEW_RES_ID";
    private static final String CHOSEN_DATE_KEY = "CHOSEN_TIME";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int dateDisplayViewId = getArguments().getInt(Keys.Extra.VIEW_TO_USE);
        viewToUse = (TextView) getActivity().findViewById(dateDisplayViewId);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LocalDate previousSavedDate = intArrayToLocalDate(getArguments().getIntArray(Keys.Extra.PREV_DATE));

        // Calendar months start at 0, therefore we need -1
        int year = 0;
        int month = -1;
        int day = 0;
        if (pickedDate != null) {
            year += pickedDate.getYear();
            month += pickedDate.getMonthValue();
            day += pickedDate.getDayOfMonth();
        } else if (previousSavedDate != null) {
            year += previousSavedDate.getYear();
            month += previousSavedDate.getMonthValue();
            day += previousSavedDate.getDayOfMonth();
        }

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), DatePicker.this, year, month, day);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
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
        viewToUse = (TextView) ((Activity) ctx).findViewById(dateDisplayViewId);
        if (pickedDate != null) viewToUse.setText(StringDisplay.getOnetimeDate(pickedDate));
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
        // java.util.Calendar months start at 0, therefore we need +1
        pickedDate = intArrayToLocalDate(new int[]{year, month + 1, day});
        viewToUse.setText(StringDisplay.getOnetimeDate(pickedDate));
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
                    dateObject.getYear()
            };
        }
        return null;
    }
}
