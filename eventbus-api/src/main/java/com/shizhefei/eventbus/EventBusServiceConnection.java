package com.shizhefei.eventbus;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Pair;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by LuckyJayce on 2017/5/7.
 */

class EventBusServiceConnection implements ServiceConnection {
    private volatile EventServiceExecutor eventServiceExecutor;
    private Queue<Pair<String, Bundle>> eventDataQueue = new ConcurrentLinkedQueue<>();
    private String currentProcessName;
    private int currentProcessId = -1;

    public boolean isConnected() {
        return eventServiceExecutor != null;
    }

    //    Context bindService 中ServiceConnection 的回调（onServiceConnected与onServiceDisconnected）一定是在主线程中执行，
    // 回调操作会被丢到主线程的消息队列中，因此只有主线程中断后，回调消息才能运行。
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        eventServiceExecutor = EventServiceExecutor.Stub.asInterface(service);
        try {
            eventServiceExecutor.register(getCurrentProcessId(), getCurrentProcessName(), new ProcessEventRemoteExecutor());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Pair<String, Bundle> bundle;
        while ((bundle = eventDataQueue.poll()) != null) {
            try {
                eventServiceExecutor.postEvent(bundle.first, bundle.second);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        eventServiceExecutor = null;
    }

    public int getCurrentProcessId() {
        if (currentProcessId == -1) {
            iniProcessInfo();
        }
        return currentProcessId;
    }

    public String getCurrentProcessName() {
        if (currentProcessId == -1) {
            iniProcessInfo();
        }
        return currentProcessName;
    }

    private void iniProcessInfo() {
        int pid = android.os.Process.myPid();
        String processName = null;
        ActivityManager activityManager = (ActivityManager) EventBus.getContext().getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : list) {
            if (runningAppProcessInfo.pid == pid) {
                processName = runningAppProcessInfo.processName;
                break;
            }
        }
        currentProcessId = pid;
        currentProcessName = processName;
    }

    public void postEvent(String processName, Bundle eventRemoteData) {
        if (isConnected()) {
            try {
                eventServiceExecutor.postEvent(processName, eventRemoteData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            eventDataQueue.add(new Pair<>(processName, eventRemoteData));
            bindService();
        }
    }

    public void bindService() {
        if (!isConnected()) {
            Intent intent = new Intent(EventBus.getContext(), EventBusService.class);
            EventBus.getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
        }
    }

    public void unBindService() {
        if (!isConnected()) {
            EventBus.getContext().unbindService(this);
        }
    }

    private static class ProcessEventRemoteExecutor extends EventProcessExecutor.Stub {
        @Override
        public void postEvent(Bundle remoteEventData) throws RemoteException {
            String eventProxyClassName = remoteEventData.getString("eventProxyClassName");
            try {
                Class clas = Class.forName(eventProxyClassName);
                EventBus.getPostMainEventProxy(clas).onRemoteEvent(remoteEventData);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
