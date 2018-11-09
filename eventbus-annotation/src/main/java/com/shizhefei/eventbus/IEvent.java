package com.shizhefei.eventbus;

/**
 * Created by LuckyJayce on 2017/3/20.
 * <p>
 * 不支持返回值，返回值必须void
 * 不支持定义的方法抛出异常
 * 只支持接口直接继承IEvent，不支持多级的接口继承
 * 需要添加 @Event 注解
 * <pre>
 * @Event
 *  public interface IMessageEvent extends IEvent {
 *      void onReceiverMessage(int messageId, String message);
 *  }
 *
 * EventBus.post(IMessageEvent.class).onReceiverMessage(1, "aaaaa");
 * <pre/>
 */
public interface IEvent {


    /**
     * 发布事件的时候通过这个参数 过滤接收者，Filter需要定义在IEvent的方法的第一个参数
     * <pre>
     * @Event
     *  public interface IMessageEvent extends IEvent {
     *      void onReceiverMessage(Filter filter, int messageId, String message);
     *  }
     *
     *  EventBus.post(IMessageEvent.class).onReceiverMessage(Filters.deliver(eventReceiver), 1, "aaaaa");
     * <pre/>
     */
    interface Filter {
        boolean accept(IEvent receiver);
    }
}
