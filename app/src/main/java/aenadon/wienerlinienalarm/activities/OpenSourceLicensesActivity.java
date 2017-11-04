package aenadon.wienerlinienalarm.activities;

import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import aenadon.wienerlinienalarm.R;

public class OpenSourceLicensesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_source_licenses);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView openSourceTextView = findViewById(R.id.open_source_textview);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            openSourceTextView.setText(
                Html.fromHtml(
                        getString(R.string.open_source_licenses),
                        Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST |
                                Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH
                )
            );
        } else {
            openSourceTextView.setText(Html.fromHtml(getString(R.string.open_source_licenses)));
        }
        openSourceTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
