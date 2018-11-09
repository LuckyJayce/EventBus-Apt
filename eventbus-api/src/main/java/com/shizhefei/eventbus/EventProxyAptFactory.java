package com.shizhefei.eventbus;

import android.os.Bundle;

import java.util.Map;

/**
 * Apt插件实现生成 实现EVENT的分发事件的代理类
 * Created by LuckyJayce
 */
public class EventProxyAptFactory implements IEventProxyFactory {

    @Override
    public <EVENT extends IEvent> EVENT createLocalProxy(Class<EVENT> eventClass, boolean postMainThread, Map<EVENT, Register<EVENT>> registers) {
        try {
            String eventProxyClassName = EventProxyNameBuilder.getProxyClassName(eventClass);
            Class<?> eventProxyClass = Class.forName(eventProxyClassName);
            EventProxy<EVENT> eventProxy = (EventProxy<EVENT>) eventProxyClass.newInstance();
            eventProxy.setEvents(registers);
            eventProxy.setPostMainThread(postMainThread);
            return (EVENT) eventProxy;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("请重新build生成eventClass" + eventClass, e);
        }
    }

    @Override
    public <EVENT extends IEvent> EVENT createRemoteProxy(Class<EVENT> eventClass, String processName) {
        try {
            String eventProxyClassName = EventProxyNameBuilder.getProxyClassName(eventClass);
            Class<?> eventProxyClass = Class.forName(eventProxyClassName);
            EventProxy<EVENT> eventProxy = (EventProxy<EVENT>) eventProxyClass.newInstance();
            eventProxy.setProcessName(processName);
            return (EVENT) eventProxy;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("请重新build生成eventClass" + eventClass, e);
        }
    }

    @Override
    public void onRemoteEvent(Bundle remoteEventData) {
        String eventProxyClassName = remoteEventData.getString(EventProxyNameBuilder.getRemoteClassParamName());
        try {
            Class<? extends IEvent> clas = (Class<? extends IEvent>) Class.forName(eventProxyClassName);
            EventProxy proxy = (EventProxy) EventBus.postMain(clas);
            proxy.onRemoteEvent(remoteEventData);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}