package com.shizhefei.eventbus;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主进程的service，所有进程和主进程连接，其它进程互不连接
 * processA 发送事件到main, processA -> main   绑定主进程的EventBusService，通过aidl发送事件到main
 * main 发送事件到processA, main -> processA   判断processA 是否有连接，没有连接就不需要发布事件，有连接就取到processA的进程的aidl的binder对象发布事件
 * processA发送事件到processB， processA -> main -> processB 绑定主进程的EventBusService,判断processB 是否有连接，没有连接就不需要发布事件，有连接就取到processB的进程的aidl的binder对象发布事件
 * processA发送事件到processB， processB -> main -> processA 绑定主进程的EventBusService,判断processA 是否有连接，没有连接就不需要发布事件，有连接就取到processA的进程的aidl的binder对象发布事件
 * <p>
 * 非主进程的EventBus.register和EventBus.postRemote的时候去绑定主进程的EventBusService,也就是说其他进程要发布事件到该进程，如果该进程没有绑定说明该进程没有注册接收者，根本不需要发送事件到该进程
 * <p/>
 */
public class EventBusService extends Service {

    private RemoteCallbackList<EventProcessExecutor> callbackList = new RemoteCallbackList<>();

    //由AIDL文件生成的BookManager
    private final EventServiceExecutor.Stub eventRemote = new EventServiceExecutor.Stub() {
        @Override
        public void register(int pid, String processName, EventProcessExecutor processExecutor) throws RemoteException {
            callbackList.register(processExecutor, new ProcessData(pid, processName));
        }

        @Override
        public void unregister(EventProcessExecutor processExecutor) throws RemoteException {
            callbackList.unregister(processExecutor);
        }

        @Override
        public void postEvent(String processName, Bundle remoteEventData) throws RemoteException {
            if (TextUtils.isEmpty(processName)) {
                final int n = callbackList.beginBroadcast();
                try {
                    for (int i = 0; i < n; i++) {
                        callbackList.getBroadcastItem(i).postEvent(remoteEventData);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                } finally {
                    callbackList.finishBroadcast();
                }
            } else {
                final int n = callbackList.beginBroadcast();
                try {
                    for (int i = 0; i < n; i++) {
                        ProcessData processData = (ProcessData) callbackList.getBroadcastCookie(i);
                        if (processData.processName.equals(processName)) {
                            callbackList.getBroadcastItem(i).postEvent(remoteEventData);
                            break;
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                } finally {
                    callbackList.finishBroadcast();
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return eventRemote;
    }


    private class ProcessData {
        private int pid;
        private String processName;

        public ProcessData(int pid, String processName) {
            this.pid = pid;
            this.processName = processName;
        }
    }
}
