// EventRemote.aidl
package com.shizhefei.eventbus;
import android.os.Bundle;

interface EventRemote {
   /*  oneway 表示用户请求相应功能时不需要等待响应可直接调用返回，非阻塞效果，该关键字可以用来声明接口或者声明方法，
     如果接口声明中用到了oneway关键字，则该接口声明的所有方法都采用oneway方式*/
    oneway void postEvent(in Bundle remoteEventData);
}
