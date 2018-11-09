package com.shizhefei.eventbus;

import android.app.Activity;
import android.content.Context;

import java.util.WeakHashMap;

/**
 * Created by LuckyJayce
 */
public class EventBus {

    private static EventProcessHandler defaultEventHandler;
    private static WeakHashMap<Activity, EventHandler> eventHandlerMap;
    private static EventHandler emptyEventHandler = new EventHandler();
    private static Context staticContext;
    private static boolean staticHasRemoteEvent;
    volatile static IEventProxyFactory staticEventProxyFactory;

    public static void init(Context context, boolean hasRemoteEvent) {
        staticContext = context.getApplicationContext();
        staticHasRemoteEvent = hasRemoteEvent;
        eventHandlerMap = new WeakHashMap<>();
        defaultEventHandler = new EventProcessHandler(hasRemoteEvent);
        staticEventProxyFactory = new EventProxyAptFactory();
    }

    public static void setEventProxyFactory(IEventProxyFactory eventProxyFactory){
        staticEventProxyFactory = eventProxyFactory;
    }

    static IEventProxyFactory getEventProxyFactory(){
        return staticEventProxyFactory;
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

    public static <EVENT extends IEvent> EVENT postMain(Class<EVENT> eventInterface) {
        checkInit();
        return defaultEventHandler.postMain(eventInterface);
    }

    public static <EVENT extends IEvent> EVENT post(Class<EVENT> eventInterface) {
        checkInit();
        return defaultEventHandler.post(eventInterface);
    }

    public static <EVENT extends IRemoteEvent> EVENT postRemote(Class<EVENT> eventInterface, String processName) {
        checkInit();
        return defaultEventHandler.postRemote(eventInterface, processName);
    }

//    public static <EVENT extends IEvent> EVENT postMainInActivity(Activity activity, Class<EVENT> eventInterface) {
//        return withActivity(activity).postMain(eventInterface);
//    }
//
//    public static <EVENT extends IEvent> EVENT postInActivity(Activity activity, Class<EVENT> eventInterface) {
//        return withActivity(activity).postMain(eventInterface);
//    }

    public static synchronized IEventHandler withActivity(Activity activity) {
        checkInit();
        if (activity == null) {
            return emptyEventHandler;
        }
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