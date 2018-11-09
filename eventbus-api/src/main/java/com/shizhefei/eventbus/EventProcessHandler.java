package com.shizhefei.eventbus;

import android.app.Service;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by LuckyJayce
 */
class EventProcessHandler implements IEventHandler {
    private Map<Class, Map<String, IEvent>> eventProxyMap = new HashMap<>();
    private EventHandler eventHandler = new EventHandler();
    private boolean remoteEvent;

    public EventProcessHandler(boolean remoteEvent) {
        this.remoteEvent = remoteEvent;
    }

    @Override
    public <EVENT extends IEvent> EVENT post(Class<EVENT> eventInterface) {
        return eventHandler.post(eventInterface);
    }

    @Override
    public <EVENT extends IEvent> EVENT postMain(Class<EVENT> eventInterface) {
        return eventHandler.postMain(eventInterface);
    }

    @Override
    public void register(IEvent subscriber) {
        if (EventBus.isHasRemoteEvent()) {
            checkServiceRegister();
            Util.bindService();
        }
        eventHandler.register(subscriber);
    }

    @Override
    public void unregister(IEvent subscriber) {
        eventHandler.unregister(subscriber);
    }

    @Override
    public boolean isRegister(IEvent subscriber) {
        return eventHandler.isRegister(subscriber);
    }

    public synchronized <EVENT extends IRemoteEvent> EVENT postRemote(Class<EVENT> eventClass, String processName) {
        if (!remoteEvent) {
            throw new RuntimeException("你在EventBus.init的時候声明不执行跨进程的event,如果要执行跨进程的event改为EventBus.init(context,true)");
        }
        checkServiceRegister();
        Map<String, IEvent> processEventProxyMap = eventProxyMap.get(eventClass);
        if (processEventProxyMap == null) {
            processEventProxyMap = new HashMap<>();
            eventProxyMap.put(eventClass, processEventProxyMap);
        }
        IEvent eventProxy = processEventProxyMap.get(processName);
        if (eventProxy == null) {
            eventProxy = EventBus.getEventProxyFactory().createRemoteProxy(eventClass, processName);
            processEventProxyMap.put(processName, eventProxy);
        }
        return eventClass.cast(eventProxy);
    }

    private boolean hasCheckService;

    private void checkServiceRegister() {
        if (hasCheckService) {
            return;
        }
        if (!isDeclareService(EventBusService.class)) {
            throw new RuntimeException("请在AndroidManifest.xml声明EventBusService");
        }
        hasCheckService = true;
    }

    private boolean isDeclareService(Class<? extends Service> serviceClass) {
        PackageManager pm = EventBus.getContext().getPackageManager();
        try {
            ComponentName component = new ComponentName(EventBus.getContext(), serviceClass);
            ServiceInfo serviceInfo = pm.getServiceInfo(component, 0);
            return serviceInfo != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}