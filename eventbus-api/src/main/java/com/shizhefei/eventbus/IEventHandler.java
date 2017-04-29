package com.shizhefei.eventbus;

/**
 * 事件处理着
 * Created by LuckyJayce on 2016/7/26.
 */
public interface IEventHandler {
    /**
     * 获取动态代理实现的IEvent，用来发送事件
     *
     * @param eventProxyClass 事件的proxyClass
     * @param <EVENT>    IEvent的class的泛型
     * @return
     */
    <EVENT extends IEvent> EVENT post(Class<? extends EventProxy<EVENT>> eventProxyClass);

    <EVENT extends IEvent> EVENT postMain(Class<? extends EventProxy<EVENT>> eventProxyClass);

    /**
     * 注册这个对象的所有event接口
     *
     * @param subscriber
     */

    void register(IEvent subscriber);

    /**
     * 注销掉这个对象的有event接口
     *
     * @param subscriber
     */

    void unregister(IEvent subscriber);

    /**
     * 是否订阅
     *
     * @param subscriber 订阅者
     * @return 是否订阅
     */
    boolean isRegister(IEvent subscriber);

}
