package com.shizhefei.eventbus;

/**
 * Created by LuckyJayce on 2017/3/20.
 *
 * 不支持返回值，返回值必须void
 * 不支持定义的方法抛出异常
 * 只支持接口直接继承IEvent，不支持多级的接口继承
 * 需要添加 @Event 注解
 *<pre>
 * @Event
 *  public interface IMessageEvent extends IEvent {
 *      void onReceiverMessage(int messageId, String message);
 *  }
 *<pre/>
 * */
public interface IEvent {
}
