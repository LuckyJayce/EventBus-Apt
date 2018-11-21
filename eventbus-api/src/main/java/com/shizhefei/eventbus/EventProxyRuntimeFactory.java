package com.shizhefei.eventbus;

import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 通过动态代理实现生成 实现EVENT的分发事件的代理类
 * Created by luckyjayce on 2017/9/25.
 */
public class EventProxyRuntimeFactory implements IEventProxyFactory {

    private static final String METHOD_NAME = "methodName";
    private static final String METHOD_PARAM_TYPES = "methodParamTypes";
    private static final String EVENT_INTERFACE_CLASS = "event_interface_class";
    private static final String METHOD_ARGS = "methodArgs";

    private static volatile Map<Class<?>, PutArg> bundleMethodNames;

    @Override
    public <EVENT extends IEvent> EVENT createLocalProxy(Class<EVENT> eventInterfaceClass, boolean postMainThread, Collection<Register<EVENT>> registers) {
        return (EVENT) Proxy.newProxyInstance(eventInterfaceClass.getClassLoader(), new Class<?>[]{eventInterfaceClass}, new ProxyInvocationHandler(eventInterfaceClass, postMainThread, registers));
    }

    @Override
    public <EVENT extends IEvent> EVENT createRemoteProxy(Class<EVENT> eventInterfaceClass, String processName) {
        return (EVENT) Proxy.newProxyInstance(eventInterfaceClass.getClassLoader(), new Class<?>[]{eventInterfaceClass}, new ProxyInvocationHandler(eventInterfaceClass, processName));
    }

