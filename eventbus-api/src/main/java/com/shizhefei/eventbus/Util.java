package com.shizhefei.eventbus;

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
}
