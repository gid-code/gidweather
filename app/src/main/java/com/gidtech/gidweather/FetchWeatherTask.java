package com.gidtech.gidweather;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * Created by gid on 5/25/2017.
 */

public class FetchWeatherTask extends AsyncTask<String,Void,String[]> {
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    public Context mContext;


    private String getReadableDateString(long time){
        //Cos the api returns a unix timestamp
        //Date date = new Date(time * 1000);
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE, MMM dd");
        return shortenedDateFormat.format(time);
    }

        /*private String formatHighLows(double high,double low){
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;

        }*/



    public String[]  getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {
        //Names of JSON objects that needs to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_WEATHER_ID = "id";
        final String OWM_DESCRIPTION = "main";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        ContentValues[] weatherContentValues = new ContentValues[weatherArray.length()];
        //Time dayTime = new Time();
        //dayTime.setToNow();

        //Start at the day returned by local time
        //int julianStartDay = Time.getJulianDay(System.currentTimeMillis(),dayTime.gmtoff);

        //working in UTC
        //dayTime = new Time();

        long normalizedUtcStartDay = WeatherDateUtils.getNormalizedUtcDateForToday();

        String[] resultStrs = new String[numDays];
        for(int i=0;i < weatherArray.length();i++){
            //String day;
            long dateTimeMillis;
            int weatherId;
            //String highAndLow;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            //Get JSON Object representing day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            //date/time is returned as long
            //final long dateTime;
            //dateTime = dayTime.setJulianDay(julianStartDay+i);
            //day = getReadableDateString(dateTime);
            //day = WeatherDateUtils.getFriendlyDateString(mContext,dateTime,false);

            dateTimeMillis = normalizedUtcStartDay + WeatherDateUtils.DAY_IN_MILLIS * i;

            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            //
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);
            String description = weatherObject.getString(OWM_DESCRIPTION);

            //
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            //highAndLow = formatHighLows(high,low);
            resultStrs[i] = dateTimeMillis + " - "+ description;// + " - "+ highAndLow;

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WContract.WeatherEntry.COLUMN_DATE, dateTimeMillis);
            weatherValues.put(WContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            weatherContentValues[i] = weatherValues;


        }

        if (weatherContentValues != null && weatherContentValues.length != 0) {
                /* Get a handle on the ContentResolver to delete and insert data */
            ContentResolver gidContentResolver = mContext.getContentResolver();

                /* Delete old weather data because we don't need to keep multiple days' data */
            gidContentResolver.delete(
                    WContract.WeatherEntry.CONTENT_URI,
                    null,
                    null);

                /* Insert our new weather data into Sunshine's ContentProvider */
            gidContentResolver.bulkInsert(
                    WContract.WeatherEntry.CONTENT_URI,
                    weatherContentValues);
        }

        for (String s : resultStrs){
            Log.v(LOG_TAG,"Forecast entry: "+s);
        }
        return resultStrs;
        //return weatherContentValues;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String[] result) {
        //showWeatherDataView();
        //super.onPostExecute();
    }

    @Override
    protected String[] doInBackground(String... params) {

        //showLoading();
        if(params.length == 0){
            return null;
        }
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        //Will contain the raw Json response as string
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        String key = "ea623ef65bf1a0d1249f1d3d4d8830dd";
        int numDays = 7;


        try{
            //Construct the URL for the OpenWeather Query
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNIT_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APIKEY = "appid";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM,params[0])
                    .appendQueryParameter(FORMAT_PARAM,format)
                    .appendQueryParameter(UNIT_PARAM,units)
                    .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                    .appendQueryParameter(APIKEY,key)
                    .build();
            URL url = new URL(builtUri.toString());
            Log.v(LOG_TAG,"Built URI"+builtUri.toString());

            //Create URL request
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Read input stream to a string
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream ==  null){
                //Nothing to do
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while((line = reader.readLine()) != null){
                //Since
                buffer.append(line+"\n");
            }

            if(buffer.length() == 0) {
                return null;
            }
            forecastJsonStr = buffer.toString();

            // Log.v(LOG_TAG,"Forecast JSON String:"+forecastJsonStr);
        }catch (IOException e){
            Log.e(LOG_TAG,"Error ",e);
            return null;
        }finally{
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(reader != null){
                try{
                    reader.close();
                }catch (final IOException e){
                    Log.e(LOG_TAG,"Error closing stream",e);
                }
            }
        }
        try{
            return getWeatherDataFromJson(forecastJsonStr,numDays);
        }catch(JSONException e){
            Log.e(LOG_TAG,e.getMessage(),e);
            e.printStackTrace();
        }
        return null;
    }
}
