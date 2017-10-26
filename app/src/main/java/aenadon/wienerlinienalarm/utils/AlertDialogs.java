package aenadon.wienerlinienalarm.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import aenadon.wienerlinienalarm.R;

@Deprecated
public class AlertDialogs {

    // This class stores some often-used dialog boxes

    public static void serverNotAvailable(Context ctx) {
        createResultCanceledBox(ctx,
                ctx.getString(R.string.server_broken_title),
                ctx.getString(R.string.server_broken_text),
                ctx.getString(R.string.ok));
    }

    public static void noConnection(Context ctx) {
        createResultCanceledBox(ctx,
                ctx.getString(R.string.no_connection_title),
                ctx.getString(R.string.no_connection_text),
                ctx.getString(R.string.ok));
    }

    public static void noSteigsAvailable(Context ctx) {
        createResultCanceledBox(ctx,
                ctx.getString(R.string.no_steigs_error_title),
                ctx.getString(R.string.no_steigs_error_text),
                ctx.getString(R.string.ok));
    }

    public static void missingInfo(Context ctx, String errors) {
        createOneButtonBox(ctx,
                ctx.getString(R.string.missing_info_title),
                errors,
                ctx.getString(R.string.ok));
    }


    private static void createOneButtonBox(final Context ctx, String title, String message, String button) {
        new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(button, null)
                .show();
    }

    // Constructs the message box
    private static void createResultCanceledBox(final Context ctx, String title, String message, String button) {
        new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((Activity)ctx).setResult(Activity.RESULT_CANCELED);
                        ((Activity)ctx).finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
}
