package com.bopr.android.smailer;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.api.client.util.Key;

import static com.bopr.android.smailer.Database.COLUMN_LATITUDE;
import static com.bopr.android.smailer.Database.COLUMN_LONGITUDE;

/**
 * Geolocation coordinates.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class GeoCoordinates {

    @Key(COLUMN_LATITUDE)
    private double latitude;
    @Key(COLUMN_LONGITUDE)
    private double longitude;

    /* Required by Jackson */
    public GeoCoordinates() {
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
    @NonNull
    public String toString() {
        return "GeoCoordinates{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
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

}
