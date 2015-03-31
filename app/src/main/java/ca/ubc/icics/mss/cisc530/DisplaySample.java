package ca.ubc.icics.mss.cisc530;

import com.google.android.gms.maps.model.BitmapDescriptor;

/**
 * Created by Elitward on 15-03-13.
 */
public class DisplaySample extends  DataSample implements Comparable{
    //raw info from web server kept in Class DataSample

    //additional info
    BitmapDescriptor icon;

    DisplaySample(DataSample sample){
        this.name     = sample.name;
        this.details  = sample.details;
        this.value    = sample.value;
        this.time     = sample.time;
        this.location = sample.location;
        this.station  = sample.station;
        this.units    = sample.units;
    }

    @Override
    public int compareTo(Object another) {
        if(another instanceof DisplaySample) {
            DisplaySample that = (DisplaySample)another;
            return this.time.compareTo(that.time);
        }else{
            return 0;
        }
    }

    @Override
    public String toString() {
        return "DisplaySample{" +
                "name=" + name +
                " details=" + details +
                " value=" + value +
                " time=" + time +
                " location=" + location.longitude + "/" + location.latitude +
                " icon=" + icon +
                '}';
    }
}
