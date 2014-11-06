package cz.muni.fi.wifinavigation;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;

import cz.muni.fi.orientation.Compass;
import cz.muni.fi.view.ScanningView;

/**
 * Created by Michal on 17.2.14.
 */
public class ScanningActivity extends Activity {

    private final String TAG = "ScanningActivity";
    private ScanningView mScanningView;
    private Compass mCompass;
    private ScanningEngine mScanningEngine;
    private Button b;
    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Bundle extras = getIntent().getExtras();
        String name = (String) extras.get("View");
        b = new Button(this);
        b.setText(name);

        mScanningEngine = WifiNavigationActivity.mScanningEngine;
        mScanningView = new ScanningView(this);
        mCompass = WifiNavigationActivity.mCompass;
        mScanningView.setCompass(mCompass);
        setContentView(mScanningView);
        mCompass.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        optionsMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.scanning_acbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Toast.makeText(this, "fachci", Toast.LENGTH_SHORT).show();
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setEnabled(false);
        switch (item.getItemId()) {
            case R.id.action_scan:
                String orientation = "" + mScanningEngine.getOrientation();
                if (WifiNavigationActivity.getScanResults(b, item)) {
                    Toast.makeText(this, "Succes, " + orientation, Toast.LENGTH_SHORT).show();

                }
                else {
                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();

                }


                item.setEnabled(true);
                return true;
            default:
                return true;
        }



    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        mCompass.start();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
        mCompass.stop();
    }
}
