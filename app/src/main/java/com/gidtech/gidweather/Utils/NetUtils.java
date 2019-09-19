package com.gidtech.gidweather.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.gidtech.gidweather.data.WeatherPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by gid on 6/29/2017.
 */

public class NetUtils {
    private static final String TAG = NetUtils.class.getSimpleName();
    private static final String STATIC_WEATHER_URL =
            "http://api.openweathermap.org/data/2.5/forecast/daily?";

    private static final String FORECAST_BASE_URL = STATIC_WEATHER_URL;
    /* The format we want our API to return */
    private static final String format = "json";
    /* The units we want our API to return */
    private static final String units = "metric";
    /* The number of days we want our API to return*/
    private static final int numDays = 5;
    /*OpenWeather API key*/
    private static final String key = "ea623ef65bf1a0d1249f1d3d4d8830dd";

    /* The query parameter allows us to provide a location string to the API */
    private static final String QUERY_PARAM = "q";

    private static final String LAT_PARAM = "lat";
    private static final String LON_PARAM = "lon";

    /* The format parameter allows us to designate whether we want JSON or XML from our API */
    private static final String FORMAT_PARAM = "mode";
    /* The units parameter allows us to designate whether we want metric units or imperial units */
    private static final String UNITS_PARAM = "units";
    /* The days parameter allows us to designate how many days of weather data we want */
    private static final String DAYS_PARAM = "cnt";
    /*OpenWeather API key parameter*/
    private static final String KEY_PARAM = "appid";

    public static URL getUrl(Context context) {
        if (WeatherPreferences.isLocationLatLonAvailable(context)) {
            double[] preferredCoordinates =WeatherPreferences.getLocationCoordinates(context);
            double latitude = preferredCoordinates[0];
            double longitude = preferredCoordinates[1];
            return buildUrlWithLatitudeLongitude(latitude, longitude);
        } else {
            String locationQuery = WeatherPreferences.getPreferredWeatherLocation(context);
            return buildUrlWithLocationQuery(locationQuery);
        }
    }

    private static URL buildUrlWithLatitudeLongitude(Double latitude, Double longitude) {
        Uri weatherQueryUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(LAT_PARAM, String.valueOf(latitude))
                .appendQueryParameter(LON_PARAM, String.valueOf(longitude))
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .appendQueryParameter(KEY_PARAM,key)
                .build();

        try {
            URL weatherQueryUrl = new URL(weatherQueryUri.toString());
            Log.v(TAG, "URL: " + weatherQueryUrl);
            return weatherQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static URL buildUrlWithLocationQuery(String locationQuery) {
        Uri weatherQueryUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, locationQuery)
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .appendQueryParameter(KEY_PARAM,key)
                .build();

        try {
            URL weatherQueryUrl = new URL(weatherQueryUri.toString());
            Log.v(TAG, "URL: " + weatherQueryUrl);
            return weatherQueryUrl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } finally {
            urlConnection.disconnect();
        }
    }
}
