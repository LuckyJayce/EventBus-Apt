package com.shizhefei.eventbus;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by LuckyJayce on 2016/7/23.
 */
public class Util {

    @SuppressWarnings("unchecked")
    public static ArrayList<Class<? extends IEvent>> getInterfaces(IEvent event) {
        Class<?>[] interfaces = event.getClass().getInterfaces();
        ArrayList<Class<? extends IEvent>> eventClass = new ArrayList<>();
        for (Class<?> in : interfaces) {
            if (isExtendsInterface(in, IEvent.class)) {
                eventClass.add((Class<? extends IEvent>) in);
            }
        }
        return eventClass;
    }


    public static boolean isExtendsInterface(Class<?> in, Class<?> superClass) {
        return superClass.isAssignableFrom(in);
    }

    /**
     * 判断是直接执行，还是需要post一个runnable
     *
     * @param postMainThread
     * @return true 直接执行，false 需要post一个runnable
     */
    public static boolean isSyncInvoke(boolean postMainThread, int receiveThreadMode) {
        boolean isMainThread = Looper.getMainLooper() == Looper.myLooper();
        boolean isInv;
        switch (receiveThreadMode) {
            case Subscribe.POSTING:
            default:
                if (postMainThread) {
                    if (isMainThread) {
                        isInv = true;
                    } else {
                        isInv = false;
                    }
                } else {
                    isInv = true;
                }
                break;
            case Subscribe.MAIN:
                isInv = isMainThread;
                break;
            case Subscribe.BACKGROUND:
                isInv = !isMainThread;
                break;
            case Subscribe.ASYNC:
                isInv = false;
                break;
        }
        return isInv;
    }

    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    public static void postMain(Runnable runnable) {
        mainHandler.post(runnable);
    }

    public static void postThread(Runnable runnable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(runnable);
        }
    }

    public static void postRemote(Class<? extends EventProxy> eventProxyClass, String methodName, Class[] methodParamsType, Object[] objects) {
//        EventRemoteData data = new EventRemoteData(eventProxyClass.getName(), methodName, methodParamsType, objects);
    }

    public static void postRemote(String processName, Bundle eventRemoteData) {
        if (isProcessRunning(EventBus.staticContext, processName)) {

        }
    }

    public static boolean isProcessRunning(Context context, String processName) {
        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> lists = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : lists) {
            if (info.processName.equals(processName)) {
                //Log.i("Service2进程", ""+info.processName);
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
}
