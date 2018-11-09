package com.shizhefei.eventbusdemo.events2;

import com.shizhefei.eventbus.IEvent;
import com.shizhefei.eventbus.annotation.Event;

/**
 * Created by LuckyJayce on 2017/3/20.
 */

@Event
public interface IMessageEvent extends IEvent {
    void onReceiveMessage(String message);
}
