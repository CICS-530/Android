package ca.ubc.icics.mss.cisc530;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Elitward on 15-03-13.
 */
public class BackgroundDownloader extends AsyncTask<Void, Integer, Boolean>{
    private String LOG_TAG = "BackgroundDownloaderLogTag";

    private ArrayList<DataSample> listSamples = new ArrayList<DataSample>();

    @Override
    protected Boolean doInBackground(Void... params) {
        Log.d(LOG_TAG, "doInBackground() with" + params);

        HttpJSONParser jsonParser = new HttpJSONParser();
        //jsonParser.setHttpURL("https://pollutantapi-aaroncheng.rhcloud.com/realtime/stationdata");
        jsonParser.setHttpURL("http://pollutantapi-aaroncheng.rhcloud.com/reading/latestData/1");
        String json = jsonParser.getJSonString();
        Log.d(LOG_TAG, "ELI: JSON-Test:" + json);

        listSamples.clear();
        parseJsonContent(json);

        for(int i=0; i<10; i++){
            //publishProgress(i);
        }
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

        try {
            JSONObject jObject = new JSONObject(json);

            String sta     = jObject.getString(STATION);
            double lng     = jObject.getDouble(LONGITUDE);
            double lat     = jObject.getDouble(LATITUDE);
            long   date    = jObject.getLong(DATE) * 1000;
            JSONArray chem = jObject.getJSONArray(CHEMICALS);

            for( int i=0; i<chem.length(); i++){
                JSONObject one = chem.getJSONObject(i);
                try{
                    String nam = one.getString(NAME);
                    double val = one.getDouble(VALUE);
                    String uni = one.getString(UNITS);
                    DataSample sample = new DataSample(sta, nam, null, val, uni, date, lat, lng);

                    listSamples.add(sample);
                    counter++;
                } catch (JSONException e){
                    continue;
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
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Log.d(LOG_TAG, "onProgressUpdate() with " + values[0]);
        super.onProgressUpdate(values);
    }
}
