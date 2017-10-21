package aenadon.wienerlinienalarm.enums;

import android.content.Context;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import aenadon.wienerlinienalarm.R;

public enum Weekday {
    MONDAY(R.string.monday),
    TUESDAY(R.string.tuesday),
    WEDNESDAY(R.string.wednesday),
    THURSDAY(R.string.thursday),
    FRIDAY(R.string.friday),
    SATURDAY(R.string.saturday),
    SUNDAY(R.string.sunday);

    int stringResId;
    private static List<Weekday> allWeekdaysList = Arrays.asList(Weekday.values());

    Weekday(int stringResId) {
        this.stringResId = stringResId;
    }

    public int getStringResId() {
        return stringResId;
    }

    public static String[] getAllStrings(Context ctx) {
        String[] strings = new String[7];
        for (int i = 0; i < allWeekdaysList.size(); i++) {
            strings[i] = ctx.getString(allWeekdaysList.get(i).getStringResId());
        }
        return strings;
    }

    public static Set<Weekday> weekdaysFromByte(byte days) {
        Set<Weekday> weekdaysAsSet = new HashSet<>();

        for (int i = 0; i < allWeekdaysList.size(); i++) {
            byte x = (byte)(2 << i);
            boolean dayChosen = (days & x) == x;
            if (dayChosen) {
                weekdaysAsSet.add(allWeekdaysList.get(i));
            }
        }
        return weekdaysAsSet;
    }

    public static byte byteFromWeekdays(Set<Weekday> weekdays) {
        byte weekdaysAsByte = 0;
        for (int i = 0; i < allWeekdaysList.size(); i++) {
            if (weekdays.contains(allWeekdaysList.get(i))) {
                weekdaysAsByte += (byte)(2 << i);
            }
        }
        return weekdaysAsByte;
    }

    public static boolean weekdaysOnlySelected(Set<Weekday> weekdays) {
        return weekdays.size() == 5 &&
                weekdays.contains(MONDAY) &&
                weekdays.contains(TUESDAY) &&
                weekdays.contains(WEDNESDAY) &&
                weekdays.contains(THURSDAY) &&
                weekdays.contains(FRIDAY);
    }

    public static boolean weekendOnlySelected(Set<Weekday> weekdays) {
        return weekdays.size() == 2 &&
                weekdays.contains(SATURDAY) &&
                weekdays.contains(SUNDAY);
    }
}
