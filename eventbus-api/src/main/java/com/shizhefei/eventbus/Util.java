package com.shizhefei.eventbus;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;


/**
 * Created by LuckyJayce on 2016/7/23.
 */
class Util {

    @SuppressWarnings("unchecked")
    static ArrayList<Class<? extends IEvent>> getInterfaces(IEvent event) {
        Class<?>[] interfaces = event.getClass().getInterfaces();
        ArrayList<Class<? extends IEvent>> eventClass = new ArrayList<>();
        for (Class<?> in : interfaces) {
            if (isExtendsInterface(in, IEvent.class)) {
                eventClass.add((Class<? extends IEvent>) in);
            }
        }
        return eventClass;
    }


    static boolean isExtendsInterface(Class<?> in, Class<?> superClass) {
        return superClass.isAssignableFrom(in);
    }

    /**
     * 判断是直接执行，还是需要post一个runnable
     * @param subscribe
     * @return true 直接执行，false 需要post一个runnable
     */
    static boolean isSyncInvoke(Subscribe subscribe) {
        if (subscribe == null) {
            return true;
        }
        boolean isMainThread = Looper.getMainLooper() == Looper.myLooper();
        boolean isInv;
        switch (subscribe.threadMode()) {
            case Subscribe.POSTING:
            default:
                isInv = true;
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

    static void postMain(Runnable runnable){
        mainHandler.post(runnable);
    }

    static void postThread(Runnable runnable){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(runnable);
        }
    }
}
