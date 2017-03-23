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

class EventHandler {

    static final Map<Class<? extends IEvent>, EventProxy> interfaceImpMap = new HashMap<>();

    private final static Map<IEvent, Set<EventProxy>> registers = new HashMap<>();

    public static <IEVENT extends IEvent> IEVENT get(Class<IEVENT> eventClass) {
        return (IEVENT) interfaceImpMap.get(eventClass);
    }

    public static synchronized void register(IEvent iEvent) {
        if (registers.containsKey(iEvent)) {
            return;
        }
        ArrayList<Class<? extends IEvent>> interfaces = Util.getInterfaces(iEvent);
        Set<EventProxy> eventProxySet = new HashSet<>();
        for (Class<? extends IEvent> in : interfaces) {
            EventProxy eventProxy = interfaceImpMap.get(in);
            if (eventProxy != null) {
                eventProxy.register(iEvent);
                eventProxySet.add(eventProxy);
            }
        }
        registers.put(iEvent, eventProxySet);
    }

    public static synchronized void unregister(IEvent iEvent) {
        Set<EventProxy> eventProxySet = registers.remove(iEvent);
        if (eventProxySet != null) {
            for (EventProxy eventProxy : eventProxySet) {
                eventProxy.unregister(iEvent);
            }
        }
    }

    static class EventProxy<IEVENT extends IEvent> implements IEvent {
        Set<IEVENT> iEvents = Collections.newSetFromMap(new ConcurrentHashMap<IEVENT, Boolean>());

        public void register(IEVENT iMessageEvent) {
            iEvents.add(iMessageEvent);
        }

        public void unregister(IEVENT iMessageEvent) {
            iEvents.remove(iMessageEvent);
        }
    }
}
