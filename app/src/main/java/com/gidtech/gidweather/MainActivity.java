package com.gidtech.gidweather;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.gidtech.gidweather.Utils.GPS;
import com.gidtech.gidweather.WeatherDateUtils;
import com.gidtech.gidweather.data.WeatherPreferences;
import com.gidtech.gidweather.sync.SyncIntentService;
import com.gidtech.gidweather.sync.SyncTask;
import com.gidtech.gidweather.sync.SyncUtils;

import static com.gidtech.gidweather.WContract.BASE_CONTENT_URI;
import static com.gidtech.gidweather.WContract.PATH_WEATHER;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
ForecastAdapter.ForecastAdapterOnClickHandler{

    public boolean sInitialized;
    GPS gps;
    public double lat,lon;

    private TextView date,weather_description,high_temperature,
            low_temperature,humidityTv,pressureTv,
            wind_measurementTv,humidityLabel,pressureLabel,windLabel;
    private ImageView weather_icon;
    private static final String FORECAST_SHARE_HASHTAG = " #GidWeatherApp";
    private ProgressBar mLoadingIndicator;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int ID_FORECAST_LOADER = 14;

    private ForecastAdapter mForecastAdapter;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;

    //private ArrayAdapter<String> mForecastAdapter;
    private Context mContext;

    public static final String[] MAIN_FORECAST_PROJECTION = {
            WContract.WeatherEntry.COLUMN_DATE,
            WContract.WeatherEntry.COLUMN_MAX_TEMP,
            WContract.WeatherEntry.COLUMN_MIN_TEMP,
            WContract.WeatherEntry.COLUMN_CITY,
            WContract.WeatherEntry.COLUMN_COUNTRY,
            WContract.WeatherEntry.COLUMN_MORN_TEMP,
            WContract.WeatherEntry.COLUMN_DAY_TEMP,
            WContract.WeatherEntry.COLUMN_EVE_TEMP,
            WContract.WeatherEntry.COLUMN_NIGHT_TEMP,
            WContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CITY = 3;
    public static final int INDEX_WEATHER_COUNTRY = 4;
    public static final int INDEX_WEATHER_MORN_TEMP = 5;
    public static final int INDEX_WEATHER_DAY_TEMP = 6;
    public static final int INDEX_WEATHER_EVE_TEMP = 7;
    public static final int INDEX_WEATHER_NIGHT_TEMP = 8;
    public static final int INDEX_WEATHER_CONDITION_ID = 9;

    private static final int ID_DETAIL_LOADER = 353;

    /* A summary of the forecast that can be shared by clicking the share button in the ActionBar */
    private String mForecastSummary;

    /* The URI that is used to access the chosen day's weather details */
    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        getSupportActionBar().setElevation(0f);



        /*gps = new GPS(this);
        if(gps.canGetLocation()){
            lat = gps.getLatitude();
            lon = gps.getLongitude();
        }

        if(lat != 0 && lon != 0) {
            WeatherPreferences.setLocationDetails(this, lat, lon);
        }*/

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        /*date = (TextView)findViewById(R.id.date);
        weather_description =(TextView)findViewById(R.id.weather_description);
        high_temperature = (TextView)findViewById(R.id.high_temperature);
        low_temperature = (TextView)findViewById(R.id.low_temperature);
        //humidityTv = (TextView)findViewById(R.id.humidity);
        //pressureTv = (TextView)findViewById(R.id.pressure);
        wind_measurementTv = (TextView)findViewById(R.id.wind_measurement);
        weather_icon = (ImageView)findViewById(R.id.weather_icon);
        humidityLabel = (TextView)findViewById(R.id.humidity_label);
        pressureLabel = (TextView)findViewById(R.id.pressure_label);
        windLabel = (TextView)findViewById(R.id.wind_label);*/

        /*Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date = cal.get(Calendar.DATE);
        cal.clear();
        cal.set(year,month,date);
        long todayMillis = cal.getTimeInMillis();*/

        long todayMillis = WeatherDateUtils.getNormalizedUtcDateForToday();

        mUri = WContract.WeatherEntry.buildWeatherUriWithDate(todayMillis);
        //mUri = getIntent().getData();
        if (mUri == null) throw new NullPointerException("URI for DetailActivity cannot be null");

        //syncWeather();
        /* This connects our Activity into the loader lifecycle. */
        getSupportLoaderManager().initLoader(ID_FORECAST_LOADER, null, this);

        //mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);
        //mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        //mRecyclerView.setHasFixedSize(true);

        mForecastAdapter = new ForecastAdapter(this,this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mForecastAdapter);

        //FetchWeatherTask weatherTask = new FetchWeatherTask();
        //weatherTask.execute("94043");

        showLoading();

        getSupportLoaderManager().initLoader(ID_FORECAST_LOADER, null, this);

        /*showLoading();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }*/
        //syncWeather();


        /*String [] forecastArray = {
                "Today - Sunny - 88/63",
                "Tomorrow - Foggy - 78/40",
                "Weds - Cloudy - 72/63",
                "Thurs - Asteroids - 75/65",
                "Fri - Heavy Rain - 65/56",
                "Sat - Heavy Storm - 60/51",
                "Sun - Sunny - 80/68"
        };

        List<String> weekForecast = new ArrayList<String>(
                Arrays.asList(forecastArray)
        );

        mForecastAdapter = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecast);

        ListView listView = (ListView) findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);*/

        SyncUtils.initialize(this);


    }

    /**
     * Called by the {@link android.support.v4.app.LoaderManagerImpl} when a new Loader needs to be
     * created. This Activity only uses one loader, so we don't necessarily NEED to check the
     * loaderId, but this is certainly best practice.
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param bundle   Any arguments supplied by the caller
     * @return A new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {


        switch (loaderId) {

            case ID_FORECAST_LOADER:
                /* URI for all rows of weather data in our weather table */
                Uri forecastQueryUri = WContract.WeatherEntry.CONTENT_URI;
                /* Sort order: Ascending by date */
                String sortOrder = WContract.WeatherEntry.COLUMN_DATE + " ASC";
                /*
                 * A SELECTION in SQL declares which rows you'd like to return. In our case, we
                 * want all weather data from today onwards that is stored in our weather table.
                 * We created a handy method to do that in our WeatherEntry class.
                 */
                String selection = WContract.WeatherEntry.getSqlSelectForTodayOnwards();
                //ZonedDateTime
                //Long today =

                return new CursorLoader(this,
                        forecastQueryUri,
                        //mUri,
                        MAIN_FORECAST_PROJECTION,
                        selection,
                        null,
                        //null,
                        sortOrder
                );

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    /**
     * Called when a Loader has finished loading its data.
     *
     * NOTE: There is one small bug in this code. If no data is present in the cursor do to an
     * initial load being performed with no access to internet, the loading indicator will show
     * indefinitely, until data is present from the ContentProvider. This will be fixed in a
     * future version of the course.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) showWeatherDataView();
    }

    /**
     * Called when a previously created loader is being reset, and thus making its data unavailable.
     * The application should at this point remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        /*
         * Since this Loader's data is now invalid, we need to clear the Adapter that is
         * displaying the data.
         */
        mForecastAdapter.swapCursor(null);
    }

    /*private void showWeatherDataView() {
        /* First, hide the loading indicator
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* Finally, make sure the weather data is visible
        mRecyclerView.setVisibility(View.VISIBLE);
    }*/

    private void showLoading() {
        // Then, hide the weather data
        mRecyclerView.setVisibility(View.INVISIBLE);
        // Finally, show the loading indicator
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }


    public void onClick(long date) {
        Intent weatherDetailIntent = new Intent(MainActivity.this, DetailActivity.class);
        Uri uriForDateClicked = WContract.WeatherEntry.buildWeatherUriWithDate(date);
        weatherDetailIntent.setData(uriForDateClicked);
        startActivity(weatherDetailIntent);
    }

    /*public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

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

        }



        private String[]  getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException{
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

                if (weatherContentValues != null && weatherContentValues.length != 0) {
                 Get a handle on the ContentResolver to delete and insert data
                    ContentResolver gidContentResolver = getApplicationContext().getContentResolver();

                //Delete old weather data because we don't need to keep multiple days' data
                    gidContentResolver.delete(
                            WContract.WeatherEntry.CONTENT_URI,
                            null,
                            null);

                //Insert our new weather data into Sunshine's ContentProvider
                    gidContentResolver.bulkInsert(
                            WContract.WeatherEntry.CONTENT_URI,
                            weatherContentValues);
                }
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
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.forecast,menu);
        //return super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        int id = item.getItemId();

        if(id == R.id.action_refresh){
            //FetchWeatherTask weatherTask = new FetchWeatherTask();
            //weatherTask.execute("Accra");
            showLoading();
            SyncUtils.startImmediateSync(this);
            showWeatherDataView();
            return true;
        }

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void syncWeather(){
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.execute("Accra");
    }

    private void showWeatherDataView() {
        /* First, hide the loading indicator */
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* Finally, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    public void initialize(){
        if(sInitialized) return;
        sInitialized = true;

        Thread checkForEmpty = new Thread(new Runnable() {
            @Override
            public void run() {

                /* URI for every row of weather data in our weather table*/
                Uri forecastQueryUri = WContract.WeatherEntry.CONTENT_URI;

                /*
                 * Since this query is going to be used only as a check to see if we have any
                 * data (rather than to display data), we just need to PROJECT the ID of each
                 * row. In our queries where we display data, we need to PROJECT more columns
                 * to determine what weather details need to be displayed.
                 */
                String[] projectionColumns = {WContract.WeatherEntry._ID};
                String selectionStatement = WContract.WeatherEntry
                        .getSqlSelectForTodayOnwards();

                /* Here, we perform the query to check to see if we have any weather data */
                Cursor cursor = getApplicationContext().getContentResolver().query(
                        forecastQueryUri,
                        projectionColumns,
                        selectionStatement,
                        null,
                        null);
                /*
                 * A Cursor object can be null for various different reasons. A few are
                 * listed below.
                 *
                 *   1) Invalid URI
                 *   2) A certain ContentProvider's query method returns null
                 *   3) A RemoteException was thrown.
                 *
                 * Bottom line, it is generally a good idea to check if a Cursor returned
                 * from a ContentResolver is null.
                 *
                 * If the Cursor was null OR if it was empty, we need to sync immediately to
                 * be able to display data to the user.
                 */
                if (null == cursor || cursor.getCount() == 0) {
                    startSync(getApplicationContext());
                }

                /* Make sure to close the Cursor to avoid memory leaks! */
                cursor.close();
            }
        });

        /* Finally, once the thread is prepared, fire it off to perform our checks. */
        checkForEmpty.start();

    }

    public static void startSync(final Context context){
        Intent intentToSync = new Intent(context,SyncIntentService.class);
        context.startService(intentToSync);
    }

    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    /*

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        gps = new GPS(this);
                        if(gps.canGetLocation()){
                            lat = gps.getLatitude();
                            lon = gps.getLongitude();
                            Log.d("Place Code: ",lat+", "+lon);
                        }

                        if(lat != 0 && lon != 0) {
                            WeatherPreferences.setLocationDetails(this, lat, lon);
                        }

                        initialize();

                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Location Permission denied", Toast.LENGTH_LONG).show();
                    initialize();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }*/
}
