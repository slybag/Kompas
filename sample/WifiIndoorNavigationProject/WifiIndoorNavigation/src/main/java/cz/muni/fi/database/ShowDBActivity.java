package cz.muni.fi.database;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cz.muni.fi.wifinavigation.R;
import cz.muni.fi.model.RssFingerprint;
import cz.muni.fi.wifinavigation.WifiNavigationActivity;

/**
 * Created by Michal on 5.11.13.
 */
public class ShowDBActivity extends Activity {
    private RssDataSource dataSource = WifiNavigationActivity.getDataSource();
    private final String TAG = "ShowDBActivity";

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.db_activity);
        String text = "";
        List<RssFingerprint> listToView = new ArrayList<RssFingerprint>();

        Intent intent = getIntent();
        String table = intent.getStringExtra("table");

        Log.d(TAG, table);

        if (table.equals(MySQLiteHelper.TABLE_MERGE)) listToView = dataSource.getAllMergedFingerprints();
        if (table.equals(MySQLiteHelper.TABLE_RSSFINGERPRINT)) listToView = dataSource.getAllFingerprints();

        for (RssFingerprint r : listToView) {
            text += r.toString() + "\n";
        }

        Log.d(TAG, text);

        TextView view = (TextView) findViewById(R.id.dbText);
        view.setMovementMethod(new ScrollingMovementMethod());
        view.setText(text);
    }
}
