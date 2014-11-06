package cz.muni.fi.orientation;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by Michal on 2.12.13.
 */
public class CompassActivity extends Activity {
    private final String TAG = "CompassActivity";
    private SensorManager mSensorManager;
    private CompassView mView;
    private Compass mCompass;
    private SharedPreferences mSharedPrefs;
    private String mCompassType = null;


    public void setCompassType(String compassType) {
        mCompassType = compassType;
    }

    /**
     * Initialization of the Activity after it is first created. Must at least
     * call {@link android.app.Activity#setContentView setContentView()} to
     * describe what is to be displayed in the screen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        if (mCompassType == null)
            mCompassType = mSharedPrefs.getString("compass_type_preference", "default");
        mCompass = Compass.compassFactory(mCompassType, mSensorManager);
        mCompass.updatePreferences(mSharedPrefs);
        setContentView(mView);
    }


    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                mCompass.reset();

                break;
        }
        return true;
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
