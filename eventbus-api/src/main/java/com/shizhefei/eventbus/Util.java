package com.shizhefei.eventbus;

import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;


/**
 * Created by LuckyJayce on 2016/7/23.
 */
public class Util {

    private static EventBusServiceConnection serviceConnection = new EventBusServiceConnection();

    private static volatile String staticProcessName;

    public static String getCurrentProcessName() {
        if (staticProcessName == null) {
            synchronized (Util.class) {
                if (staticProcessName == null) {
                    staticProcessName = getProcessNameByFile();
                }
                if (staticProcessName == null) {
                    staticProcessName = getProcessNameByAppInfo();
                }
            }
        }
        return staticProcessName;
    }

    private static String getProcessNameByAppInfo() {
        int pid = Process.myPid();
        String processName = null;
        ActivityManager activityManager = (ActivityManager) EventBus.getContext().getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        if (list != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : list) {
                if (runningAppProcessInfo.pid == pid) {
                    processName = runningAppProcessInfo.processName;
                    break;
                }
            }
        }
        return processName;
    }

    private static String getProcessNameByFile() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

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

    private static void postMain(Runnable runnable) {
        mainHandler.post(runnable);
    }

    private static void postThread(Runnable runnable) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(runnable);
    }

    public static void post(IEvent iEvent, boolean isPostMainThread, Runnable runnable) {
        if (iEvent == null) {
            return;
        }
        Subscribe subscribe = iEvent.getClass().getAnnotation(Subscribe.class);
        int receiveThreadMode = subscribe == null ? Subscribe.POSTING : subscribe.receiveThreadMode();
        if (Util.isSyncInvoke(isPostMainThread, receiveThreadMode)) {
            runnable.run();
        } else {
            if ((receiveThreadMode == Subscribe.MAIN) || (receiveThreadMode == Subscribe.POSTING && isPostMainThread)) {
                postMain(runnable);
            } else {
                postThread(runnable);
            }
        }
    }

    public static void postRemote(String processName, Bundle eventRemoteData) {
        serviceConnection.postEvent(processName, eventRemoteData);
    }

    static void bindService() {
        serviceConnection.bindService();
    }

    static void unBindService() {
        serviceConnection.unBindService();
    }

    public static boolean isProcessRunning(Context context, String processName) {
        boolean isRunning = false;
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> lists = am.getRunningAppProcesses();
        if (lists != null) {
            for (ActivityManager.RunningAppProcessInfo info : lists) {
                if (info.processName.equals(processName)) {
                    //Log.i("Service2进程", ""+info.processName);
                    isRunning = true;
                    break;
                }
            }
        }
        return isRunning;
    }

    public static <EVENT extends IEvent> Class<EVENT> getEventClass(Class<? extends EventProxy<EVENT>> eventClass) {
        Class<?>[] classes = eventClass.getInterfaces();
        return (Class<EVENT>) classes[0];
    }

//    public static String getEventProxyClassName(String eventClassName) {
//        return (Class<EVENT>) classes[0];
//    }

    public static <EVENT extends IEvent> Class<EVENT> getEventProxyClass(Class<EVENT> eventClass) {
//        eventClass.getComponentType()
        Class<?>[] classes = eventClass.getInterfaces();
        return (Class<EVENT>) classes[0];
    }

    public static IBinder getBinder(Bundle bundle, String key) {
        return Build.VERSION.SDK_INT >= 18 ? bundle.getBinder(key) : BundleCompatBaseImpl.getBinder(bundle, key);
    }

    public static void putBinder(Bundle bundle, String key, IBinder binder) {
        if (Build.VERSION.SDK_INT >= 18) {
            bundle.putBinder(key, binder);
        } else {
            BundleCompatBaseImpl.putBinder(bundle, key, binder);
        }

    }

    static class BundleCompatBaseImpl {
        private static final String TAG = "BundleCompatBaseImpl";
        private static Method sGetIBinderMethod;
        private static boolean sGetIBinderMethodFetched;
        private static Method sPutIBinderMethod;
        private static boolean sPutIBinderMethodFetched;

        BundleCompatBaseImpl() {
        }

        public static IBinder getBinder(Bundle bundle, String key) {
            if (!sGetIBinderMethodFetched) {
                try {
                    sGetIBinderMethod = Bundle.class.getMethod("getIBinder", new Class[]{String.class});
                    sGetIBinderMethod.setAccessible(true);
                } catch (NoSuchMethodException var3) {
                    Log.i("BundleCompatBaseImpl", "Failed to retrieve getIBinder method", var3);
                }

                sGetIBinderMethodFetched = true;
            }

            if (sGetIBinderMethod != null) {
                try {
                    return (IBinder) sGetIBinderMethod.invoke(bundle, new Object[]{key});
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException var4) {
                    Log.i("BundleCompatBaseImpl", "Failed to invoke getIBinder via reflection", var4);
                    sGetIBinderMethod = null;
                }
            }

            return null;
        }

        public static void putBinder(Bundle bundle, String key, IBinder binder) {
            if (!sPutIBinderMethodFetched) {
                try {
                    sPutIBinderMethod = Bundle.class.getMethod("putIBinder", new Class[]{String.class, IBinder.class});
                    sPutIBinderMethod.setAccessible(true);
                } catch (NoSuchMethodException var5) {
                    Log.i("BundleCompatBaseImpl", "Failed to retrieve putIBinder method", var5);
                }

                sPutIBinderMethodFetched = true;
            }

            if (sPutIBinderMethod != null) {
                try {
                    sPutIBinderMethod.invoke(bundle, new Object[]{key, binder});
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException var4) {
                    Log.i("BundleCompatBaseImpl", "Failed to invoke putIBinder via reflection", var4);
                    sPutIBinderMethod = null;
                }
            }

        }
    }
}
