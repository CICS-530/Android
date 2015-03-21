package ca.ubc.icics.mss.cisc530;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Elitward on 15-03-20.
 */

public class DbHelper extends SQLiteOpenHelper{

    private static final String DB_FILENAME = "sample.sql";
    private static final int    DB_VERSION  = 1;

    public static final String TABLE_NAME = "samples";
    public static final String COLUMN_NAME = "sample";

    public DbHelper(Context context) {
        super(context, DB_FILENAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME+ "(" + COLUMN_NAME + " BLOB not null);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
