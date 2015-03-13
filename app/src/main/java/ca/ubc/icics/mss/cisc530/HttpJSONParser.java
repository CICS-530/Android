package ca.ubc.icics.mss.cisc530;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Elitward on 15-03-13.
 */
public class HttpJSONParser {
    private String mJSonStr = null;

    public void setHttpURL(String url){
        DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
        //HttpPost httppost = new HttpPost(url);
        //httppost.setHeader("Content-type", "application/json");
        HttpGet httpget = new HttpGet(url);

        InputStream inputStream = null;
        String result = null;
        try {
            HttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
        }
        mJSonStr = result;
    }

    public String getJSonString(){
        return mJSonStr;
    }

}
