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

import android.text.TextUtils;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;


public class ExtractTimeFromXml_ISY {

    private ExtractTimeFromXml_ISY() {
    }

    private static String LOG_TAG = ExtractTimeFromXml_ISY.class.getSimpleName();

    public static String extractsTimeFromXml(String isyDeviceXML)
            throws XmlPullParserException, IOException {


        String text = null;
        String time = null;
        //Testing
        int count = 0;

        //if the XML string is empty or null, then return early.
        if (TextUtils.isEmpty(isyDeviceXML)) {
            return null;
        }


        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput(new StringReader(isyDeviceXML));
        int eventType = xpp.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = xpp.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    //Log.v(LOG_TAG, "XmlPullParser.START_TAG is: " + tagName);
                    //if the start tag is node create a new device
                    if (tagName.equalsIgnoreCase("NTP")) {
                        Log.v(LOG_TAG, "NTP found");
                    }

                    //set a variable with the text of the current value of the tag
                    //this is not related to XmlPullParser.START_TAG.
                    //this method will grab the text for every tag and can be set to an item
                    //under the XmlPullParser.END_TAG
                case XmlPullParser.TEXT:
                    text = xpp.getText();
                    break;
                //END TAG  set the previously saved text (string) based on the end tag
                case XmlPullParser.END_TAG:
                    ///////////////////////Scene (node)///////////////////////////////
                    if (tagName.equalsIgnoreCase("ntp")) {
                        //add the device to the devices arrayList
                        //device.setQueryType(HubDataValues.ISY994iDeviceTypes.QUERY_TYPE_DEVICE_SWITCH);
                        time = text;
                    }
                default:
                    break;
            }
            eventType = xpp.next();
        }
        //Testing Log
        Log.v(LOG_TAG, "programs count is: " + count);
        return time;
    }
}