package ca.ubc.icics.mss.cisc530;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Elitward on 15-03-13.
 */
public class BackgroundDownloader extends AsyncTask<Void, Integer, Boolean>{
    private String LOG_TAG = "BackgroundDownloaderLogTag";
    private long OVERLAP_TIME = 2*60*60*1000; //two hours in ms

    private DbManager dbManager = null;
    private OnTaskCompleted  completeCallback = null;
    private OnTaskInProgress progressCallback = null;

    private ArrayList<DataSample> listSamples = new ArrayList<DataSample>();
    Date lastDate;


    public BackgroundDownloader setDatabaseManager(DbManager database){
        dbManager = database;
        return this;
    }

    public BackgroundDownloader setCompletedCallbackListener(OnTaskCompleted onTaskCompleted){
        completeCallback = onTaskCompleted;
        return this;
    }

    public BackgroundDownloader setInProgressCallbackListener(OnTaskInProgress onTaskInProgress){
        progressCallback = onTaskInProgress;
        return this;
    }

    public interface OnTaskCompleted{
        void OnTaskCompleted();
    }

    public interface OnTaskInProgress{
        void OnTaskInProgress(int cur, int max);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        //final String url = "http://pollutantapi-aaroncheng.rhcloud.com/reading/dataDate/[id]/[dayOffset]";
        final String url = "http://pollutantapi-aaroncheng.rhcloud.com/reading/dataDate/";
        final int idMax = 23;
        Log.d(LOG_TAG, "doInBackground() with" + params);

        //if(dbManager!=null){
        //    lastDate = dbManager.getLastDate();
        //    Log.d(LOG_TAG, "doInBackground() lastDate=" + lastDate);
        //}else{
        //    lastDate = null;
        //}

        HttpJSONParser jsonParser = new HttpJSONParser();
        //jsonParser.setHttpURL("https://pollutantapi-aaroncheng.rhcloud.com/realtime/stationdata");
        //jsonParser.setHttpURL("http://pollutantapi-aaroncheng.rhcloud.com/reading/latestData/1");
        listSamples.clear();

        for(int id=1; id<=idMax;id++){
            boolean na = true;  //assume not available, unless get same data
            for(int day=0; ; day++){
                jsonParser.setHttpURL(url + "/" + id + "/" + day);
                String json = jsonParser.getJSonString();
                Log.d(LOG_TAG, "ELI: JSON-Text " + id + "-" + day + ":" + json);

                if(json!=null) {
                    int added = parseJsonContent(json);
                    Log.d(LOG_TAG, "ELI: JSON-Added " + id + "-" + day + ":" + added);
                    if( added<0 ){
                        break;
                    }else if(added==0){
                        na = false;
                        break;  //good
                    }else{
                        na = false;
                    }
                }else{
                    break;
                }
            }

            if(na){
                break;
            }

            publishProgress(id, idMax);
        }

        //for(int i=0; i<10; i++){
        //    publishProgress(i);
        //}
        return null;
    }

    private int parseJsonContent(String json){
        int counter = 0;
        final String STATION   = "station";
        final String LONGITUDE = "longitude";
        final String LATITUDE  = "latitude";
        final String DATE      = "date";
        final String CHEMICALS = "Chemicals";
        final String NAME      = "name";
        final String VALUE     = "value";
        final String UNITS     = "units";

        JSONArray  jArr = null;
        JSONObject jObj = null;
        if(json.startsWith("[")){   //this is an array
            try {
                jArr = new JSONArray(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
       }else{ //this is NOT an array
            try {
                jObj = new JSONObject(json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(jArr==null && jObj==null){
            return -1;
        }

        try {
            for(int a=0; (jObj!=null && a==0) || (jArr!=null && a<jArr.length()); a++){
                JSONObject tmp = null;

                if(jObj!=null){
                    tmp = jObj;
                }else if(jArr!=null) {
                    tmp = jArr.getJSONObject(a);
                }else{
                    break;
                }

                String sta     = tmp.getString(STATION);
                double lng     = tmp.getDouble(LONGITUDE);
                double lat     = tmp.getDouble(LATITUDE);
                long   date    = tmp.getLong(DATE) * 1000;
                JSONArray chem = tmp.getJSONArray(CHEMICALS);

                for( int i=0; i<chem.length(); i++){
                    JSONObject one = chem.getJSONObject(i);
                    DataSample sample = null;
                    try{
                        String nam = one.getString(NAME);
                        double val = one.getDouble(VALUE);
                        String uni = one.getString(UNITS);
                        sample = new DataSample(sta, nam, null, val, uni, date, lat, lng);

                        Log.d(LOG_TAG, "ELI: sample " + sample);
                        listSamples.add(sample);
                    } catch (JSONException e){
                        continue;
                    }

                    //save into database
                    if(sample!=null) {
                        if(lastDate!=null){
                            if(sample.time.getTime()-OVERLAP_TIME<lastDate.getTime()){
                                Log.d(LOG_TAG, "Skip sample: last=" + lastDate + ", sample=" + sample.time);
                                continue;   //skip this sample
                            }
                        }
                        if( dbManager.add(sample) ){
                            counter++;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }

        return counter;
    }

    @Override
    protected void onPreExecute() {
        Log.d(LOG_TAG, "onPreExecute()");
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        Log.d(LOG_TAG, "onPostExecute() with " + aBoolean);
        super.onPostExecute(aBoolean);
        if(completeCallback !=null) {
            completeCallback.OnTaskCompleted();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Log.d(LOG_TAG, "onProgressUpdate() with " + values[0]);
        super.onProgressUpdate(values);
        if(progressCallback!=null){
            progressCallback.OnTaskInProgress(values[0], values[1]);
        }
    }
}
