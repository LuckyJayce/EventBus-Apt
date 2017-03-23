package com.shizhefei.eventbus.events;

import com.shizhefei.eventbus.IEvent;
import com.shizhefei.eventbus.annotation.Event;

/**
 * Created by LuckyJayce on 2017/3/22.
 */
@Event
public interface ITestEvent extends IEvent{
    <E extends IEvent & IAccountEvent, H> void onTest(E event, H h, int messageId, String message) throws Exception;

    <E extends IEvent & IAccountEvent, H> void onTest(E event, H h, int messageId);

    void onTest(String s, int... messageId);

    void aaa();
}
