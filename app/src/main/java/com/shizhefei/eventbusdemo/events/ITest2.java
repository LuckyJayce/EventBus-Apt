package com.shizhefei.eventbusdemo.events;

import android.os.Bundle;
import android.os.Parcelable;

import com.shizhefei.eventbus.IRemoteEvent;
import com.shizhefei.eventbusdemo.Person;
import com.shizhefei.eventbusdemo.Point;
import com.shizhefei.eventbus.annotation.Event;

/**
 * Created by LuckyJayce on 2017/5/6.
 */
@Event
public interface ITest2 extends IRemoteEvent {
    void write(int[] ids, String[] message, float[] prices, byte[] data);

    void onTest(int messageId, String message, Bundle data, Parcelable parcelable, Parcelable[] parcelables, Point[] name, Point point, Person person, Person[] persons);

    void onTest();
}
