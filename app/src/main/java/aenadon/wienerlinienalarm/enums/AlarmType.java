package aenadon.wienerlinienalarm.enums;

import aenadon.wienerlinienalarm.R;

public enum AlarmType {
    ONETIME(R.string.one_time_alarms),
    RECURRING(R.string.recurring_alarms);

    private int messageCode;

    AlarmType(int messageCode) {
        this.messageCode = messageCode;
    }

    public int getMessageCode() {
        return messageCode;
    }
}
