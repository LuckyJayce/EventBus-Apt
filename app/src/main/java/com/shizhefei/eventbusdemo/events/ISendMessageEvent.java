package com.shizhefei.eventbusdemo.events;

import com.shizhefei.eventbus.IRemoteEvent;
import com.shizhefei.eventbus.annotation.Event;
import com.shizhefei.eventbus.demo.MessageCallback;

/**
 * Created by luckyjayce on 18-3-23.
 */
@Event
public interface ISendMessageEvent extends IRemoteEvent {
    void sendMessage(String content, MessageCallback messageCallback);
}
