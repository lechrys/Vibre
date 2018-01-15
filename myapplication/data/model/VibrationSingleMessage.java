package fr.myapplication.dc.myapplication.data.model;

import util.MessageUtil;

/**
 * Created by Crono on 04/12/16.
 */

//Todo: no need to store the all pattern in DB, we can store this message with and ID
    //for exemple ID 1 for long vibration message ID 2 for another one.
    //etc ..
public class VibrationSingleMessage extends VibrationMessage{

    public VibrationSingleMessage(){
        init();
    }

    // The first value indicates the number of millisecond to wait before turning the vibrator on
    // 0 means vibration is instantly on
    public VibrationSingleMessage init(){
        this.setPattern(MessageUtil.getSingleMessageListValue());
        return this;
    }
}

