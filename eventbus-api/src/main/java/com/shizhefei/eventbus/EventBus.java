package com.shizhefei.eventbus;

import android.app.Activity;

import java.util.WeakHashMap;

public class EventBus {
    private static final EventHandler defaultEventHandler = new EventHandler();

    private static final WeakHashMap<Activity, IEventHandler> eventHandlerMap = new WeakHashMap<Activity, IEventHandler>();

    public static void register(IEvent iEvent) {
        defaultEventHandler.register(iEvent);
    }

    public static void unregister(IEvent iEvent) {
        defaultEventHandler.unregister(iEvent);
    }

    public static <IEVENT extends IEvent> IEVENT postMain(Class<? extends EventProxy<IEVENT>> eventClass) {
        return defaultEventHandler.postMain(eventClass);
    }

    public static <IEVENT extends IEvent> IEVENT post(Class<? extends EventProxy<IEVENT>> eventClass) {
        return defaultEventHandler.post(eventClass);
    }

    public static synchronized IEventHandler withActivity(Activity activity) {
        IEventHandler eventBusImp = eventHandlerMap.get(activity);
        if (eventBusImp == null) {
            eventBusImp = new EventHandler();
            eventHandlerMap.put(activity, eventBusImp);
        }
        return eventBusImp;
    }
}