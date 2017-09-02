package aenadon.wienerlinienalarm.models.alarm;

import java.util.List;

import aenadon.wienerlinienalarm.enums.Weekday;

public class RecurringAlarmData extends AlarmData {

    private byte recurringChosenDays;

    public RecurringAlarmData() {
    }

    public List<Weekday> getRecurringChosenDays() {
        return Weekday.weekdaysFromByte(recurringChosenDays);
    }

    public void setRecurringChosenDays(List<Weekday> recurringChosenDays) {
        this.recurringChosenDays = Weekday.byteFromWeekdays(recurringChosenDays);
    }
}
