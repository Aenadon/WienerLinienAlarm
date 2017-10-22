package aenadon.wienerlinienalarm.activities.pickers;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import aenadon.wienerlinienalarm.R;
import aenadon.wienerlinienalarm.enums.Weekday;
import aenadon.wienerlinienalarm.utils.StringDisplay;

public class DaysPicker implements AlarmPicker {

    private boolean[] savedChoice;
    private boolean[] dialogTemporaryChoice;
    private TextView viewToUse;

    private AlertDialog.Builder weekdayDialogBuilder;
    private String[] weekdayStrings;

    private static final String VIEW_RES_ID_KEY = "VIEW_RES_ID";
    private static final String CHOICE_KEY = "CHOICE";
    private static final String TEMP_CHOICE_KEY = "TEMP_CHOICE";

    public DaysPicker(final Context ctx, Set<Weekday> previousChoice, int viewResId) {
        this.weekdayStrings = Weekday.getAllStrings(ctx);
        if (previousChoice != null) {
            boolean[] previousChoiceArray = arrayFromSet(previousChoice);
            this.savedChoice = previousChoiceArray.clone();
        } else {
            this.savedChoice = new boolean[7];
        }
        this.dialogTemporaryChoice = savedChoice.clone();

        this.viewToUse = (TextView)((Activity)ctx).findViewById(viewResId);

        weekdayDialogBuilder = new AlertDialog.Builder(ctx)
                .setTitle(R.string.alarm_recurring_dialog_expl)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        savedChoice = dialogTemporaryChoice.clone();
                        viewToUse.setText(StringDisplay.getRecurringDays(ctx, setFromArray(savedChoice)));
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogTemporaryChoice = savedChoice.clone();
                    }
                });
    }

    public void show() {
        // need to do that before every "show" because the boolean array is cloned internally on set
        updateCheckedItems();
        weekdayDialogBuilder.show();
    }

    private void updateCheckedItems() {
        weekdayDialogBuilder.setMultiChoiceItems(weekdayStrings, dialogTemporaryChoice, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                dialogTemporaryChoice[which] = isChecked;
            }
        });
    }

    public Set<Weekday> getPickedDays() {
        return setFromArray(savedChoice);
    }

    @Override
    public Bundle saveState() {
        Bundle saveBundle = new Bundle();
        if (viewToUse != null) {
            saveBundle.putInt(VIEW_RES_ID_KEY, viewToUse.getId());
        }
        saveBundle.putBooleanArray(CHOICE_KEY, savedChoice);
        saveBundle.putBooleanArray(TEMP_CHOICE_KEY, savedChoice);
        return saveBundle;
    }

    @Override
    public void restoreState(Context ctx, Bundle restoreBundle) {
        savedChoice = restoreBundle.getBooleanArray(CHOICE_KEY);
        dialogTemporaryChoice = restoreBundle.getBooleanArray(TEMP_CHOICE_KEY);

        int viewResId = restoreBundle.getInt(VIEW_RES_ID_KEY);
        viewToUse = (TextView) ((Activity) ctx).findViewById(viewResId);
        if (savedChoice != null) viewToUse.setText(StringDisplay.getRecurringDays(ctx, setFromArray(savedChoice)));
    }

    @Override
    public boolean hasError() {
        return setFromArray(savedChoice).isEmpty();
    }

    @Override
    public Integer getErrorStringId() {
        return R.string.missing_info_days;
    }

    private boolean[] arrayFromSet(Set<Weekday> weekdays) {
        List<Weekday> allWeekdaysList = Weekday.getAllWeekdaysList();
        boolean[] weekdayArray = new boolean[7];
        for (int i = 0; i < allWeekdaysList.size(); i++) {
            if (weekdays.contains(allWeekdaysList.get(i))) {
                weekdayArray[i] = true;
            }
        }
        return weekdayArray;
    }

    private Set<Weekday> setFromArray(boolean[] weekdayArray) {
        List<Weekday> allWeekdaysList = Weekday.getAllWeekdaysList();
        Set<Weekday> weekdaySet = new HashSet<>();
        for (int i = 0; i < allWeekdaysList.size(); i++) {
            if (weekdayArray[i]) {
                weekdaySet.add(allWeekdaysList.get(i));
            }
        }
        return weekdaySet;
    }
}
