package com.shizhefei.eventbusdemo;

import android.app.ActivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.shizhefei.eventbus.EventBus;
import com.shizhefei.eventbus.Util;
import com.shizhefei.eventbus.demo.MessageCallback;
import com.shizhefei.eventbus.demo.R;
import com.shizhefei.eventbusdemo.events.IMessageEvent;
import com.shizhefei.eventbusdemo.events.ISendMessageEvent;

import java.util.List;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.postMain(IMessageEvent.class).onReceiverMessage(1, "测试本地事件");
            }
        });
        findViewById(R.id.sendButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发布事件到主进程
                String processName = getPackageName();
                EventBus.postRemote(IMessageEvent.class, processName).onReceiverMessage(1, "测试远程事件");
            }
        });
        findViewById(R.id.sendButton3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String processName = getPackageName();


                EventBus.postRemote(ISendMessageEvent.class, processName).sendMessage("hi：你好", new MessageCallback.Stub() {
                    @Override
                    public void onSuccess(final int code, final String content) throws RemoteException {
                        Log.d("pppp", "loop:" + Looper.myLooper() + " main:" + Looper.getMainLooper() + " thread:" + Thread.currentThread());
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "我是SecondActivity 收到你的返回数据 code:" + code + " content:" + content, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });


            }
        });

        Log.d("pppp", "second: Process.myPid():" + Process.myPid());
        Log.d("pppp", "second: processName:" + Util.getCurrentProcessName());
    }
}
