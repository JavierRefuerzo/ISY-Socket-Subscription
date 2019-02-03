/*
 * Copyright 2016 - 2019 Javier Refuerzo. Swansea Software LLC. Denver, CO. USA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package com.voiceforiot.isycustomsocket.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import com.voiceforiot.isycustomsocket.isy.FindISY;


/**
 * Created by Refuerzo on 12/25/2016.
 */

public class FindHubAsyncTaskLoader extends AsyncTaskLoader<String> {




    private String LOG_TAG = FindHubAsyncTaskLoader.class.getSimpleName();

    public FindHubAsyncTaskLoader(Context context) {
        super(context);

    }


    @Override
    protected void onStartLoading() {
        forceLoad();
        Log.v(LOG_TAG, "onStartLoading");
    }


    @Override
    public String loadInBackground() {
        Log.v(LOG_TAG, "loadInBackground");
        // Perform the network request, parse the response, and extract a list of iot items
        String response = "attempt failed";

        try {
            response = FindISY.findIsy(null);
        } catch (Exception e) {
            Log.v(LOG_TAG, "Exception" + e);
        }

        return response;
    }



}
