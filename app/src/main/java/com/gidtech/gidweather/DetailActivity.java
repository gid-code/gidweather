package com.gidtech.gidweather;

import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.gidtech.gidweather.databinding.ActivityDetailBinding;

/**
 * Created by gid on 2/19/2018.
 */

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    public static final String[] WEATHER_DETAIL_PROJECTION = {
            WContract.WeatherEntry.COLUMN_DATE,
            WContract.WeatherEntry.COLUMN_MAX_TEMP,
            WContract.WeatherEntry.COLUMN_MIN_TEMP,
            WContract.WeatherEntry.COLUMN_MORN_TEMP,
            WContract.WeatherEntry.COLUMN_DAY_TEMP,
            WContract.WeatherEntry.COLUMN_EVE_TEMP,
            WContract.WeatherEntry.COLUMN_NIGHT_TEMP,
            WContract.WeatherEntry.COLUMN_HUMIDITY,
            WContract.WeatherEntry.COLUMN_PRESSURE,
            WContract.WeatherEntry.COLUMN_WIND_SPEED,
            WContract.WeatherEntry.COLUMN_DEGREES,
            WContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_MORN_TEMP = 3;
    public static final int INDEX_WEATHER_DAY_TEMP = 4;
    public static final int INDEX_WEATHER_EVE_TEMP = 5;
    public static final int INDEX_WEATHER_NIGHT_TEMP = 6;
    public static final int INDEX_WEATHER_HUMIDITY = 7;
    public static final int INDEX_WEATHER_PRESSURE = 8;
    public static final int INDEX_WEATHER_WIND_SPEED = 9;
    public static final int INDEX_WEATHER_DEGREES = 10;
    public static final int INDEX_WEATHER_CONDITION_ID = 11;

    /* A summary of the forecast that can be shared by clicking the share button in the ActionBar */
    private String mForecastSummary;

    /* The URI that is used to access the chosen day's weather details */
    private Uri mUri;

    private ActivityDetailBinding mDetailBinding;

    private static final String FORECAST_SHARE_HASHTAG = " #GidWeatherApp";

    private static final int ID_DETAIL_LOADER = 265;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        mUri = getIntent().getData();
        if (mUri == null) throw new NullPointerException("URI for DetailActivity cannot be null");

        /* This connects our Activity into the loader lifecycle. */
        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.detail, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Get the ID of the clicked item */
        int id = item.getItemId();

        /* Settings menu item clicked */
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        /* Share menu item clicked */
        if (id == R.id.action_share) {
            Intent shareIntent = createShareForecastIntent();
            startActivity(shareIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(mForecastSummary + FORECAST_SHARE_HASHTAG)
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {

        switch (loaderId) {

            case ID_DETAIL_LOADER:

                return new CursorLoader(this,
                        mUri,
                        WEATHER_DETAIL_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        /*
         * Before we bind the data to the UI that will display that data, we need to check the
         * cursor to make sure we have the results that we are expecting. In order to do that, we
         * check to make sure the cursor is not null and then we call moveToFirst on the cursor.
         * Although it may not seem obvious at first, moveToFirst will return true if it contains
         * a valid first row of data.
         *
         * If we have valid data, we want to continue on to bind that data to the UI. If we don't
         * have any data to bind, we just return from this method.
         */
        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

        /****************
         * Weather Icon *
         ****************/
        /* Read weather condition ID from the cursor (ID provided by Open Weather Map) */
        int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
        /* Use our utility method to determine the resource ID for the proper art */
        int weatherImageId = WeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);

        /* Set the resource ID on the icon to display the art */
        mDetailBinding.primaryInfo.weatherIcon.setImageResource(weatherImageId);

        /****************
         * Weather Date *
         ****************/
        long localDateMidnightGmt = data.getLong(INDEX_WEATHER_DATE);
        String dateText = WeatherDateUtils.getFriendlyDateString(this, localDateMidnightGmt, true);

        mDetailBinding.primaryInfo.date.setText(dateText);

        /***********************
         * Weather Description *
         ***********************/
        /* Use the weatherId to obtain the proper description */
        String description = WeatherUtils.getStringForWeatherCondition(this, weatherId);

        /* Create the accessibility (a11y) String from the weather description */
        String descriptionA11y = getString(R.string.a11y_forecast, description);

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.primaryInfo.weatherDescription.setText(description);
        mDetailBinding.primaryInfo.weatherDescription.setContentDescription(descriptionA11y);

        /* Set the content description on the weather image (for accessibility purposes) */
        mDetailBinding.primaryInfo.weatherIcon.setContentDescription(descriptionA11y);

        /**************************
         * High (max) temperature *
         **************************/
        /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = data.getDouble(INDEX_WEATHER_MAX_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String highString = WeatherUtils.formatTemperature(this, highInCelsius);

        /* Create the accessibility (a11y) String from the weather description */
        String highA11y = getString(R.string.a11y_high_temp, highString);

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.primaryInfo.highTemperature.setText(highString);
        mDetailBinding.primaryInfo.highTemperature.setContentDescription(highA11y);

        /*************************
         * Low (min) temperature *
         *************************/
        /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = data.getDouble(INDEX_WEATHER_MIN_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String lowString = WeatherUtils.formatTemperature(this, lowInCelsius);

        String lowA11y = getString(R.string.a11y_low_temp, lowString);

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.primaryInfo.lowTemperature.setText(lowString);
        mDetailBinding.primaryInfo.lowTemperature.setContentDescription(lowA11y);

        /**************************
         * Morning temperature *
         **************************/
        /* Read high temperature from the cursor (in degrees celsius) */
        double mornInCelsius = data.getDouble(INDEX_WEATHER_MORN_TEMP);

        String mornString = WeatherUtils.formatTemperature(this, mornInCelsius);

        /* Create the accessibility (a11y) String from the weather description */
        String mornA11y = getString(R.string.a11y_morn_temp, mornString);

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.primaryInfo.mornValue.setText(mornString);
        mDetailBinding.primaryInfo.mornValue.setContentDescription(mornA11y);

        /**************************
         * Day temperature *
         **************************/
        /* Read high temperature from the cursor (in degrees celsius)
        double dayInCelsius = data.getDouble(INDEX_WEATHER_DAY_TEMP);

        String dayString = WeatherUtils.formatTemperature(this, dayInCelsius);

        /* Create the accessibility (a11y) String from the weather description
        String dayA11y = getString(R.string.a11y_day_temp, dayString);

        /* Set the text and content description (for accessibility purposes)
        mDetailBinding.primaryInfo.dayValue.setText(dayString);
        mDetailBinding.primaryInfo.dayValue.setContentDescription(dayA11y);*/

        /**************************
         * Evening temperature *
         **************************/
        /* Read high temperature from the cursor (in degrees celsius) */
        double eveInCelsius = data.getDouble(INDEX_WEATHER_EVE_TEMP);

        String eveString = WeatherUtils.formatTemperature(this, eveInCelsius);

        /* Create the accessibility (a11y) String from the weather description */
        String eveA11y = getString(R.string.a11y_eve_temp, eveString);

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.primaryInfo.eveningValue.setText(eveString);
        mDetailBinding.primaryInfo.eveningValue.setContentDescription(eveA11y);

        /**************************
         * Night temperature *
         **************************/
        /* Read high temperature from the cursor (in degrees celsius) */
        double nightInCelsius = data.getDouble(INDEX_WEATHER_NIGHT_TEMP);

        String nightString = WeatherUtils.formatTemperature(this, nightInCelsius);

        /* Create the accessibility (a11y) String from the weather description */
        String nightA11y = getString(R.string.a11y_night_temp, nightString);

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.primaryInfo.nightValue.setText(nightString);
        mDetailBinding.primaryInfo.nightValue.setContentDescription(nightA11y);

        /************
         * Humidity *
         ************/
        /* Read humidity from the cursor */
        float humidity = data.getFloat(INDEX_WEATHER_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidity);

        String humidityA11y = getString(R.string.a11y_humidity, humidityString);

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.extraDetails.humidity.setText(humidityString);
        mDetailBinding.extraDetails.humidity.setContentDescription(humidityA11y);

        mDetailBinding.extraDetails.humidityLabel.setContentDescription(humidityA11y);

        /****************************
         * Wind speed and direction *
         ****************************/
        /* Read wind speed (in MPH) and direction (in compass degrees) from the cursor  */
        float windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_WEATHER_DEGREES);
        String windString = WeatherUtils.getFormattedWind(this, windSpeed, windDirection);

        String windA11y = getString(R.string.a11y_wind, windString);

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.extraDetails.windMeasurement.setText(windString);
        mDetailBinding.extraDetails.windMeasurement.setContentDescription(windA11y);

        mDetailBinding.extraDetails.windLabel.setContentDescription(windA11y);

        /************
         * Pressure *
         ************/
        /* Read pressure from the cursor */
        float pressure = data.getFloat(INDEX_WEATHER_PRESSURE);

        String pressureString = getString(R.string.format_pressure, pressure);

        String pressureA11y = getString(R.string.a11y_pressure, pressureString);

        /* Set the text and content description (for accessibility purposes) */
        mDetailBinding.extraDetails.pressure.setText(pressureString);
        mDetailBinding.extraDetails.pressure.setContentDescription(pressureA11y);

        mDetailBinding.extraDetails.pressureLabel.setContentDescription(pressureA11y);

        /* Store the forecast summary String in our forecast summary field to share later */
        mForecastSummary = String.format("%s - %s - %s/%s",
                dateText, description, highString, lowString);
    }

    /**
     * Called when a previously created loader is being reset, thus making its data unavailable.
     * The application should at this point remove any references it has to the Loader's data.
     * Since we don't store any of this cursor's data, there are no references we need to remove.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