    @Override
    public void onRemoteEvent(Bundle eventRemoteData) {
        //执行跨进程的调用
        Class<? extends IEvent> proxyClass = (Class<? extends IEvent>) eventRemoteData.getSerializable(EVENT_INTERFACE_CLASS);
        String methodName = eventRemoteData.getString(METHOD_NAME);
        Class<?>[] methodParamTypes = (Class<?>[]) eventRemoteData.getSerializable(METHOD_PARAM_TYPES);
        Bundle methodArgs = eventRemoteData.getBundle(METHOD_ARGS);
        IEvent proxy = EventBus.postMain(proxyClass);
        try {
            Method method = proxyClass.getMethod(methodName, methodParamTypes);
            Object[] args = null;
            if (methodArgs != null) {
                args = new Object[methodArgs.size()];
                Set<String> keySet = methodArgs.keySet();
                for (String name : keySet) {
                    Integer index = Integer.parseInt(name);
                    args[index] = methodArgs.get(name);
                    Class<?> methodParamType = methodParamTypes[index];
                    if (IInterface.class.isAssignableFrom(methodParamType)) {
                        Class<?> aClass = Class.forName(methodParamType.getName() + "$Stub");
                        Method asInterface = aClass.getMethod("asInterface", IBinder.class);
                        args[index] = asInterface.invoke(aClass, args[index]);
                    }
                }
            }
            if (args != null) {
                method.invoke(proxy, args);
            } else {
                method.invoke(proxy);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 消息发送的实现类
     */
    private static class ProxyInvocationHandler<EVENT extends IEvent> implements InvocationHandler {
        private final Class<EVENT> eventInterfaceClass;
        private boolean isPostMainThread;
        private Collection<Register<EVENT>> registers;
        private String processName;

        public ProxyInvocationHandler(Class<EVENT> eventInterfaceClass, boolean postMainThread, Collection<Register<EVENT>> registers) {
            this.isPostMainThread = postMainThread;
            this.registers = registers;
            this.eventInterfaceClass = eventInterfaceClass;
        }

        public ProxyInvocationHandler(Class<EVENT> eventInterfaceClass, String processName) {
            this.processName = processName;
            this.eventInterfaceClass = eventInterfaceClass;
        }

        @Override
        public Object invoke(Object proxy, final Method method, final Object... args) throws Throwable {
            if (!TextUtils.isEmpty(processName)) {//执行跨进程的
                Bundle eventRemoteData = new Bundle();
                eventRemoteData.putSerializable(EVENT_INTERFACE_CLASS, eventInterfaceClass);
                eventRemoteData.putString(METHOD_NAME, method.getName());
                Class<?>[] parameterTypes = method.getParameterTypes();
                eventRemoteData.putSerializable(METHOD_PARAM_TYPES, parameterTypes);
                if (args != null) {
                    Bundle methodArgs = new Bundle();
                    for (int i = 0; i < args.length; i++) {
                        Object arg = args[i];
                        Class<?> argClass = arg.getClass();
                        String paramName = String.valueOf(i);
                        PutArg putArg = getBundlePutMethodName(eventInterfaceClass, argClass);
                        putArg.put(methodArgs, paramName, arg);
                    }
                    eventRemoteData.putBundle(METHOD_ARGS, methodArgs);
                }
                Util.postRemote(processName, eventRemoteData);
            } else {
                IEvent.Filter filter = null;
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes != null && parameterTypes.length > 0) {
                    Class<?> parameterType = parameterTypes[0];
                    if (IEvent.Filter.class.equals(parameterType)) {
                        filter = (IEvent.Filter) args[0];
                    }
                }
                for (final Register<EVENT> register : registers) {
                    if (filter == null || filter.accept(register.getEvent())) {
                        Util.post(register.getEvent(), isPostMainThread, new Runnable() {
                            @Override
                            public void run() {
                                EVENT event = register.getEvent();
                                if (event != null) {
                                    try {
                                        method.invoke(register.getEvent(), args);
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                }
            }
            return null;
        }
    }

    private static PutArg getBundlePutMethodName(Class<?> eventInterfaceClass, Class<?> typeMirror) {
        Map<Class<?>, PutArg> bundleMethodNames = getBundleMethodNames();
        PutArg putArg = bundleMethodNames.get(typeMirror);
        if (putArg != null) {
            return putArg;
        }
        if (Parcelable.class.isAssignableFrom(typeMirror)) {
            return bundleMethodNames.get(Parcelable.class);
        }
        if (IBinder.class.isAssignableFrom(typeMirror)) {
            return bundleMethodNames.get(IBinder.class);
        }
        if (typeMirror.isArray()) {
            Class<?> componentType = typeMirror.getComponentType();
            if (Parcelable.class.isAssignableFrom(componentType)) {
                return bundleMethodNames.get(Parcelable[].class);
            }
        }
        if (isSerializableType(typeMirror)) {
            return bundleMethodNames.get(Serializable.class);
        }
        throw new RuntimeException("eventInterfaceClass:" + eventInterfaceClass + "：不支持的参数类型:" + typeMirror);
    }

    private static Map<Class<?>, PutArg> getBundleMethodNames() {
        if (bundleMethodNames == null) {
            synchronized (EventProxyRuntimeFactory.class) {
                if (bundleMethodNames == null) {
                    bundleMethodNames = new HashMap<>();
                    bundleMethodNames.put(Integer.class, new PutArg() {

                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putInt(paramName, (Integer) arg);
                        }
                    });
                    bundleMethodNames.put(String.class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putString(paramName, arg == null ? null : arg.toString());
                        }
                    });
                    bundleMethodNames.put(Float.class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putFloat(paramName, (Float) arg);
                        }

                    });
                    bundleMethodNames.put(Double.class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putDouble(paramName, (Double) arg);
                        }
                    });
                    bundleMethodNames.put(Short.class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putShort(paramName, (Short) arg);
                        }
                    });
                    bundleMethodNames.put(char.class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putChar(paramName, (char) arg);
                        }
                    });
                    bundleMethodNames.put(Character.class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putChar(paramName, (Character) arg);
                        }
                    });
                    bundleMethodNames.put(Long.class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putLong(paramName, (Long) arg);
                        }
                    });
                    bundleMethodNames.put(Byte.class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putByte(paramName, (Byte) arg);
                        }
                    });
                    bundleMethodNames.put(Boolean.class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putBoolean(paramName, (Boolean) arg);
                        }
                    });
                    bundleMethodNames.put(int[].class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putIntArray(paramName, (int[]) arg);
                        }
                    });
                    bundleMethodNames.put(String[].class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putStringArray(paramName, (String[]) arg);
                        }
                    });
                    bundleMethodNames.put(float[].class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putFloatArray(paramName, (float[]) arg);
                        }
                    });
                    bundleMethodNames.put(double[].class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putDoubleArray(paramName, (double[]) arg);
                        }
                    });
                    bundleMethodNames.put(short[].class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putShortArray(paramName, (short[]) arg);
                        }
                    });
                    bundleMethodNames.put(char[].class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putCharArray(paramName, (char[]) arg);
                        }
                    });
                    bundleMethodNames.put(long[].class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putLongArray(paramName, (long[]) arg);
                        }
                    });
                    bundleMethodNames.put(byte[].class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putByteArray(paramName, (byte[]) arg);
                        }
                    });
                    bundleMethodNames.put(boolean[].class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putBooleanArray(paramName, (boolean[]) arg);
                        }
                    });
                    bundleMethodNames.put(CharSequence.class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putCharSequence(paramName, (CharSequence) arg);
                        }
                    });
                    bundleMethodNames.put(CharSequence[].class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putCharSequenceArray(paramName, (CharSequence[]) arg);
                        }
                    });
                    bundleMethodNames.put(Parcelable.class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putParcelable(paramName, (Parcelable) arg);
                        }
                    });
                    bundleMethodNames.put(Parcelable[].class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putParcelableArray(paramName, (Parcelable[]) arg);
                        }
                    });
                    bundleMethodNames.put(Bundle.class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putBundle(paramName, (Bundle) arg);
                        }
                    });
                    bundleMethodNames.put(Serializable.class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putSerializable(paramName, (Serializable) arg);
                        }
                    });
                    bundleMethodNames.put(Serializable[].class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            bundle.putSerializable(paramName, (Serializable[]) arg);
                        }
                    });
                    bundleMethodNames.put(IBinder.class, new PutArg() {
                        @Override
                        public void put(Bundle bundle, String paramName, Object arg) {
                            Util.putBinder(bundle, paramName, (IBinder) arg);
                        }
                    });
                }
            }
        }
        return bundleMethodNames;
    }

    private static boolean isSerializableType(Class<?> typeMirror) {
        //由于非序列化的类数组也是serializable，但是又序列化不了，所有要判断数组的单个类的类型
        if (typeMirror.isArray()) {
            Class<?> componentType = typeMirror.getComponentType();
            return isSerializableType(componentType);
        }
        return Serializable.class.isAssignableFrom(typeMirror);
    }

    private static abstract class PutArg {
        abstract void put(Bundle bundle, String paramName, Object arg);

        public Object get(Bundle bundle, String paramName) {
            return bundle.get(paramName);
        }
    }
}
