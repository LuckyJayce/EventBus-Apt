package com.shizhefei.eventbusdemo;

import android.app.Application;

import com.shizhefei.eventbus.EventBus;
import com.shizhefei.eventbus.EventProxyAptFactory;
import com.shizhefei.eventbus.EventProxyRuntimeFactory;

/**
 * Created by LuckyJayce on 2017/5/7.
 */

public class AppContext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.init(this, true);
        EventBus.setEventProxyFactory(new EventProxyAptFactory());
    }
}
