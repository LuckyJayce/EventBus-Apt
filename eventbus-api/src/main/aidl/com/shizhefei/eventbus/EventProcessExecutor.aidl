// EventProcessExecutor.aidl
package com.shizhefei.eventbus;

// Declare any non-default types here with import statements
/**
* EventProcessExecutor处理对应进程的事件发布
*/
interface EventProcessExecutor {
      oneway void postEvent(in Bundle remoteEventData);
}
