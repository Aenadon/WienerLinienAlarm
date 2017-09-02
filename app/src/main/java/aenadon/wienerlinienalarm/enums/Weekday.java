package aenadon.wienerlinienalarm.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Weekday {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    private static List<Weekday> weekdayList = Arrays.asList(Weekday.values());

    public static List<Weekday> weekdaysFromByte(byte days) {
        List<Weekday> weekdaysAsList = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            byte x = (byte)Math.pow(2,i);
            boolean dayChosen = (days & x) == x;
            if (dayChosen) {
                weekdaysAsList.add(weekdayList.get(i));
            }
        }

        return weekdaysAsList;
    }

    public static byte byteFromWeekdays(List<Weekday> weekdays) {
        byte weekdaysAsByte = 0;
        for (int i = 0; i < 7; i++) {
            if (weekdays.contains(weekdayList.get(i))) {
                weekdaysAsByte += Math.pow(2, i);
            }
        }
        return weekdaysAsByte;
    }

}
