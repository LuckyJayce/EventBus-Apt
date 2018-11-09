package com.shizhefei.eventbusdemo.news;

import com.shizhefei.eventbus.IEvent;
import com.shizhefei.eventbus.annotation.Event;

/**
 * Created by luckyjayce on 2017/8/17.
 */
@Event
public interface IPostNewsEvent extends IEvent {
    void onPost(int newsId, String message);
}
