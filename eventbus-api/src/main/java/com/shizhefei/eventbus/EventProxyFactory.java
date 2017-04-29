package com.shizhefei.eventbus;

import java.util.Set;

class EventProxyFactory {

    public static <IEVENT extends IEvent> EventProxy<IEVENT> create(Class<? extends EventProxy<IEVENT>> eventProxyClass, boolean postMainThread, Set<IEVENT> registers) {
        try {
            EventProxy<IEVENT> eventProxy = eventProxyClass.newInstance();
            eventProxy.setEvents(registers);
            eventProxy.setPostMainThread(postMainThread);
            return eventProxy;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}