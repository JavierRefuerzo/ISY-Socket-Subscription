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
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.voiceforiot.isycustomsocket.R;
import com.voiceforiot.isycustomsocket.constants.SecurityConstants;
import com.voiceforiot.isycustomsocket.constants.UriValues;
import com.voiceforiot.isycustomsocket.data.DatabaseContract;
import com.voiceforiot.isycustomsocket.dataRequestUtils.Crypto;

public class HubsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    
    private String LOG_TAG = HubsActivity.class.getSimpleName();

    /* EditText field to enter the hub name */
    private EditText mNameEditText;
    private String mNameString;

    /*EditText field to enter the Local ip Address */
    private TextView mLocalIPAddressTextView;
    private EditText mLocalIpAddressEditText;
    private String mLocalIpAddressString;
    private ImageView mFindLocalNetworkImageView;

    //request ID (Code) for startActivityForResult
    public static final int HUB_LOCAL_IP_ADDRESS_REQUEST_ID = 1;

    /*EditText field to enter the Local username */
    private EditText mLocalUsernameEditText;
    private String mLocalUsernameString;

    /*EditText field to enter the Local password */
    private TextView mLocalPasswordTextView;
    private EditText mLocalPasswordEditText;
    private String mLocalPasswordString;

    private Button mTestLocalNetworkButton;

    /* uri to call when editing a hub */
    private Uri mCurrentHubUri;

    /** Boolean flag that keeps track of whether the hub has been edited (true) or not (false) */
    private boolean mHubHasChanged = false;

    // Loader ID. This id is used to identify the Loader<cursor> with a uri of a single hub row
    //in the database, that was passed to this activity in the intent, when a row in the database
    //was entered in edit mode
    private static final int URI_LOADER = 1;
    //loader ID. This id is used identify a Loader<cursor> used for validation, to verify that
    //that we do not enter repetitive data
    private static final int VALIDATION_URI_LOADER =2;

    //Flag used to save hub when testing the hub connection
    private boolean SAVE_CALL_LAUNCHED_FROM_CHECK_NETWORK_CONNECTION = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hubs);

        //find relevant views
        mNameEditText = (EditText) findViewById(R.id.edittext_hub_name);
        mLocalIpAddressEditText = (EditText) findViewById(R.id.edittext_local_ip_address);
        mLocalUsernameEditText = (EditText) findViewById(R.id.edittext_local_username);
        mLocalPasswordEditText = (EditText) findViewById(R.id.edittext_local_password);
        mTestLocalNetworkButton = (Button) findViewById(R.id.test_local_network_description);
        mFindLocalNetworkImageView = (ImageView) findViewById(R.id.find_hub_circle);

        //Set onTouchListener to all items to notify user if activity is discarded
        //mTouchListener is and override method
        mLocalIpAddressEditText.setOnTouchListener(mTouchListener);
        mLocalUsernameEditText.setOnTouchListener(mTouchListener);
        mLocalPasswordEditText.setOnTouchListener(mTouchListener);

        mTestLocalNetworkButton.setOnClickListener(localNetworkTestClickListener);
        mFindLocalNetworkImageView.setOnClickListener(findHubClickListener);

        //Use: getIntent() and getData to get the associated URI
        Intent intent = getIntent();
        //get the intent data
        Uri currentUri = intent.getData();
        // Figure out if the URI matcher can match the URI to a specific code
        final int match = UriValues.sUriMatcher.match(currentUri);
        switch (match) {
            case UriValues.HUBS:
                //uri has table name only
                //Set the title to Add Hub
                setTitle(R.string.add_hub);
                //Remove the menu as deleting a hub is not available in add hub
                //Note that onPrepareOptionsMenu() must be override
                invalidateOptionsMenu();
                //set Hub has changed as save is required to test network connection
                mHubHasChanged = true;
                break;
            case UriValues.HUBS_ID:
                //uri has table name and row number
                setTitle(R.string.edit_hub);
                //set the global from the local
                mCurrentHubUri = currentUri;
                Log.v(LOG_TAG, "uri is: " + mCurrentHubUri);
                //get the loader manager, must override onCreateLoader, onLoadFinished, and onLoaderReset
                //and modify this class with extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>
                //and set an int variable for the instance of the URL loader
                getLoaderManager().initLoader(URI_LOADER, null, this);
                break;
            default:
                throw new IllegalArgumentException("Can not load this page as uri does not match an excepted uri");
        }
    }


    /*On click listener for finding the local ip address*/
    private  View.OnClickListener findHubClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(HubsActivity.this, FindHubDialogueActivity.class);
            startActivityForResult(intent, HUB_LOCAL_IP_ADDRESS_REQUEST_ID);
        }
    };


    /* On click listener for testing local network connection*/
    private View.OnClickListener localNetworkTestClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //check if the hub has been saved
            if (mHubHasChanged) {
                showSaveConfirmationDialog();
            }else {
                SAVE_CALL_LAUNCHED_FROM_CHECK_NETWORK_CONNECTION = false;
                Intent intent = new Intent(HubsActivity.this, CheckNetworkDialogueActivity.class);
                intent.setData(mCurrentHubUri);
                startActivity(intent);
            }

        }
    };


    private void showSaveConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.save_dialog_msg);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //save the hub
                SAVE_CALL_LAUNCHED_FROM_CHECK_NETWORK_CONNECTION = true;
                runDataValidation();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //helper method to save a hub
    private void runDataValidation() {

        //get all user input
        mNameString = mNameEditText.getText().toString().trim();
        mLocalIpAddressString = mLocalIpAddressEditText.getText().toString().trim();
        mLocalUsernameString = mLocalUsernameEditText.getText().toString().trim();
        mLocalPasswordString = mLocalPasswordEditText.getText().toString().trim();

        //Validate data
        if (TextUtils.isEmpty(mNameString) || mNameString == null) {
            Toast.makeText(this, "Hub Name is empty", Toast.LENGTH_LONG).show();
            //return as there is no need to insert or update table
            return;
        }
        else if (TextUtils.isEmpty(mLocalIpAddressString) || mLocalIpAddressString == null){
            Toast.makeText(this, "IP Address is empty", Toast.LENGTH_LONG).show();
            return;
        }
        else if (TextUtils.isEmpty(mLocalUsernameString) || mLocalUsernameString == null){
            Toast.makeText(this, "Username is empty", Toast.LENGTH_LONG).show();
            return;
        }
        else if (TextUtils.isEmpty(mLocalPasswordString) || mLocalPasswordString == null){
            Toast.makeText(this, "Password is empty", Toast.LENGTH_LONG).show();
            return;
        }
        else {
            //TODO: finish method by validating data based on the mHubType
            //data is present in both the name and BSSID so validate data by requesting a new
            //cursor with the entire hub table, in on load finished validation will be called
            getLoaderManager().initLoader(VALIDATION_URI_LOADER, null, this);
        }
    }





        /**
         * OnTouchListener that listens for any user touches on a View, implying that they are modifying
         * the view, and we change the mHubHasChanged boolean to true.
         */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mHubHasChanged = true;
            return false;
        }
    };


    /**
     *  Method which receives data when the "startActivityForResult" is returned
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        switch (requestCode){
            case HUB_LOCAL_IP_ADDRESS_REQUEST_ID:
                switch (resultCode){
                    //result was returned
                    case Activity.RESULT_OK:
                        String mAddress = data.getStringExtra("address");
                        mLocalIpAddressEditText.setText(mAddress);
                        mHubHasChanged = true;
                        Log.v(LOG_TAG, "address is: " + mAddress);
                        break;
                    //user backed out of the activity or there was another error
                    case Activity.RESULT_CANCELED:
                        Log.v(LOG_TAG, "onActivityResult case RESULT_CANCELED");
                        break;
                }
            default:
                Log.v(LOG_TAG, "Could not match resultCode");
        }
    }


    //Creates the Menu button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }


    //defines what happens when a menu item is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                runDataValidation();
                //Toast.makeText(this, "save button clicked", Toast.LENGTH_LONG).show();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_help:
                Log.v(LOG_TAG, "Help button clicked");
                goToUrl("https://voiceforiot.wordpress.com/setup/#hub");
                return true;
            case android.R.id.home:
                //NOTE THIS HAS BEEN REMOVED FROM MANIFEST
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //send user to url
    private void goToUrl(String url) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri uri = Uri.parse(url);
        intent.setData(uri);
        Log.v(LOG_TAG, "Intent started");
        startActivity(intent);
    }


    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteHub();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the hub in the database.
     */
    private void deleteHub() {
        int numOfRowsDeleted = 0;
        //Only perform the delete action if there is an existing hub
        if (mCurrentHubUri != null) {
            //Delete the single hub with the uri passed by the intent to create this activity in edit mode
            numOfRowsDeleted = getContentResolver().delete(mCurrentHubUri, null, null);
            //Show a toast message with informatics regarding deletion
            if (numOfRowsDeleted == 0) {
                // If the new content URI is 0, then no rows where deleted.
                Toast.makeText(this, getString(R.string.editor_delete_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the deletion was successful and we can display the number of rows deleted as a toast.
                String stringNumber = String.valueOf(numOfRowsDeleted);
                Toast.makeText(this, getString(R.string.editor_delete_successful) + "\nNumber of rows deleted :" + stringNumber ,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }



    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

        String[] projection = {
                DatabaseContract.HubEntry._ID,
                DatabaseContract.HubEntry.COLUMN_HUB_NAME,
                DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_IP_ADDRESS,
                DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_USERNAME,
                DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_PASSWORD
        };
        
        
        switch (loaderId) {
            case URI_LOADER:
                
                return new CursorLoader(
                        this,   // Parent activity context
                        mCurrentHubUri,        // Table to query
                        projection,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order
                );
            case VALIDATION_URI_LOADER:
                String selectionClause;
                //if we are validating against and existing row, do not include
                //the row in the query
                if (mCurrentHubUri != null) {
                    selectionClause = DatabaseContract.HubEntry.COLUMN_HUB_NAME + "=?" +
                            " AND " + DatabaseContract.HubEntry._ID + " != " + mCurrentHubUri.getLastPathSegment();
                }else {
                    selectionClause = DatabaseContract.HubEntry.COLUMN_HUB_NAME + "=?";
                }
                String[] selectionArguments = {mNameString} ;
                return new CursorLoader(
                        this,   // Parent activity context
                        DatabaseContract.HubEntry.CONTENT_URI, //uri to query, query the entire hub table
                        projection,     // Projection to return
                        selectionClause,            // No selection clause
                        selectionArguments,            // No selection arguments
                        null             // Default sort order
                );    
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        int loaderId = loader.getId();
        switch (loaderId) {
            case URI_LOADER:
                //Loader used when editing an existing hub
                //check to see if we have a row of data
                if (cursor.moveToFirst()) {
                    //get the data from the columns needed to update the textViews as we are editing an existing hub
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HubEntry.COLUMN_HUB_NAME));
                    String localIpAddress = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_IP_ADDRESS));
                    String localUsername = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_USERNAME));
                    String localPassword = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_PASSWORD));

                    String decrypted = "";
                    try {
                        decrypted = Crypto.decryptString(HubsActivity.this, SecurityConstants.KEYSTORE_ALIAS, localPassword);
                    } catch (Exception e) {
                        Log.v(LOG_TAG, "Decryption Error: " + e);
                        Toast.makeText(this, "Error decrypting local password please reset password\n" + e,
                                Toast.LENGTH_LONG).show();
                    }
                    localPassword = decrypted;

                    //Update textViews
                    mNameEditText.setText(name);
                    mLocalIpAddressEditText.setText(localIpAddress);
                    mLocalUsernameEditText.setText(localUsername);
                    mLocalPasswordEditText.setText(localPassword);

                }
                break;
            case VALIDATION_URI_LOADER:
                //loader called to when attempting to save a hub, this runs
                //a check on the database to be sure there is no duplicate entries in areas
                //where we do not want duplicate entries
                Log.v(LOG_TAG, "onLoadFinished Called ID is: " + VALIDATION_URI_LOADER);
                //helper method which validates and saves data
                hubDataNoDuplicateValidation(loaderId, cursor);
                break;
            default:
                Log.v(LOG_TAG, "could not find loader ID in onLoadFinished");
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }



    private void hubDataNoDuplicateValidation(int id, Cursor cursor) {
        //see onOptionsItemSelected onBackPressed() for other calls on sHowUnsavedChangesDialog
        //get the number of rows returned
        int numOfRowsReturned = cursor.getCount();
        //Log.v(LOG_TAG, "cursor count is: " + numOfRowsReturned);
        getLoaderManager().destroyLoader(id);
        if (numOfRowsReturned == 0) {
            //if there are no rows returned there is no duplicate name or bssid so save data
            saveHub();
        }else {
            //else there a name or bssid matches the current item being saved so use a
            // dialogue to warn user and do not save data
            // Otherwise if there are unsaved changes, setup a dialog to warn the user.
            // Create a click listener to handle the user confirming that
            // changes should be discarded.
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked "Discard" button, navigate to parent activity.
                            //NavUtils.navigateUpFromSameTask(HubsActivity.this);
                            finish();
                        }
                    };

            // Show a dialog that notifies the user they have unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener, R.string.duplicate_data_dialog_msg);
            return;
        }

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener, int messageStringResource) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(messageStringResource);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    //helper method to save hub
    private void saveHub() {
        //Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.HubEntry.COLUMN_HUB_NAME, mNameString);
        values.put(DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_IP_ADDRESS, mLocalIpAddressString);
        values.put(DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_USERNAME, mLocalUsernameString);
        String encrypted = "";
        try {
            encrypted = Crypto.encryptString(HubsActivity.this, SecurityConstants.KEYSTORE_ALIAS, mLocalPasswordString);
            Log.v(LOG_TAG, "Encrypted local password is: " + encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error encrypting local password please notify developer\n" + e,
                    Toast.LENGTH_LONG).show();
        }
        mLocalPasswordString = encrypted;
        values.put(DatabaseContract.HubEntry.COLUMN_HUB_LOCAL_PASSWORD, mLocalPasswordString);

        //Check to see if we are updating an existing hub or adding a new hub
        if (mCurrentHubUri == null) {
            //there is no uri so this is a new hub
            //Use the content resolver to insert a new hub into the database
            Uri newUri = getContentResolver().insert(DatabaseContract.HubEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_failed),
                        Toast.LENGTH_SHORT).show();
            }else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_successful),
                        Toast.LENGTH_SHORT).show();
                //If the save action was started from the create a hub_network_relation
                //do not finish this activity
                if (!SAVE_CALL_LAUNCHED_FROM_CHECK_NETWORK_CONNECTION){
                    //close the activity
                    finish();
                } else if (SAVE_CALL_LAUNCHED_FROM_CHECK_NETWORK_CONNECTION) {
                    mCurrentHubUri = newUri;
                    SAVE_CALL_LAUNCHED_FROM_CHECK_NETWORK_CONNECTION = false;
                    Intent intent = new Intent(HubsActivity.this, CheckNetworkDialogueActivity.class);
                    intent.setData(mCurrentHubUri);
                    startActivity(intent);
                }
                //Create action associations when a hub is saved
            }
        }
        else {
            //else we are updating an existing hub
            //update method returns an int of number of rows affected
            int rowsAffected = getContentResolver().update(mCurrentHubUri, values, null, null);
            // Show a toast message depending on whether or not the insertion was successful
            if (rowsAffected > 1) {
                Toast.makeText(this, getString(R.string.editor_insert_failed),
                        Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, getString(R.string.editor_insert_successful),
                        Toast.LENGTH_SHORT).show();
                if (SAVE_CALL_LAUNCHED_FROM_CHECK_NETWORK_CONNECTION){
                    SAVE_CALL_LAUNCHED_FROM_CHECK_NETWORK_CONNECTION = false;
                    //mTestLocalConnectionLinearLayout.callOnClick();
                    Intent intent = new Intent(HubsActivity.this, CheckNetworkDialogueActivity.class);
                    intent.setData(mCurrentHubUri);
                    startActivity(intent);
                }else {
                    Toast.makeText(this, getString(R.string.editor_insert_successful),
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
}
