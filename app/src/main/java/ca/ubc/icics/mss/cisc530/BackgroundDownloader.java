package ca.ubc.icics.mss.cisc530;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by Elitward on 15-03-13.
 */
public class BackgroundDownloader extends AsyncTask<Void, Integer, Boolean>{
    private String LOG_TAG = "BackgroundDownloaderLogTag";

    @Override
    protected Boolean doInBackground(Void... params) {
        Log.d(LOG_TAG, "doInBackground() with" + params);

        HttpJSONParser jsonParser = new HttpJSONParser();
        jsonParser.setHttpURL("http://pollutantapi-aaroncheng.rhcloud.com/category/");
        String json = jsonParser.getJSonString();
        Log.d(LOG_TAG, "ELI: JSON-Test:" + json);

        for(int i=0; i<10; i++){
            publishProgress(i);
        }
        return null;
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
