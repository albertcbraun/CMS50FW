/*
 * Copyright (c) 2015 Albert C. Braun
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

package com.albertcbraun.cms50fwlib;

/**
 * Selected command bytes for use on output stream back to the
 * CMS50FW, as defined in the manual:
 * <i>Communication protocol of pulse oximeter V7.0.pdf</i>
 * <p>
 * Not all possible CMS50FW commands are represented here.
 * <p>
 * Created by albertb on 1/13/2015.
 */
enum CMS50FWCommand {
    START_DATA((byte) 0xA1),             // 161
    STOP_DATA((byte) 0xA2),             // 162
    STAY_CONNECTED((byte) 0xAF),         // 175
    SEND_USER_INFORMATION((byte) 0xAB),  // 171
    PADDING((byte) 0x80),                // 128
    COMMAND_FOLLOWS((byte) 0x7D);        // 125

    private final byte command;

    CMS50FWCommand(byte command) {
        this.command = command;
    }

    public int asInt() {
        return (int) command;
    }
}
