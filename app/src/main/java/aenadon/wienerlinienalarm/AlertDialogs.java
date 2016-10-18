package aenadon.wienerlinienalarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

class AlertDialogs {

    static void serverNotAvailable(Context ctx) {
        createResultCanceledBox(ctx,
                ctx.getString(R.string.connection_error_title),
                ctx.getString(R.string.connection_error_text) + " " + ctx.getString(R.string.connection_error_text_add),
                ctx.getString(R.string.ok));
    }

    static void noSteigsAvailable(Context ctx) {
        createResultCanceledBox(ctx,
                ctx.getString(R.string.no_steigs_error_title),
                ctx.getString(R.string.no_steigs_error_text),
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
                .show();
    }

}
