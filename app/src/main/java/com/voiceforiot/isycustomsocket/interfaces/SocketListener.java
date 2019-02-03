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





package com.voiceforiot.isycustomsocket.interfaces;

public interface SocketListener {

    /**
     * The initial response from the socket
     * @param line is the xml returned
     * @param header is the included header
     */
    void onOpen(String line, String header);

    /**
     * Called when the socket is closed
     * @param lines the last xml returned.
     * This (@param lines) may be null or duplicate the last onMessage or onOpen line
     */
    void onClose(String lines);

    /**
     * Called when an exception is thrown
     * @param lines the last xml returned.
     * This (@param lines) may be null or duplicate of last onMessage or onOpen line.
     */
    void onError(String lines);

    /**
     * Called for any response after onOpen.
     * @param line is the xml returned.
     * @param header is the included header
     */
    void onMessage(String line, String header);





}
