package com.shizhefei.eventbusdemo.events;

import com.shizhefei.eventbus.IEvent;
import com.shizhefei.eventbus.annotation.Event;

/**
 * Created by luckyjayce on 2018/1/16.
 */
@Event
public interface TestFilterEvent extends IEvent {
    void onWrite(Filter filter, String text);
}
