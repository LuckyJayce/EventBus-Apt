package com.shizhefei.eventbus;

/**
 * 事件处理着
 * Created by LuckyJayce on 2016/7/26.
 */
public interface IEventHandler {
    /**
     * 获取事件分发的代理类实例，用来发送事件，哪个线程发布事件就在哪个线程接收事件，不过注册事件接收的类还可以添加Subscribe定义事件接口的线程
     *
     * @param <EVENT>    IEvent的class的泛型
     * @param eventInterface 事件的proxyClass
     * @return
     */
    <EVENT extends IEvent> EVENT post(Class<EVENT> eventInterface);

    /**
     * 获取事件分发的代理类实例（在主线程发布事件），用来发送事件，哪个线程发布事件就在哪个线程接收事件，不过注册事件接收的类还可以添加Subscribe定义事件接口的线程
     * @param eventInterface
     * @param <EVENT>
     * @return
     */
    <EVENT extends IEvent> EVENT postMain(Class<EVENT> eventInterface);

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
