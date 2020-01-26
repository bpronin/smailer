package com.bopr.android.smailer;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.core.util.ObjectsCompat;

/**
 * Geolocation coordinates.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class GeoCoordinates implements Parcelable {

    private double latitude;
    private double longitude;

    public GeoCoordinates() {
        /* Default constructor required by Jackson */
    }

    public GeoCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GeoCoordinates(Location location) {
        this(location.getLatitude(), location.getLongitude());
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        GeoCoordinates that = (GeoCoordinates) o;
        return Double.compare(that.latitude, latitude) == 0 && Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return ObjectsCompat.hash(latitude, longitude);
    }

    @Override
    @NonNull
    public String toString() {
        return "GeoCoordinates{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    /* Generated Parcelable stuff. Alt+Enter on "implements Parcelable" to update */

    protected GeoCoordinates(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GeoCoordinates> CREATOR = new Creator<GeoCoordinates>() {
        @Override
        public GeoCoordinates createFromParcel(Parcel in) {
            return new GeoCoordinates(in);
        }

        @Override
        public GeoCoordinates[] newArray(int size) {
            return new GeoCoordinates[size];
        }
    };


}
