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

    private LocalDate chosenDate = null;
    private TextView dateDisplayView;

    private static final String VIEW_RES_ID_KEY = "VIEW_RES_ID";
    private static final String CHOSEN_DATE_KEY = "CHOSEN_TIME";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int dateDisplayViewId = getArguments().getInt(Keys.Extra.VIEW_TO_USE);
        dateDisplayView = (TextView) getActivity().findViewById(dateDisplayViewId);
        LocalDate previousSavedDate = intArrayToLocalDate(getArguments().getIntArray(Keys.Extra.PREV_DATE));

        // Calendar months start at 0, therefore we need -1
        int year = 0;
        int month = -1;
        int day = 0;
        if (chosenDate != null) {
            year += chosenDate.getYear();
            month += chosenDate.getMonthValue();
            day += chosenDate.getDayOfMonth();
        } else if (previousSavedDate != null) {
            year += previousSavedDate.getYear();
            month += previousSavedDate.getMonthValue();
            day += previousSavedDate.getDayOfMonth();
        }

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), DatePicker.this, year, month, day);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        return dialog;
    }

    public LocalDate getChosenDate() {
        return chosenDate;
    }

    @Override
    public Bundle saveState() {
        Bundle saveBundle = new Bundle();
        if (dateDisplayView != null) {
            saveBundle.putInt(VIEW_RES_ID_KEY, dateDisplayView.getId());
        }
        saveBundle.putIntArray(CHOSEN_DATE_KEY, localDateToIntArray(chosenDate));
        return saveBundle;
    }

    @Override
    public void restoreState(Context ctx, Bundle restoreBundle) {
        chosenDate = intArrayToLocalDate(restoreBundle.getIntArray(CHOSEN_DATE_KEY));

        int dateDisplayViewId = restoreBundle.getInt(VIEW_RES_ID_KEY);
        dateDisplayView = (TextView) ((Activity) ctx).findViewById(dateDisplayViewId);
        if (chosenDate != null) dateDisplayView.setText(StringDisplay.getOnetimeDate(chosenDate));
    }

    @Override
    public boolean hasError() {
        return chosenDate == null;
    }

    @Override
    public Integer getErrorStringId() {
        return R.string.missing_info_date;
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year, int month, int day) {
        // java.util.Calendar months start at 0, therefore we need +1
        chosenDate = intArrayToLocalDate(new int[]{year, month + 1, day});
        dateDisplayView.setText(StringDisplay.getOnetimeDate(chosenDate));
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
