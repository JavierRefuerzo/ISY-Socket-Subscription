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

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Refuerzo on 1/24/2017.
 */

public class FindISY {

    private static String LOG_TAG = FindISY.class.getSimpleName();
    private void FindISY(){}


    public static String findIsy(String args[]) throws Exception {
        /* create byte arrays to hold our send and response data */
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];

        /* our M-SEARCH data as a byte array */
        String MSEARCH = "M-SEARCH * HTTP/1.1\n" +
                "HOST:239.255.255.250:1900\n" +
                "MAN:\"ssdp.discover\"\n" +
                "MX:1\n" +
                "ST:urn:udi-com:device:X_Insteon_Lighting_Device:1";
        sendData = MSEARCH.getBytes();

        /* create a packet from our data destined for 239.255.255.250:1900 */
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("239.255.255.250"), 1900);

        /* send packet to the socket*/
        DatagramSocket clientSocket = new DatagramSocket();
        clientSocket.send(sendPacket);

        /* receive response and store in our receivePacket */
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);

        /* get the response as a string */
        String response = new String(receivePacket.getData());

        Log.v(LOG_TAG, response);

        /* close the socket */
        clientSocket.close();

        return  response;
    }
}
