package com.gidtech.gidweather.sync;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.text.format.DateUtils;

import com.gidtech.gidweather.Utils.NetUtils;
import com.gidtech.gidweather.Utils.NotificationUtils;
import com.gidtech.gidweather.Utils.OpenWeatherJsonUtils;
import com.gidtech.gidweather.WContract;
import com.gidtech.gidweather.data.WeatherPreferences;

import java.net.URL;

/**
 * Created by gid on 6/29/2017.
 */

public class SyncTask {

    synchronized public static void synCWeather(Context context){
        try{
            URL weatherRequestUrl = NetUtils.getUrl(context);

            /* Use the URL to retrieve the JSON */
            String jsonWeatherResponse = NetUtils.getResponseFromHttpUrl(weatherRequestUrl);

            /* Parse the JSON into a list of weather values */
            ContentValues[] weatherValues = OpenWeatherJsonUtils
                    .getWeatherContentValuesFromJson(context, jsonWeatherResponse);

            /*
             * In cases where our JSON contained an error code, getWeatherContentValuesFromJson
             * would have returned null. We need to check for those cases here to prevent any
             * NullPointerExceptions being thrown. We also have no reason to insert fresh data if
             * there isn't any to insert.
             */
            if (weatherValues != null && weatherValues.length != 0) {
                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver gidWContentResolver = context.getContentResolver();

                /* Delete old weather data because we don't need to keep multiple days' data */
                gidWContentResolver.delete(
                        WContract.WeatherEntry.CONTENT_URI,
                        null,
                        null);

                /* Insert our new weather data into Sunshine's ContentProvider */
                gidWContentResolver.bulkInsert(
                        WContract.WeatherEntry.CONTENT_URI,
                        weatherValues);

                /*
                 * Finally, after we insert data into the ContentProvider, determine whether or not
                 * we should notify the user that the weather has been refreshed.
                 */
                boolean notificationsEnabled = WeatherPreferences.areNotificationsEnabled(context);

                /*
                 * If the last notification was shown was more than 1 day ago, we want to send
                 * another notification to the user that the weather has been updated. Remember,
                 * it's important that you shouldn't spam your users with notifications.
                 */
                long timeSinceLastNotification = WeatherPreferences
                        .getEllapsedTimeSinceLastNotification(context);

                boolean oneDayPassedSinceLastNotification = false;

                if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
                    oneDayPassedSinceLastNotification = true;
                }

                /*
                 * We only want to show the notification if the user wants them shown and we
                 * haven't shown a notification in the past day.
                 */
                if (notificationsEnabled && oneDayPassedSinceLastNotification) {
                    NotificationUtils.notifyUserOfNewWeather(context);
                }

            /* If the code reaches this point, we have successfully performed our sync */

            }
        }catch (Exception e) {
            /* Server probably invalid */
            e.printStackTrace();
        }
    }
}
