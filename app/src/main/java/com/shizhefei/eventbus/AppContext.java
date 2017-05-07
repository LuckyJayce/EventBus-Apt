package com.shizhefei.eventbus;

import android.app.Application;

/**
 * Created by LuckyJayce on 2017/5/7.
 */

public class AppContext extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.init(this, true);
    }
}
