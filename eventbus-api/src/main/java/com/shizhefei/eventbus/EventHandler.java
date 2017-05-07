package com.shizhefei.eventbus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by LuckyJayce on 2017/3/21.
 */

class EventHandler implements IEventHandler {

    private final Map<Class<? extends IEvent>, EventProxyCollections> interfaceImpMap = new HashMap<>();

    private final Map<IEvent, Set<EventProxyCollections>> registers = new HashMap<>();

    synchronized <EVENT extends IEvent> EventProxy<EVENT> getPostMainEventProxy(Class<? extends EventProxy<EVENT>> eventProxyClass) {
        return post(eventProxyClass, true);
    }

    @Override
    public <EVENT extends IEvent> EVENT post(Class<? extends EventProxy<EVENT>> eventProxyClass) {
        return (EVENT) post(eventProxyClass, false);
    }

    @Override
    public <EVENT extends IEvent> EVENT postMain(Class<? extends EventProxy<EVENT>> eventProxyClass) {
        return (EVENT) post(eventProxyClass, true);
    }

    private synchronized <EVENT extends IEvent> EventProxy<EVENT> post(Class<? extends EventProxy<EVENT>> eventProxyClass, boolean postMainThread) {
        Class<EVENT> eventClass = Util.getEventClass(eventProxyClass);
        EventProxyCollections<EVENT> eventProxyCollections = getEventImpCollections(eventClass);
        EventProxy<EVENT> eventProxy = postMainThread ? eventProxyCollections.mainEventProxy : eventProxyCollections.eventProxy;
        if (eventProxy == null) {
            eventProxy = EventProxyFactory.createLocal(eventProxyClass, postMainThread, eventProxyCollections.iEvents);
            if (postMainThread) {
                eventProxyCollections.mainEventProxy = eventProxy;
            } else {
                eventProxyCollections.eventProxy = eventProxy;
            }
        }
        return eventProxy;
    }

    private synchronized <EVENT extends IEvent> EventProxyCollections<EVENT> getEventImpCollections(Class<EVENT> eventClass) {
        EventProxyCollections eventProxyCollections = interfaceImpMap.get(eventClass);
        if (eventProxyCollections == null) {
            eventProxyCollections = new EventProxyCollections();
            interfaceImpMap.put(eventClass, eventProxyCollections);
        }
        return eventProxyCollections;
    }


    @Override
    public synchronized void register(IEvent subscriber) {
        if (registers.containsKey(subscriber)) {
            return;
        }
        ArrayList<Class<? extends IEvent>> interfaces = Util.getInterfaces(subscriber);
        Set<EventProxyCollections> eventProxySet = new HashSet<>();
        for (Class<? extends IEvent> in : interfaces) {
            EventProxyCollections eventProxy = getEventImpCollections(in);
            if (eventProxy != null) {
                eventProxy.register(subscriber);
                eventProxySet.add(eventProxy);
            }
        }
        registers.put(subscriber, eventProxySet);
    }

    public synchronized void unregister(IEvent subscriber) {
        Set<EventProxyCollections> eventProxySet = registers.remove(subscriber);
        if (eventProxySet != null) {
            for (EventProxyCollections eventProxy : eventProxySet) {
                eventProxy.unregister(subscriber);
            }
        }
    }

    @Override
    public synchronized boolean isRegister(IEvent subscriber) {
        return registers.containsKey(subscriber);
    }

    private static class EventProxyCollections<EVENT extends IEvent> {
        EventProxy<EVENT> eventProxy;
        EventProxy<EVENT> mainEventProxy;

        Set<EVENT> iEvents = Collections.newSetFromMap(new ConcurrentHashMap<EVENT, Boolean>());

        void register(EVENT iMessageEvent) {
            iEvents.add(iMessageEvent);
        }

        void unregister(EVENT iMessageEvent) {
            iEvents.remove(iMessageEvent);
        }
    }
}
