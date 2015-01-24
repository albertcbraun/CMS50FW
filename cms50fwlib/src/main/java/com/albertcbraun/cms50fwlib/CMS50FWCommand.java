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
