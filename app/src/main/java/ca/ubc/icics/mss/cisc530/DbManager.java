package ca.ubc.icics.mss.cisc530;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Elitward on 15-03-20.
 */
public class DbManager {

    private static DbManager instance = null;

    private DbHelper helper;
    private SQLiteDatabase db;

    private DbManager(Context context){
        helper = new DbHelper(context);
        db = helper.getWritableDatabase();
    }

    public static DbManager getInstance(Context context){
        if(instance==null){
            instance = new DbManager(context);
        }
        return instance;
    }

    public boolean add(DataSample sample){
        ContentValues cv = new ContentValues();
        byte[] buf = DataSample.seralize(sample);
        cv.put(DbHelper.COLUMN_NAME, buf);
        long ret = db.insert(DbHelper.TABLE_NAME, null, cv);
        if(ret>=0){
            return true;
        }else{
            return false;
        }
    }

    public DataSample[] get(){
        ArrayList<DataSample> list = new ArrayList<DataSample>();
        String[] columns = {DbHelper.COLUMN_NAME};
        Cursor c = db.query(DbHelper.TABLE_NAME, columns, null, null, null, null, null);
        while(c.moveToNext()){
            byte[] array = c.getBlob(0);
            DataSample sample = DataSample.deseralize(array);
            list.add(sample);
        }

        if(list.size()>0){
            DataSample[] array = new DataSample[list.size()];
            return list.toArray(array);
        }else{
            return null;
        }
    }

    public void clear(){
        db.delete(DbHelper.TABLE_NAME, null, null);
    }
}
