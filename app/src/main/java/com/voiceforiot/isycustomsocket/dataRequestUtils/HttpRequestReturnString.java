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

package com.voiceforiot.isycustomsocket.dataRequestUtils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.charset.Charset;


/**
 * Created by Refuerzo on 1/31/2017.
 */

public class HttpRequestReturnString {

    private static String LOG_TAG = HttpRequestReturnString.class.getSimpleName();

    /**
     * Create a PRIVATE CONSTRUCTOR because no one should ever create a {@link HttpRequestReturnString} object.
     * This class is only meant to hold ****static**** variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private HttpRequestReturnString(){}



    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    public static String makeHttpRequest(URL url, final String username, final String password) throws IOException {
        String response = "";

        //set the authenticator only if we have a user name and password
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()){
            //http://stackoverflow.com/questions/1968416/how-to-do-http-authentication-in-android
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    //Log.v(LOG_TAG, "mLocalPassword is: " + mLocalPassword);
                    return new PasswordAuthentication(username, password.toCharArray());
                }
            });

        }


        // If the URL is null, then return early.
        if (url == null) {
            return response;
        }
        Log.v(LOG_TAG, "url is: " + url);

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                response = readFromStream(inputStream);
            } else {
                response = "Error" + urlConnection.getResponseCode();
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            response = "Error (IOException) " + e;
            Log.e(LOG_TAG, "Problem retrieving the results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return response;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole XML response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
