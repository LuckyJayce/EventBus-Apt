// EventServiceExecuter.aidl
package com.shizhefei.eventbus;
import com.shizhefei.eventbus.EventProcessExecutor;
// Declare any non-default types here with import statements
/**
* EventServiceExecutor用于Service提供注册EventProcessExecutor，分发事件给对应的EventProcessExecutor
* */
interface EventServiceExecutor {
    oneway void register(int pid, String processName, EventProcessExecutor processExecutor);

    oneway void unregister(EventProcessExecutor processExecutor);

   /*  oneway 表示用户请求相应功能时不需要等待响应可直接调用返回，非阻塞效果，该关键字可以用来声明接口或者声明方法，
     如果接口声明中用到了oneway关键字，则该接口声明的所有方法都采用oneway方式*/
    oneway void postEvent(String processName, in Bundle remoteEventData);
}
