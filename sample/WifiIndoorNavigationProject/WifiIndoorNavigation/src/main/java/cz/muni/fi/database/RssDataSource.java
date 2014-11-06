package cz.muni.fi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.muni.fi.model.RssFingerprint;

/**
 * Created by Michal on 4.11.13.
 */
public class RssDataSource {

    private static final String TAG = "RssDataSource";
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allCollums = {
            MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_BSSID,
            MySQLiteHelper.COLUMN_ORIENTATION,
            MySQLiteHelper.COLUMN_RP,
            MySQLiteHelper.COLUMN_AVERAGE,
            MySQLiteHelper.COLUMN_DEVIATION};

    public RssDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        dbHelper.onCreate(database);
    }

    public void close() {
        dbHelper.close();
    }

    public void deleteDatabase() {
        dbHelper.dropDatabse(database);
        dbHelper.onCreate(database);
    }

    public RssFingerprint createRecord(RssFingerprint item) {

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_BSSID, item.getBssid());
        values.put(MySQLiteHelper.COLUMN_ORIENTATION, item.getOrientation());
        values.put(MySQLiteHelper.COLUMN_RP, item.getRp());
        values.put(MySQLiteHelper.COLUMN_AVERAGE, item.getAverage());
        values.put(MySQLiteHelper.COLUMN_DEVIATION, item.getDeviation());

        long insertRssId = database.insert(MySQLiteHelper.TABLE_RSSFINGERPRINT, null, values);
        Log.d(TAG, "inserting into RSS: " + insertRssId);

        Cursor cursor = database.query(MySQLiteHelper.TABLE_RSSFINGERPRINT, allCollums,
                MySQLiteHelper.COLUMN_ID + "=" + insertRssId, null, null, null, null);
        cursor.moveToFirst();
        RssFingerprint fingerPrint = cursorToFingerprint(cursor);
        cursor.close();
        return fingerPrint;
    }

    //BASIC SECTION

    public List<RssFingerprint> getAllFingerprints() {
        List<RssFingerprint> data = new ArrayList<RssFingerprint>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_RSSFINGERPRINT, allCollums,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            RssFingerprint item = cursorToFingerprint(cursor);
            data.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return data;
    }

    public Map<String, List<RssFingerprint>> getSortedFingerprints(int orientation) {
        Map<String, List<RssFingerprint>> data = doSort(orientation, MySQLiteHelper.TABLE_RSSFINGERPRINT);

        return data;
    }

    //MERGE SECTION

    public boolean merge(int orientation, int diff) {
        List<Integer> orientations = calculateOrientationWithDiff(orientation, diff);

        List<RssFingerprint> orientationFstList = getUnsortedFingerprints(orientations.get(0), MySQLiteHelper.TABLE_RSSFINGERPRINT);
        List<RssFingerprint> orientationSecList = getUnsortedFingerprints(orientations.get(1), MySQLiteHelper.TABLE_RSSFINGERPRINT);
        List<RssFingerprint> orientationThdList = getUnsortedFingerprints(orientations.get(2), MySQLiteHelper.TABLE_RSSFINGERPRINT);

        try {
            for (RssFingerprint fst : orientationFstList) {
                Log.d(TAG, "fst: " + fst.toString());

                int average = 0;
                int deviation = 0;
                boolean flag = false;

                for (RssFingerprint sec : orientationSecList) {
                    if (fst.getBssid().equals(sec.getBssid()) && fst.getRp().equals(sec.getRp())) {
                        Log.d(TAG, "sec: " + sec.toString());

                        average = fst.getAverage() + sec.getAverage();

                        for (RssFingerprint thd : orientationThdList) {
                            if (thd.getBssid().equals(sec.getBssid()) && thd.getRp().equals(sec.getRp())) {
                                Log.d(TAG, "thd: " + thd.toString());

                                average += thd.getAverage();
                                average /= 3;
                                deviation += Math.pow(thd.getAverage() - average, 2);
                                flag = true;
                            }
                        }

                        deviation += Math.pow(sec.getAverage() - average, 2);
                    }
                }

                if (flag) {
                    deviation += Math.pow(fst.getAverage() - average, 2);
                    deviation = (int) Math.sqrt(deviation / 3);
                    if (deviation == 0) deviation = 1;

                    RssFingerprint item = new RssFingerprint(fst.getBssid(), orientation, fst.getRp(), average, deviation);
                    ContentValues values = new ContentValues();
                    values.put(MySQLiteHelper.COLUMN_BSSID, item.getBssid());
                    values.put(MySQLiteHelper.COLUMN_ORIENTATION, item.getOrientation());
                    values.put(MySQLiteHelper.COLUMN_RP, item.getRp());
                    values.put(MySQLiteHelper.COLUMN_AVERAGE, item.getAverage());
                    values.put(MySQLiteHelper.COLUMN_DEVIATION, item.getDeviation());

                    long insertRssId = database.insert(MySQLiteHelper.TABLE_MERGE, null, values);

                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }


        return true;
    }

    public List<RssFingerprint> getAllMergedFingerprints() {
        List<RssFingerprint> data = new ArrayList<RssFingerprint>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_MERGE, allCollums,
                null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            RssFingerprint item = cursorToFingerprint(cursor);
            data.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return data;
    }

    public Map<String, List<RssFingerprint>> getSortedMergedFingerprints(int orientation) {
        Map<String, List<RssFingerprint>> data = doSort(orientation, MySQLiteHelper.TABLE_MERGE);

        return data;
    }

    //TOOLS

    private RssFingerprint cursorToFingerprint(Cursor cursor) {
        RssFingerprint item = new RssFingerprint();
        item.setBssid(cursor.getString(1));
        item.setOrientation(cursor.getInt(2));
        item.setRp(cursor.getString(3));
        item.setAverage(cursor.getInt(4));
        item.setDeviation(cursor.getInt(5));
        return item;
    }

    public List<Integer> calculateOrientationWithDiff(int orientation, int diff) {
        Set<Integer> orientationToMerge = new HashSet<Integer>();

        for (int i = 0; i <= 360; i += 45) {
            if (orientation == 0) {
                if (Math.abs(360 - i) <= diff || Math.abs(i - 360) <= diff) orientationToMerge.add(i);
            }

            if (Math.abs(orientation - i) <= diff || Math.abs(i - orientation) <= diff) {
                if (i == 360) orientationToMerge.add(0);
                    else orientationToMerge.add(i);
            }
        }

        List<Integer> tempList = new ArrayList<Integer>(orientationToMerge);
        if (tempList.contains(360)) tempList.remove(new Integer(360));

        return tempList;
    }

    public List<RssFingerprint> getUnsortedFingerprints(int orientation, String table) {
        String sql = "SELECT * FROM " + table + " WHERE orientation=" + orientation + ";";
        List<RssFingerprint> data = new ArrayList<RssFingerprint>();

        Cursor cursor = database.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            RssFingerprint item = cursorToFingerprint(cursor);
            data.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        return data;
    }

    public Map<String, List<RssFingerprint>> doSort(int orientation, String table) {
        Map<String, List<RssFingerprint>> data = new HashMap<String, List<RssFingerprint>>();
        List<RssFingerprint> tempList = getUnsortedFingerprints(orientation, table);

        for (RssFingerprint rss : tempList) {
            if (!data.containsKey(rss.getRp())) {
                List<RssFingerprint> tmp = new ArrayList<RssFingerprint>();
                tmp.add(rss);
                data.put(rss.getRp(), tmp);
            } else {
                List<RssFingerprint> tmp = data.get(rss.getRp());
                tmp.add(rss);
            }
        }
        return data;
    }

    public boolean deleteTable(String tableName) {
        try {
            database.execSQL("DELETE FROM " + tableName + ";");
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    public boolean deleteRP(String rp) {
        try {
            database.execSQL("DELETE FROM " + MySQLiteHelper.TABLE_RSSFINGERPRINT + " WHERE rp='" + rp +"';");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean contains(String rp) {
        String sql = "SELECT * FROM " + MySQLiteHelper.TABLE_RSSFINGERPRINT + " WHERE rp='" + rp + "';";
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor != null) return true;
        return false;
    }
}
