package cz.muni.fi.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.util.Log;

/**
 * Created by Michal on 4.11.13.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TAG = "MySQLiteHelper";
    public static final String DATABASE_NAME = "rssfingerprint.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_MERGE = "merge";
    public static final String TABLE_RSSFINGERPRINT = "rssfingerprint";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_BSSID = "bssid";
    public static final String COLUMN_ORIENTATION = "orientation";
    public static final String COLUMN_RP = "rp";
    public static final String COLUMN_AVERAGE = "average";
    public static final String COLUMN_DEVIATION = "deviation";

    public static final String DATABASE_CREATE_RSS = "create table if not exists "
            + TABLE_RSSFINGERPRINT + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_BSSID + " text not null, "
            + COLUMN_ORIENTATION + " integer, "
            + COLUMN_RP + " text not null, "
            + COLUMN_AVERAGE + " integer, "
            + COLUMN_DEVIATION + " integer);";

    public static final String DATABASE_CREATE_MERGE = "create table if not exists "
            + TABLE_MERGE + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_BSSID + " text not null, "
            + COLUMN_ORIENTATION + " integer, "
            + COLUMN_RP + " text not null, "
            + COLUMN_AVERAGE + " integer, "
            + COLUMN_DEVIATION + " integer);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "construc");
    }

    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_RSS);
        database.execSQL(DATABASE_CREATE_MERGE);
        Log.d(TAG, "onCreate, database.exec");
    }

    public void dropDatabse(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS '" + TABLE_RSSFINGERPRINT + "';");
        database.execSQL("DROP TABLE IF EXISTS '" + TABLE_MERGE + "';");
        Log.d(TAG, "dropDatabase");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RSSFINGERPRINT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MERGE);
        onCreate(db);
        Log.d(TAG, "onUpgrade");
    }
}
