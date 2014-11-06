package cz.muni.fi.wifinavigation;

import android.content.Context;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.muni.fi.model.RssFingerprint;

/**
 * Created by Michal on 20.12.13.
 */
public class ScanningEngine {

    private final String TAG = "ScanningEngine";
    private SensorManager mSensorManager;
    private Context mContext;

    public ScanningEngine(SensorManager mSensorManager, Context mContext) {
        this.mSensorManager = mSensorManager;
        this.mContext = mContext;
    }

    /**
     * This is used when we creates radio map
     * @param b
     * @return
     */
    public List<RssFingerprint> getScanResult(Button b) {
        return doScan(b, true);
    }

    public List<RssFingerprint> getScanResult() {
        return doScan(null, false);
    }

    public List<RssFingerprint> doScan(Button b, boolean scanWithTime) {
        WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> listScanResults = new ArrayList<ScanResult>();
        List<RssFingerprint> listRawRss = new ArrayList<RssFingerprint>();
        List<List<RssFingerprint>> listToOrder = new ArrayList<List<RssFingerprint>>();
        List<RssFingerprint> listToStore = new ArrayList<RssFingerprint>();

        String rp;
        if (b == null) rp = null;
            else rp = b.getText().toString();

        long start = System.currentTimeMillis();
        long end = start + 5 * 1000;

        if (scanWithTime) {
            while (System.currentTimeMillis() < end) {
                manager.startScan();
                listScanResults.addAll(manager.getScanResults());
            }
        } else {
            for (int j = 0; j < 100; j++) {
                manager.startScan();

                listScanResults.addAll(manager.getScanResults());
            }
        }

        //Log.d(TAG, "scan results: " + listScanResults);

        for (ScanResult sr : listScanResults) {
            /*if (sr.level >= -80) {
                RssFingerprint rss = new RssFingerprint(sr.BSSID, sr.level + 100, getOrientation(), rp, -1, -1);
                listRawRss.add(rss);
            }*/

            Set<String> set = new HashSet<>();
            set.add("f4:ec:38:bc:fb:ea");
            set.add("44:32:c8:34:8c:0d");
            set.add("00:80:48:55:c7:ae");
            set.add("00:80:48:67:64:e9");
            //set.add("02:15:6d:85:71:5b");
            //set.add("02:15:6d:85:71:5a");

            if (set.contains(sr.BSSID)) {
                RssFingerprint rss = new RssFingerprint(sr.BSSID, sr.level + 100, getOrientation(), rp, -1, -1);
                listRawRss.add(rss);
            }
        }

        //Log.d(TAG, "raw data (level >= 70): " + listRawRss);

        RssFingerprint tempRss = listRawRss.get(0);
        List<RssFingerprint> tempList = new ArrayList<RssFingerprint>();
        tempList.add(tempRss);
        listToOrder.add(tempList);
        listRawRss.remove(0);

        while (!listRawRss.isEmpty()) {
            boolean flag = true;
            RssFingerprint fingerprint = listRawRss.get(0);
            //Log.d(TAG, "fingerprint: " + fingerprint.toString());

            for (List<RssFingerprint> allLists : listToOrder) {
                //Log.d(TAG, "fingerprint to equals: " + allLists.get(0).toString());

                if (fingerprint.getBssid().equals(allLists.get(0).getBssid())) {
                    //Log.d(TAG, "podminka splnena");
                    allLists.add(fingerprint);
                    flag = false;
                }
            }

            if (flag) {
                listToOrder.add(new ArrayList<RssFingerprint>());

                for (List<RssFingerprint> lists : listToOrder) {
                    if (lists.isEmpty()) {
                        lists.add(fingerprint);
                    }

                }
            }
            //Log.d(TAG, "list to orded: " + listToOrder);
            //Log.d(TAG, "list to orded2: " + listToOrder);
            listRawRss.remove(fingerprint);
        }

        //Log.d(TAG, "raw data (should be empty: " + listRawRss);
        //Log.d(TAG, "sorted list: " + listToOrder);

        for (List<RssFingerprint> allLists : listToOrder) {
            int sum = 0;
            int count = 0;
            int deviation = 0;
            int average;

            for (RssFingerprint rss : allLists) {
                sum += rss.getLevel();
                count++;
            }

            average = sum / count;

            for (RssFingerprint rss : allLists) {
                deviation += Math.pow(rss.getLevel() - average, 2);
            }

            deviation = (int) Math.sqrt(deviation / count);

            if (deviation == 0) deviation = 1;

            listToStore.add(new RssFingerprint(allLists.get(0).getBssid(), allLists.get(0).getOrientation(), allLists.get(0).getRp(), average, deviation));
        }

        return listToStore;
    }

    public int getOrientation() {
        int orientation = -1;
        long heading;

        heading = Math.round(180 * WifiNavigationActivity.mCompass.getHeading() / Math.PI);

        if (heading >= 337 || heading < 22) orientation = 0;
        if (heading >= 22 && heading < 67) orientation = 45;
        if (heading >= 67 && heading < 112) orientation = 90;
        if (heading >= 112 && heading < 157) orientation = 135;
        if (heading >= 157 && heading < 202) orientation = 180;
        if (heading >= 202 && heading < 247) orientation = 225;
        if (heading >= 247 && heading < 292) orientation = 270;
        if (heading >= 292 && heading < 337) orientation = 315;

        return orientation;
    }
}
