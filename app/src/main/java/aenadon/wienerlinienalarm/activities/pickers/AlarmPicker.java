package aenadon.wienerlinienalarm.activities.pickers;

import android.content.Context;
import android.os.Bundle;

public interface AlarmPicker {

    Bundle saveState();

    void restoreState(Context ctx, Bundle restoreBundle);

    boolean hasError();

    Integer getErrorStringId();
}
