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




package com.voiceforiot.isycustomsocket.isy;

import android.content.Context;
import android.util.Log;

import com.voiceforiot.isycustomsocket.constants.SecurityConstants;
import com.voiceforiot.isycustomsocket.cursorQueries.QueryHub;
import com.voiceforiot.isycustomsocket.dataRequestUtils.Crypto;
import com.voiceforiot.isycustomsocket.dataRequestUtils.HttpRequestReturnString;
import com.voiceforiot.isycustomsocket.objects.HubObject;


import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class TestConnection {

    /** Tag for the log messages */
    private static final String LOG_TAG = TestConnection.class.getSimpleName();

    private static String mLocalIpAddress;
    private static String mLocalUsername;
    private static String mLocalPassword;
    private static String mUserName;
    private static String mPassword;

    private TestConnection(){}

    //public static List<IotItem> fetchData(String requestUrl)
    public static String testISY994iConnection(Context context, long hubId) {




        //get the relevant hub data such as username passwords ect
        //and set to globals
        getHubData(context, hubId);

        //NOTE THAT INTERNET CONNECTION WAS TESTED IN THE CheckNetworkDialogue ACTIVITY
        String baseUrl;

        if (mLocalIpAddress.startsWith("http://")){
            Log.v(LOG_TAG, "request sent via http");
            baseUrl = mLocalIpAddress + "/rest";
            mUserName = mLocalUsername;
            mPassword = mLocalPassword;
        }
        else if (mLocalIpAddress.startsWith("https://")){
            baseUrl = mLocalIpAddress + "/rest";
            mUserName = mLocalUsername;
            mPassword = mLocalPassword;
        }
        else {
            return "URL (Address) must be fully qualified (starting with http:// or https://)";
        }



        String decrypted = "";
        try {
            decrypted = Crypto.decryptString(context, SecurityConstants.KEYSTORE_ALIAS, mPassword);
        } catch (Exception e) {
            Log.v(LOG_TAG, "password decryption failed");
            return "Password decryption failed. Please reset password";

        }
        mPassword = decrypted;


        //string to append to the end of the base url to get queried items
        String queryString = null;
        queryString = "/time";



        String newUrl = baseUrl + queryString;
        Log.v(LOG_TAG, "newUrl is: " + newUrl);

        // Create URL object
        final URL url = createUrl(newUrl);


        //Preform HTTP request to the url and receive an XML response back
        String xmlResponse = null;
        try {
            xmlResponse = HttpRequestReturnString.makeHttpRequest(url, mUserName, mPassword);
        } catch (IOException e) {
            Log.v(LOG_TAG, "Error closing input stream ", e);
            return "IOException: \n" + e;
        }


        String time = null;
        try {

            time = ExtractTimeFromXml_ISY.extractsTimeFromXml(xmlResponse);
            if (time != null) {
                return "Connection success!\n" + time;
            }


        } catch (XmlPullParserException e) {
            Log.e(LOG_TAG, "XmlPullParserException ", e);
            //return "XmlPullParserException " + e;

        } catch (IOException e) {
            Log.e(LOG_TAG, "IOExcepton ", e);
            //return "IOExcepton " + e;
        }
        //Return the
        return "Error: \n" + xmlResponse;
    }


    /**
     * Get hub data from the Hubs table
     */
    private static void getHubData(Context context, long HubId){
        HubObject hubObject = QueryHub.getHubDataFromHubId(context, HubId);
        mLocalIpAddress = hubObject.getLocalIpAddress();
        mLocalUsername = hubObject.getLocalUsername();
        mLocalPassword = hubObject.getLocalPassword();
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }
}