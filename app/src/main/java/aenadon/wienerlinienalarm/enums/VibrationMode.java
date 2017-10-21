package aenadon.wienerlinienalarm.enums;

import android.content.Context;

import aenadon.wienerlinienalarm.R;

public enum VibrationMode {
    NONE(0, R.string.alarm_none),
    SHORT(100, R.string.alarm_vibration_short),
    MEDIUM(250, R.string.alarm_vibration_medium),
    LONG(500, R.string.alarm_vibration_long);

    private int duration;
    private int messageCode;

    VibrationMode(int duration, int messageCode) {
        this.duration = duration;
        this.messageCode = messageCode;
    }

    public static String[] getMessageCodes(Context ctx) {
        VibrationMode[] allValues = VibrationMode.values();
        String[] messageCodes = new String[allValues.length];

        for (int i = 0; i < allValues.length; i++) {
            messageCodes[i] = ctx.getString(allValues[i].getMessageCode());
        }
        return messageCodes;
    }

    public int getDuration() {
        return duration;
    }

    public int getMessageCode() {
        return messageCode;
    }
}
