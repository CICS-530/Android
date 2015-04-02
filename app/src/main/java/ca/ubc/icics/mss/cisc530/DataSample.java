package ca.ubc.icics.mss.cisc530;

import com.google.android.gms.maps.model.LatLng;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Elitward on 3/5/2015.
 */
public class DataSample implements Serializable{
    //raw info from web server
    String station;
    String name;
    String details;
    Double value;
    String units;
    Date   time;
    transient LatLng location;      //it's not Serializable but it is Parcelable

    public DataSample(){
    }

    public DataSample(String station, String name, String details, Double value, String units, Date time, LatLng location) {
        this.station = station;
        this.name = name;
        this.details = details;
        this.value = value;
        this.units = units;
        this.time = time;
        this.location = location;
    }

    public DataSample(String station, String name, String details, Double value, String units, long time, double lat, double lng) {
        this.station = station;
        this.name = name;
        this.details = details;
        this.value = value;
        this.units = units;
        this.time = new Date(time);
        this.location = new LatLng(lat, lng);
    }

    public static byte[] Seralize(DataSample sample) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(sample);
            out.writeDouble(sample.location.latitude);
            out.writeDouble(sample.location.longitude);
            out.close();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static DataSample Deseralize(byte[] array) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(array));
            DataSample sample = (DataSample) (in.readObject());
            double lat = in.readDouble();
            double lng = in.readDouble();
            sample.location = new LatLng(lat, lng);
            in.close();
            return sample;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}