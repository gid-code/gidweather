package com.gidtech.gidweather.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by gid on 12/5/2017.
 */

public class SyncIntentService extends IntentService {

    public SyncIntentService(){
        super("SyncIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SyncTask.synCWeather(this);
    }
}
