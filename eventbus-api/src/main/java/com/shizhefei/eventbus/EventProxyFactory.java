package com.shizhefei.eventbus;

import java.util.Set;

class EventProxyFactory {

    public static <EVENT extends IEvent> EventProxy<EVENT> create(Class<? extends EventProxy<EVENT>> eventProxyClass, boolean postMainThread, String processName, Set<EVENT> registers) {
        try {
            EventProxy<EVENT> eventProxy = eventProxyClass.newInstance();
            eventProxy.setEvents(registers);
            eventProxy.setPostMainThread(postMainThread);
            eventProxy.setProcessName(processName);
            return eventProxy;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}