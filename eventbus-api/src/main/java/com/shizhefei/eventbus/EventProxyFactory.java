package com.shizhefei.eventbus;

import java.util.Set;

class EventProxyFactory {

    public static <EVENT extends IEvent> EventProxy<EVENT> create(Class<? extends EventProxy<EVENT>> eventProxyClass, boolean postMainThread, Set<EVENT> registers) {
        try {
            EventProxy<EVENT> eventProxy = eventProxyClass.newInstance();
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