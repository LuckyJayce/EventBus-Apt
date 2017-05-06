package com.shizhefei.eventbus;

import android.app.Activity;
import android.content.Context;

import java.util.WeakHashMap;

public class EventBus {
    private static final EventHandler defaultEventHandler = new EventHandler();

    private static final WeakHashMap<Activity, EventHandler> eventHandlerMap = new WeakHashMap<Activity, EventHandler>();

    static Context staticContext;
    public static void init(Context context){
        staticContext = context.getApplicationContext();
    }

    public static void register(IEvent iEvent) {
        defaultEventHandler.register(iEvent);
    }

    public static void unregister(IEvent iEvent) {
        defaultEventHandler.unregister(iEvent);
    }

    public static <EVENT extends IEvent> EVENT postMain(Class<? extends EventProxy<EVENT>> eventClass) {
        return defaultEventHandler.postMain(eventClass);
    }

    public static <EVENT extends IEvent> EVENT post(Class<? extends EventProxy<EVENT>> eventClass) {
        return defaultEventHandler.post(eventClass);
    }

    public static <EVENT extends IRemoteEvent> EVENT postRemote(Class<? extends EventProxy<EVENT>> eventClass, String processName) {
        return defaultEventHandler.postRemote(eventClass, processName);
    }

    static EventProxy getEventProxy(Class<? extends EventProxy> eventClass) {
        Class c = eventClass;
        return defaultEventHandler.getEventProxy(c);
    }

    public static synchronized IEventHandler withActivity(Activity activity) {
        EventHandler eventBusImp = eventHandlerMap.get(activity);
        if (eventBusImp == null) {
            eventBusImp = new EventHandler();
            eventHandlerMap.put(activity, eventBusImp);
        }
        return eventBusImp;
    }
}