package ca.ubc.icics.mss.cisc530;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Elitward on 15-03-20.
 */
public class DbManager {

    private final String LOG_TAG = "DbHelperLogTag";

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
        //byte[] buf = DataSample.Seralize(sample);
        //cv.put(DbHelper.COLUMN_NAME, buf);
        for(DataSampleDef def: DataSampleDef.values()){
            switch (def){
                case STATION:   cv.put(DbHelper.getDataSampleColumnName(def), sample.station);              break;
                case NAME:      cv.put(DbHelper.getDataSampleColumnName(def), sample.name);                 break;
                case DETAILS:   cv.put(DbHelper.getDataSampleColumnName(def), sample.details);              break;
                case VALUE:     cv.put(DbHelper.getDataSampleColumnName(def), sample.value);                break;
                case UNITS:     cv.put(DbHelper.getDataSampleColumnName(def), sample.units);                break;
                case TIME:      cv.put(DbHelper.getDataSampleColumnName(def), sample.time.getTime());       break;
                case LATITUDE:  cv.put(DbHelper.getDataSampleColumnName(def), sample.location.latitude);    break;
                case LONGITUDE: cv.put(DbHelper.getDataSampleColumnName(def), sample.location.longitude);   break;
            }
        }
        //long ret = db.insert(DbHelper.TABLE_NAME, null, cv);
        long ret = -1;
        try {
            ret = db.insertOrThrow(DbHelper.TABLE_NAME, null, cv);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(ret>=0){
            return true;
        }else{
            return false;
        }
    }

    public DataSample[] get(String name){
        ArrayList<DataSample> list = new ArrayList<DataSample>();
        String selection = null;
        String[] selectionArgs = null;
        if(name!=null){
            selection = DbHelper.getDataSampleColumnName(DataSampleDef.NAME) + "=?";
            selectionArgs = new String[]{name};
        }
        Cursor c = db.query(DbHelper.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        while(c.moveToNext()){
            //byte[] array = c.getBlob(0);
            //DataSample sample = DataSample.Deseralize(array);
            DataSample sample = new DataSample();
            int col=0;
            double lng=0, lat=0;
            for(DataSampleDef def: DataSampleDef.values()){
                switch (def){
                    case STATION:   sample.station = c.getString(col++);        break;
                    case NAME:      sample.name    = c.getString(col++);        break;
                    case DETAILS:   sample.details = c.getString(col++);        break;
                    case VALUE:     sample.value   = c.getDouble(col++);        break;
                    case UNITS:     sample.units   = c.getString(col++);        break;
                    case TIME:      sample.time    = new Date(c.getLong(col++));break;
                    case LATITUDE:  lat = c.getDouble(col++);                   break;
                    case LONGITUDE: lng = c.getDouble(col++);                   break;
                }
            }
            if(lng!=0 && lat!=0){
                sample.location = new LatLng(lat, lng);
            }
            list.add(sample);
        }
        c.close();

        if(list.size()>0){
            DataSample[] array = new DataSample[list.size()];
            return list.toArray(array);
        }else{
            return null;
        }
    }

    public String[] getNames(){
        String name_col = DbHelper.getDataSampleColumnName(DataSampleDef.NAME);
        String[] columns = {name_col};
        Cursor c = db.query(DbHelper.TABLE_NAME, columns, null, null, name_col, null, null);
        ArrayList<String> names = new ArrayList<String>();
        while(c.moveToNext()){
            String one = c.getString(0);
            names.add(one);
        }
        c.close();
        if(names.size()>0){
            String[] array = new String[names.size()];
            return names.toArray(array);
        }else{
            return null;
        }
    }

    public Date getLastDate(){
        //"SELECT MAX(date) FROM table_name"
        String sql = "SELECT MAX(" +
                DbHelper.getDataSampleColumnName(DataSampleDef.TIME) +
                ") FROM " +
                DbHelper.TABLE_NAME +
                ";";
        Cursor c = db.rawQuery(sql, null);
        if(c!=null && c.getCount()>0) {
            if(c.moveToFirst()){
                long l = c.getLong(0);
                if(l>0) {
                    Date time = new Date();
                    return time;
                }
            }
            c.close();
        }
        return null;
    }

    public void clear(){
        db.delete(DbHelper.TABLE_NAME, null, null);
    }
}
