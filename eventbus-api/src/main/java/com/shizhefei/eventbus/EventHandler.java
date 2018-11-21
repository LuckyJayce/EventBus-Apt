package com.shizhefei.eventbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by LuckyJayce
 */

class EventHandler implements IEventHandler {

    /**
     * key 为 事件类型的接口
     * value 为 事件对应的【注册对象集合】<br/>
     * <br/>
     * 为了方便查找事件接口 有哪些注册对象
     */
    private final Map<Class<? extends IEvent>, EventProxyCollections> interfaceImpMap = new HashMap<>();

    /**
     * key 为注册接收事件的对象
     * value 接收事件的对象 有注册哪些类型的【注册对象集合】的set集合<br/>
     * <br/>
     * 为了方便查找有 注册对象实现了哪些事件接口
     */
    private final Map<IEvent, Set<EventProxyCollections>> registers = new HashMap<>();

    @Override
    public <EVENT extends IEvent> EVENT post(Class<EVENT> eventInterface) {
        return post(eventInterface, false);
    }

    @Override
    public <EVENT extends IEvent> EVENT postMain(Class<EVENT> eventInterface) {
        return post(eventInterface, true);
    }

    private synchronized <EVENT extends IEvent> EVENT post(Class<EVENT> eventInterface, boolean postMainThread) {
        EventProxyCollections<EVENT> eventProxyCollections = getEventImpCollections(eventInterface);
        EVENT eventProxy = postMainThread ? eventProxyCollections.mainEventProxy : eventProxyCollections.eventProxy;
        if (eventProxy == null) {
            eventProxy = EventBus.getEventProxyFactory().createLocalProxy(eventInterface, postMainThread, eventProxyCollections.registers);
            if (postMainThread) {
                eventProxyCollections.mainEventProxy = eventProxy;
            } else {
                eventProxyCollections.eventProxy = eventProxy;
            }
        }
        return eventProxy;
    }

    private synchronized <EVENT extends IEvent> EventProxyCollections<EVENT> getEventImpCollections(Class<EVENT> eventInterface) {
        EventProxyCollections eventProxyCollections = interfaceImpMap.get(eventInterface);
        if (eventProxyCollections == null) {
            eventProxyCollections = new EventProxyCollections();
            interfaceImpMap.put(eventInterface, eventProxyCollections);
        }
        return eventProxyCollections;
    }


    @Override
    public synchronized void register(IEvent register) {
        if (registers.containsKey(register)) {
            return;
        }
        ArrayList<Class<? extends IEvent>> interfaces = Util.getInterfaces(register);
        Set<EventProxyCollections> eventProxySet = new HashSet<>();
        for (Class<? extends IEvent> in : interfaces) {
            EventProxyCollections eventProxyCollections = getEventImpCollections(in);
            eventProxyCollections.addRegister(register);
            eventProxySet.add(eventProxyCollections);
        }
        registers.put(register, eventProxySet);
    }

    public synchronized void unregister(IEvent register) {
        Set<EventProxyCollections> eventProxySet = registers.remove(register);
        if (eventProxySet != null) {
            for (EventProxyCollections proxyCollections : eventProxySet) {
                proxyCollections.removeRegister(register);
            }
        }
    }

    @Override
    public synchronized boolean isRegister(IEvent subscriber) {
        return registers.containsKey(subscriber);
    }

    private static class EventProxyCollections<EVENT extends IEvent> {
        EVENT eventProxy;
        EVENT mainEventProxy;
        Queue<Register<EVENT>> registers;

        public EventProxyCollections() {
            registers = new ConcurrentLinkedQueue<>();
        }

        void addRegister(EVENT event) {
            registers.add(new Register<>(event));
        }

        void removeRegister(EVENT event) {
            for (Register<EVENT> register : registers) {
                if (register.getEvent().equals(event)) {
                    registers.remove(register);
                    register.setEvent(null);
                    break;
                }
            }
        }
    }
}
