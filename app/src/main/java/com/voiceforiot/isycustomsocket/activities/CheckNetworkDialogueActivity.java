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

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.voiceforiot.isycustomsocket.R;
import com.voiceforiot.isycustomsocket.loaders.TestNetworkConnectionsLoader;

public class CheckNetworkDialogueActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    /* Loader ID */
    private static final int IOT_ITEM_LOADER_ID = 1;
    /*Empty State for list */
    private TextView mEmptyStateTextView;
    //Global variable for the Progress Bar
    private ProgressBar mProgressBar;
    /*Hub Id passed in by intent*/
    private int mHubId;


    private static final String LOG_TAG = CheckNetworkDialogueActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_network);

        setTitle(R.string.checking_hub_connection);


        //find relevant views
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view_iot_items_list);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_iot_item_list);

        //Use: getIntent() and getData to get the associated URI
        Intent intent = getIntent();
        //get the intent data
        Uri currentHubUri = intent.getData();
        String hubIdString = currentHubUri.getLastPathSegment();
        if (hubIdString != null) {
            mHubId = Integer.parseInt(hubIdString);
        } else {
            Toast.makeText(this, getString(R.string.editor_insert_failed),
                    Toast.LENGTH_LONG).show();
            finish();
        }


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
        return new TestNetworkConnectionsLoader(this, mHubId);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        mProgressBar.setVisibility(View.GONE);
        mEmptyStateTextView.setText(data);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        mEmptyStateTextView.setText(null);
    }
}
