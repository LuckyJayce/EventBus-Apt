package com.shizhefei.eventbus;

/**
 * Created by luckyjayce on 2017/9/23.
 */

public class EventProxyNameBuilder {

    public static String getProxyClassName(Class<? extends IEvent> eventClass) {
        return getProxyClassName(eventClass.getPackage().getName(), eventClass.getName());
    }

    public static String getProxyClassName(String packageName, String interfaceEventClassName) {
        return packageName + "." + getProxySimpleClassName(packageName, interfaceEventClassName);
    }

    public static String getProxySimpleClassName(String packageName, String interfaceEventClassName) {
        String withoutPackageClassName = interfaceEventClassName.substring(packageName.length() + 1);
        withoutPackageClassName = withoutPackageClassName.replaceAll("\\$", "_");
        withoutPackageClassName = withoutPackageClassName.replaceAll("\\.", "_");
        String name = withoutPackageClassName + "Proxy";
        return name;
    }

    public static String getRemoteClassParamName() {
        return "eventClassName";
    }
}
