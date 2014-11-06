package cz.muni.fi.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.os.Handler;
import android.util.Log;

/**
 * Created by Michal on 10.1.14.
 */
public class WifiService extends IntentService {

    public final String TAG = "WifiService";
    Handler h = new Handler();

    public WifiService() {
        super("WifiService");
    }

    //binduju servicu z Main
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        repeatingScanning.run();
        return super.onStartCommand(intent, flags, startId);
    }

    Runnable repeatingScanning = new Runnable() {
        @Override
        public void run() {
            doScanning();
            h.postDelayed(repeatingScanning, 1000);
        }
    };

    public void doScanning() {
        /*List<RssFingerprint> list = WifiNavigationActivity.mScanningEngine.getScanResult();
        Intent intent = new Intent();
        intent.putExtra(TAG, list);
        intent.setAction(TAG);
        //Toast.makeText(this, "service", Toast.LENGTH_SHORT).show();
        getApplicationContext().sendBroadcast(intent);*/
    }
}
