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

    static final Map<Class<? extends IEvent>, EventImpFactory<? extends IEvent>> factoryMap = new HashMap<>();

    private final Map<Class<? extends IEvent>, EventImpCollections> interfaceImpMap = new HashMap<>();

    private final Map<IEvent, Set<EventImpCollections>> registers = new HashMap<>();

    @Override
    public synchronized <IEVENT extends IEvent> IEVENT post(Class<IEVENT> eventClass) {
        EventImpCollections<IEVENT> eventImpCollections = getEventData(eventClass);
        EventProxy<IEVENT> eventProxy = eventImpCollections.eventProxy;
        if (eventProxy == null) {
            EventImpFactory<? extends IEvent> eventImpFactory = factoryMap.get(eventClass);
            eventProxy = eventImpFactory.create();
            eventProxy.setPostMainThread(false);
            eventProxy.setEvents(eventImpCollections.iEvents);
        }
        eventImpCollections.eventProxy = eventProxy;
        return eventClass.cast(eventProxy);
    }

    @Override
    public synchronized <IEVENT extends IEvent> IEVENT postMain(Class<IEVENT> eventClass) {
        EventImpCollections<IEVENT> eventImpCollections = getEventData(eventClass);
        EventProxy<IEVENT> eventProxy = eventImpCollections.mainEventProxy;
        if (eventProxy == null) {
            EventImpFactory<? extends IEvent> eventImpFactory = factoryMap.get(eventClass);
            eventProxy = eventImpFactory.create();
            eventProxy.setPostMainThread(true);
            eventProxy.setEvents(eventImpCollections.iEvents);
        }
        eventImpCollections.mainEventProxy = eventProxy;
        return eventClass.cast(eventProxy);
    }

    private synchronized <IEVENT extends IEvent> EventImpCollections<IEVENT> getEventData(Class<IEVENT> eventClass) {
        EventImpCollections eventImpCollections = interfaceImpMap.get(eventClass);
        if (eventImpCollections == null) {
            eventImpCollections = new EventImpCollections();
            interfaceImpMap.put(eventClass, eventImpCollections);
        }
        return eventImpCollections;
    }


    @Override
    public synchronized void register(IEvent subscriber) {
        if (registers.containsKey(subscriber)) {
            return;
        }
        ArrayList<Class<? extends IEvent>> interfaces = Util.getInterfaces(subscriber);
        Set<EventImpCollections> eventProxySet = new HashSet<>();
        for (Class<? extends IEvent> in : interfaces) {
            EventImpCollections eventProxy = getEventData(in);
            if (eventProxy != null) {
                eventProxy.register(subscriber);
                eventProxySet.add(eventProxy);
            }
        }
        registers.put(subscriber, eventProxySet);
    }

    public synchronized void unregister(IEvent subscriber) {
        Set<EventImpCollections> eventProxySet = registers.remove(subscriber);
        if (eventProxySet != null) {
            for (EventImpCollections eventProxy : eventProxySet) {
                eventProxy.unregister(subscriber);
            }
        }
    }

    @Override
    public synchronized boolean isRegister(IEvent subscriber) {
        return registers.containsKey(subscriber);
    }

     interface EventImpFactory<IEVENT extends EventProxy> {

         IEVENT create();

    }

    static class EventProxy<IEVENT extends IEvent> implements IEvent {
        private boolean postMainThread;
        Set<IEVENT> iEvents;

        public void setPostMainThread(boolean postMainThread) {
            this.postMainThread = postMainThread;
        }

        public boolean isPostMainThread() {
            return postMainThread;
        }

        public void setEvents(Set<IEVENT> events) {
            this.iEvents = events;
        }
    }

    private static class EventImpCollections<IEVENT extends IEvent> {
        EventProxy<IEVENT> eventProxy;
        EventProxy<IEVENT> mainEventProxy;

        Set<IEVENT> iEvents = Collections.newSetFromMap(new ConcurrentHashMap<IEVENT, Boolean>());

        void register(IEVENT iMessageEvent) {
            iEvents.add(iMessageEvent);
        }

        void unregister(IEVENT iMessageEvent) {
            iEvents.remove(iMessageEvent);
        }
    }
}
