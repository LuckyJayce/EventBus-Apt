package com.shizhefei.eventbus;

import android.os.Bundle;

import java.util.Map;

/**
 * 生成实现EVENT的分发事件的代理类实例的 工厂
 * Created by luckyjayce on 2017/9/25.
 */
public interface IEventProxyFactory {

    /**
     * * 创建进程内 分发事件的代理类的实例
     * @param eventClass
     * @param postMainThread
     * @param registers
     * @param <EVENT>
     * @return
     */
    <EVENT extends IEvent> EVENT createLocalProxy(Class<EVENT> eventClass, boolean postMainThread, Map<EVENT, Register<EVENT>> registers);

    /**
     * 创建跨进程 分发事件的代理类的实例
     * @param eventClass
     * @param processName
     * @param <EVENT>
     * @return
     */
    <EVENT extends IEvent> EVENT createRemoteProxy(Class<EVENT> eventClass, String processName);

    /**
     * 跨进程调用事件
     * @param eventRemoteData
     */
    void onRemoteEvent(Bundle eventRemoteData);
}
