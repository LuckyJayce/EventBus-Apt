package com.shizhefei.eventbusdemo.events;

import com.shizhefei.eventbus.IRemoteEvent;
import com.shizhefei.eventbus.annotation.Event;

/**
 * Created by LuckyJayce on 2017/3/20.
 */

@Event
public interface IMessageEvent extends IRemoteEvent {
    void onReceiverMessage(int messageId, String message);
}
