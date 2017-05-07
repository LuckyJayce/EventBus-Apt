package com.shizhefei.eventbus;

import android.app.Activity;
import android.content.Context;

import java.util.WeakHashMap;

public class EventBus {

    private static EventProcessHandler defaultEventHandler;
    private static WeakHashMap<Activity, EventHandler> eventHandlerMap;
    private static Context staticContext;
    private static boolean staticHasRemoteEvent;

    public static void init(Context context, boolean hasRemoteEvent) {
        staticContext = context.getApplicationContext();
        staticHasRemoteEvent = hasRemoteEvent;
        eventHandlerMap = new WeakHashMap<>();
        defaultEventHandler = new EventProcessHandler(hasRemoteEvent);
    }

    static Context getContext() {
        return staticContext;
    }

    static boolean isHasRemoteEvent() {
        return staticHasRemoteEvent;
    }
    public static void register(IEvent iEvent) {
        checkInit();
        defaultEventHandler.register(iEvent);
    }

    public static void unregister(IEvent iEvent) {
        checkInit();
        defaultEventHandler.unregister(iEvent);
    }

    public static <EVENT extends IEvent> EVENT postMain(Class<? extends EventProxy<EVENT>> eventClass) {
        checkInit();
        return defaultEventHandler.postMain(eventClass);
    }

    public static <EVENT extends IEvent> EVENT post(Class<? extends EventProxy<EVENT>> eventClass) {
        checkInit();
        return defaultEventHandler.post(eventClass);
    }

    public static <EVENT extends IRemoteEvent> EVENT postRemote(Class<? extends EventProxy<EVENT>> eventClass, String processName) {
        checkInit();
        return defaultEventHandler.postRemote(eventClass, processName);
    }

    static EventProxy getPostMainEventProxy(Class<? extends EventProxy> eventClass) {
        checkInit();
        return defaultEventHandler.getPostMainEventProxy(eventClass);
    }

    public static synchronized IEventHandler withActivity(Activity activity) {
        checkInit();
        EventHandler eventBusImp = eventHandlerMap.get(activity);
        if (eventBusImp == null) {
            eventBusImp = new EventHandler();
            eventHandlerMap.put(activity, eventBusImp);
        }
        return eventBusImp;
    }

    private static void checkInit() {
        if (staticContext == null) {
            throw new RuntimeException("请在Application调用EventBus.init方法进行初始化");
        }
    }
}