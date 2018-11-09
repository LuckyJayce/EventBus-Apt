package com.shizhefei.eventbusdemo;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.shizhefei.eventbus.EventBus;
import com.shizhefei.eventbus.IEvent;
import com.shizhefei.eventbus.Util;
import com.shizhefei.eventbus.annotation.Event;
import com.shizhefei.eventbus.demo.MessageCallback;
import com.shizhefei.eventbus.demo.R;
import com.shizhefei.eventbusdemo.events.IAccountEventHAHAHAHAHHALLLLL;
import com.shizhefei.eventbusdemo.events.IMessageEvent;
import com.shizhefei.eventbusdemo.events.ISendMessageEvent;

import java.util.List;

public class MainActivity extends AppCompatActivity implements IMessageEvent {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SecondActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TestFilterActivity.class);
                startActivity(intent);
            }
        });
        Log.d("pppp", "main: Process.myPid():" + Process.myPid());
        EventBus.register(this);

        Log.d("pppp", "main: processName:" + Util.getCurrentProcessName());


        EventBus.register(sendMessageEvent);

        //TODO eventbus log
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.unregister(this);
        EventBus.unregister(sendMessageEvent);
    }

    @Override
    public void onReceiverMessage(int messageId, String message) {
        Toast.makeText(this, "message:" + message, Toast.LENGTH_SHORT).show();
    }

    @Event
    public interface AAAA extends IEvent {
        void ssss();
    }

    private ISendMessageEvent sendMessageEvent = new ISendMessageEvent() {
        @Override
        public void sendMessage(final String content, final MessageCallback messageCallback) {
            Toast.makeText(getApplicationContext(), "我是MainActivity收到你的消息:" + content, Toast.LENGTH_LONG).show();
            try {
                String processName = Util.getCurrentProcessName();
                messageCallback.onSuccess(1, " 请求时间1秒 " + processName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };
}

