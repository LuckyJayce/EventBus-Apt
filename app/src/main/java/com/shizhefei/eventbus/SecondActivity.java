package com.shizhefei.eventbus;

import android.app.ActivityManager;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.shizhefei.eventbus.demo.R;
import com.shizhefei.eventbus.events.IMessageEventProxy;

import java.util.List;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.postMain(IMessageEventProxy.class).onReceiverMessage(1, "测试本地事件");
            }
        });
        findViewById(R.id.sendButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发布事件到主进程
                String processName = getPackageName();
                EventBus.postRemote(IMessageEventProxy.class, processName).onReceiverMessage(1, "测试远程事件");
            }
        });
        Log.d("pppp", "second: Process.myPid():" + Process.myPid());
        String processName = null;
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : list) {
            if (runningAppProcessInfo.pid ==  Process.myPid()) {
                processName = runningAppProcessInfo.processName;
            }
        }
        Log.d("pppp", "second: processName:" + processName);
    }
}
