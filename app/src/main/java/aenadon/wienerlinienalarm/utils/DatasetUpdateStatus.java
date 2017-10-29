package aenadon.wienerlinienalarm.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class DatasetUpdateStatus {

    private static String KEY_SNACKBAR_MESSAGE = "KEY_SNACKBAR_MESSAGE";

    public static String getSnackbarMessage(Context ctx) {
        return getPrefs(ctx).getString(KEY_SNACKBAR_MESSAGE, null);
    }

    public static void putSnackbarMessage(Context ctx, String message) {
        getPrefs(ctx).edit().putString(KEY_SNACKBAR_MESSAGE, message).apply();
    }

    public static void deleteSnackbarMessage(Context ctx) {
        getPrefs(ctx).edit().remove(KEY_SNACKBAR_MESSAGE).apply();
    }

    private static SharedPreferences getPrefs(Context ctx) {
        String FILE_DATASET_UPDATE_STATUS = "FILE_DATASET_UPDATE_STATUS";
        return ctx.getSharedPreferences(FILE_DATASET_UPDATE_STATUS, Context.MODE_PRIVATE);
    }

}
