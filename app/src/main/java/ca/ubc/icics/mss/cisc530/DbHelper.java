package ca.ubc.icics.mss.cisc530;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Elitward on 15-03-20.
 */

public class DbHelper extends SQLiteOpenHelper{

    private final String LOG_TAG = "DbHelperLogTag";

    private static final String DB_FILENAME = "sample.sql";
    private static final int    DB_VERSION  = 1;

    public static final String TABLE_NAME = "samples";
    //public static final String COLUMN_NAME = "sample";

    public DbHelper(Context context) {
        super(context, DB_FILENAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //SQL:
        // CREATE TABLE samples (
        //  station TEXT,
        //  name TEXT,
        //  details TEXT,
        //  value REAL,
        //  units TEXT,
        //  time INTEGER,
        //  latitude REAL,
        //  longitude REAL,
        //  PRIMARY KEY (latitude, longitude, time, name)
        // );
        //String sql = "CREATE TABLE " + TABLE_NAME+ "(" + COLUMN_NAME + " BLOB not null);";

        String sql = "CREATE TABLE " + TABLE_NAME+ "(" ;
        for(DataSampleDef def : DataSampleDef.values()){
            sql += getDataSampleColumnName(def) + " " + getDataSampleColumnType(def) + ", ";
        }
        sql+= "PRIMARY KEY (" +
                getDataSampleColumnName(DataSampleDef.LATITUDE)   + ", " +
                getDataSampleColumnName(DataSampleDef.LONGITUDE)  + ", " +
                getDataSampleColumnName(DataSampleDef.TIME)       + ", " +
                getDataSampleColumnName(DataSampleDef.NAME) + ")";
        sql+= ");";

        Log.d(LOG_TAG, sql);

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    static public String getDataSampleColumnName(DataSampleDef sample){
        switch (sample){
            case STATION:   return "STATION";
            case NAME:      return "NAME";
            case DETAILS:   return "DETAILS";
            case VALUE:     return "VALUE";
            case UNITS:     return "UNITS";
            case TIME:      return "TIME";
            case LATITUDE:  return "LATITUDE";
            case LONGITUDE: return "LONGITUDE";
        }
        return null;
    }

    static public String getDataSampleColumnType(DataSampleDef sample){
        switch (sample){
            case STATION:   return "TEXT";
            case NAME:      return "TEXT";
            case DETAILS:   return "TEXT";
            case VALUE:     return "REAL";
            case UNITS:     return "TEXT";
            case TIME:      return "INTEGER";
            case LATITUDE:  return "REAL";
            case LONGITUDE: return "REAL";
        }
        return null;
    }
}
