package com.gidtech.gidweather;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by gid on 5/11/2017.
 */

public class WContract {
    public static final String CONTENT_AUTHORITY = "com.gidtech.gidweather";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WEATHER = "weather";

    /* Inner class that defines the table contents of the weather table */
    public static final class WeatherEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the Weather table from the content provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_WEATHER)
                .build();

        /* Used internally as the name of our weather table. */
        public static final String TABLE_NAME = "weather";
        public static final String COLUMN_DATE = "date";

        /* Weather ID as returned by API, used to identify the icon to be used */
        public static final String COLUMN_WEATHER_ID = "weather_id";

        /* Min and max temperatures in °C for the day (stored as floats in the database) */
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";
        public static final String COLUMN_MORN_TEMP = "morn";
        public static final String COLUMN_DAY_TEMP = "day";
        public static final String COLUMN_EVE_TEMP = "eve";
        public static final String COLUMN_NIGHT_TEMP = "night";

        public static final String COLUMN_CITY = "name";
        public static final String COLUMN_COUNTRY = "country";

        /*Humidity is stored as a float representing percentage*/
        public static final String COLUMN_HUMIDITY = "humidity";

        //Pressure is stored as a float representing percentage
        public static final String COLUMN_PRESSURE = "pressure";

        //Wind speed is stored as a float representing wind speed in mph
        public static final String COLUMN_WIND_SPEED = "wind";

        /*
         * Degrees are meteorological degrees (e.g, 0 is north, 180 is south).
         * Stored as floats in the database.
         */
        public static final String COLUMN_DEGREES = "degrees";

        /**
         * @param date Normalized date in milliseconds
         * @return Uri to query details about a single weather entry
         */
        public static Uri buildWeatherUriWithDate(long date) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(date))
                    .build();
        }

        /**
         * Returns just the selection part of the weather query from a normalized today value.
         * This is used to get a weather forecast from today's date. To make this easy to use
         * in compound selection, we embed today's date as an argument in the query.
         *
         * @return The selection part of the weather query for today onwards
         */
        public static String getSqlSelectForTodayOnwards() {
            long normalizedUtcNow = WeatherDateUtils.normalizeDate(System.currentTimeMillis());
            return WContract.WeatherEntry.COLUMN_DATE + " >= " + normalizedUtcNow;
        }
    }
}
