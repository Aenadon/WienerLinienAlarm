package aenadon.wienerlinienalarm.enums;

import android.content.Context;

import java.util.Arrays;
import java.util.List;

import aenadon.wienerlinienalarm.R;

public enum VibrationMode {
    NONE(0, R.string.alarm_none),
    SHORT(100, R.string.alarm_vibration_short),
    MEDIUM(250, R.string.alarm_vibration_medium),
    LONG(500, R.string.alarm_vibration_long);

    private int duration;
    private int messageCode;

    private static List<VibrationMode> vibrationModeList = Arrays.asList(VibrationMode.values());

    VibrationMode(int duration, int messageCode) {
        this.duration = duration;
        this.messageCode = messageCode;
    }

    public static String[] getMessageCodes(Context ctx) {
        String[] messageCodes = new String[vibrationModeList.size()];
        for (int i = 0; i < vibrationModeList.size(); i++) {
            messageCodes[i] = ctx.getString(vibrationModeList.get(i).getMessageCode());
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
