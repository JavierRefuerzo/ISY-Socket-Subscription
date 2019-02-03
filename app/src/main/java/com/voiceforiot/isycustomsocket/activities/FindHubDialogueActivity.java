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



package com.voiceforiot.isycustomsocket.activities;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.voiceforiot.isycustomsocket.R;
import com.voiceforiot.isycustomsocket.loaders.FindHubAsyncTaskLoader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FindHubDialogueActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    /* Loader ID */
    private static final int IOT_ITEM_LOADER_ID = 1;
    /*Empty State for list */
    private TextView mEmptyStateTextView;
    //Global variable for the Progress Bar
    private ProgressBar mProgressBar;
    /*Hub Id passed in by intent*/
    private int mHubType;

    private int mConnectionType;

    private static final String LOG_TAG = FindHubDialogueActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_network);

        setTitle(R.string.looking_for_hub);

        //find relevant views
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view_iot_items_list);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_iot_item_list);

        //Use: getIntent() and getData to get the associated URI
        Intent intent = getIntent();


        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        // Initialize the loader. Pass in the int ID constant defined above and pass in null for
        // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
        // because this activity implements the LoaderCallbacks interface).
        //loaderManager.initLoader(IOT_ITEM_LOADER_ID, null, this);
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(IOT_ITEM_LOADER_ID, null, this);
        } else {
            // display error
            //set the progress bar visibility to GONE
            mProgressBar.setVisibility(View.GONE);

            //Set the text for the Empty State of the list
            mEmptyStateTextView.setText(R.string.no_internet);
        }

    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new FindHubAsyncTaskLoader(FindHubDialogueActivity.this);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        mProgressBar.setVisibility(View.GONE);
        mEmptyStateTextView.setText(data);
        parseIpAddressForIsy(data);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        mEmptyStateTextView.setText(null);
    }


    //parse out ip address for isy
    private void parseIpAddressForIsy(String result){
        //send a returnIntent to the activity which called this Activity
        String patternString = "LOCATION:(.+)\\/desc";
        //Create a pattern object
        Pattern patternObject = Pattern.compile(patternString);
        //Create a matcher object
        Matcher matcherObject = patternObject.matcher(result);
        if (matcherObject.find()){
            result = matcherObject.group(1);
            Intent returnIntent = new Intent();
            returnIntent.putExtra("address", result);
            setResult(Activity.RESULT_OK, returnIntent);
        }
    }

}
