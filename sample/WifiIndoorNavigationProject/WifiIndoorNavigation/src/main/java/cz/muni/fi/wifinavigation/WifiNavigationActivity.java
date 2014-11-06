package cz.muni.fi.wifinavigation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import cz.muni.fi.database.MySQLiteHelper;
import cz.muni.fi.database.RssDataSource;
import cz.muni.fi.database.ShowDBActivity;
import cz.muni.fi.model.RssFingerprint;
import cz.muni.fi.orientation.Compass;

public class WifiNavigationActivity extends Activity implements NumberPicker.OnValueChangeListener{

    public static final String TAG = "WifiNavigationActivity";
    protected static RssDataSource datasource;
    private SensorManager mSensorManager;
    private WifiManager mWifiManager;
    private static final int DIALOG_ALERT = 10;
    protected static Compass mCompass;
    protected static ScanningEngine mScanningEngine;
    protected static SearchingEngine mSearchingEngine;
    protected static Dialog d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.wifi_activity);
        datasource = new RssDataSource(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        try {
            Log.d(TAG, "database open");
            datasource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        mCompass = Compass.compassFactory("lorem", mSensorManager);
        mScanningEngine = new ScanningEngine(mSensorManager, this);
        mSearchingEngine = new SearchingEngine(mSensorManager, this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mCompass.start();
    }

    /*@Override
    protected void onStop() {
        super.onStop();
        mCompass.stop();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wifi_navigation, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteDb:
                datasource.deleteDatabase();
                return true;
            case R.id.position:
                //mSearchingEngine.getPosition();
                //String apName = mSearchingEngine.getPosition();
                //Toast.makeText(this, apName, Toast.LENGTH_SHORT).show();
                whereAmI();
                return true;
            case R.id.showDb:
                Intent intent = new Intent(this, ShowDBActivity.class);
                intent.putExtra("table", MySQLiteHelper.TABLE_RSSFINGERPRINT);
                Log.d(TAG, MySQLiteHelper.TABLE_RSSFINGERPRINT);
                startActivity(intent);
                return true;
            case R.id.mergeDb:
                Intent intentMerge = new Intent(this, ShowDBActivity.class);
                intentMerge.putExtra("table", MySQLiteHelper.TABLE_MERGE);
                Log.d(TAG, MySQLiteHelper.TABLE_MERGE);
                startActivity(intentMerge);
                return true;
            case R.id.deleteMerged:
                if (datasource.deleteTable(MySQLiteHelper.TABLE_MERGE)) Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.deleteRP:
                deleteRp();
                return true;
            case R.id.merge:
                boolean flag = true;

                for (int i = 0; i < 360; i += 45) {
                    if (datasource.merge(i, 69)) continue;
                    flag = false;
                    break;
                }

                if (flag) Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                    else Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void onClickHandler(View v) {
        Button b = (Button) v;
        /*Log.d(TAG, b.getText().toString());
        if (getScanResults(b)) Toast.makeText(this, "Succes", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();*/
        /*if (datasource.contains(((Button) v).getText().toString())) {
            showDialog(DIALOG_ALERT);
        }*/
        Intent intent = new Intent(this, ScanningActivity.class);
        intent.putExtra("View", b.getText());
        startActivity(intent);
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_ALERT:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("This will end the activity");
                builder.setCancelable(true);
                builder.setPositiveButton("I agree", new OkOnClickListener());
                builder.setNegativeButton("No, no", new CancelOnClickListener());
                AlertDialog dialog = builder.create();
                dialog.show();
        }
        return super.onCreateDialog(id);
    }

    private final class CancelOnClickListener implements
            DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            Toast.makeText(getApplicationContext(), "Activity will continue",
                    Toast.LENGTH_LONG).show();
        }
    }

    private final class OkOnClickListener implements
            DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int which) {

        }
    }

    public static boolean getScanResults(Button b, MenuItem item) {
        try {
            for (RssFingerprint s : mScanningEngine.getScanResult(b)) {

                datasource.createRecord(s);
            }
            return true;
        } catch (Exception e) {
            Log.d(TAG, "exception, failed");
            return false;
        } finally {

        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

        Log.i("value is",""+newVal);

    }

    public void whereAmI() {
        if (!mWifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Wifi is Disabled, please enable WiFi", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, List<RssFingerprint>> database = datasource.getSortedMergedFingerprints(mScanningEngine.getOrientation());
        int size = database.size();

        if (size == 0) {
            Toast.makeText(this, "No data in database", Toast.LENGTH_LONG).show();
            return;
        }

        final Dialog d = new Dialog(WifiNavigationActivity.this);

        d.setTitle("Choose your position");
        d.setContentView(R.layout.picker);

        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);

        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(size - 1);
        np.setMinValue(0);

        final String[] array = new String[size];
        int i = 0;

        for (String s : database.keySet()) {
            array[i] = s;
            i++;
        }

        np.setDisplayedValues(array);

        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                String rp = array[np.getValue()];
                String apName = mSearchingEngine.getPosition(rp);
                Toast.makeText(getApplicationContext(), apName, Toast.LENGTH_SHORT).show();
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss(); // dismiss the dialog
            }
        });
        d.show();
    }

    public void deleteRp() {
        Map<String, List<RssFingerprint>> database = datasource.getSortedFingerprints(mScanningEngine.getOrientation());
        int size = database.size();

        if (size == 0) {
            Toast.makeText(this, "No data in database", Toast.LENGTH_LONG).show();
            return;
        }

        final Dialog d = new Dialog(WifiNavigationActivity.this);

        d.setTitle("Choose your position");
        d.setContentView(R.layout.picker);

        Button b1 = (Button) d.findViewById(R.id.button1);
        Button b2 = (Button) d.findViewById(R.id.button2);

        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(size - 1);
        np.setMinValue(0);

        final String[] array = new String[size];
        int i = 0;

        for (String s : database.keySet()) {
            array[i] = s;
            i++;
        }

        np.setDisplayedValues(array);

        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                String rp = array[np.getValue()];
                if (datasource.deleteRP(rp)) {
                    Toast.makeText(getApplicationContext(), rp + "deleted", Toast.LENGTH_SHORT).show();
                } Toast.makeText(getApplicationContext(), rp + "failed to delete", Toast.LENGTH_SHORT).show();
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss(); // dismiss the dialog
            }
        });
        d.show();
    }

    public static RssDataSource getDataSource() {
        return datasource;
    }
}
