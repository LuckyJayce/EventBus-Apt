package com.shizhefei.eventbus.events;

import com.shizhefei.eventbus.annotation.Event;
import com.shizhefei.eventbus.IEvent;

/**
 * Created by LuckyJayce on 2017/3/21.
 */
@Event
public interface IAccountEvent extends IEvent {
    void logout();
    void login();
}
