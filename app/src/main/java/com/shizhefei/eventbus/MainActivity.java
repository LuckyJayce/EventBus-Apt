package com.shizhefei.eventbus;

import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.shizhefei.eventbus.demo.R;
import com.shizhefei.eventbus.events.IMessageEvent;

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
        Log.d("pppp", "main: Process.myPid():" + Process.myPid());
        EventBus.register(this);

        int pid = android.os.Process.myPid();
        String processName = null;
        ActivityManager activityManager = (ActivityManager) EventBus.staticContext.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : list) {
            if (runningAppProcessInfo.pid == pid) {
                processName = runningAppProcessInfo.processName;
            }
        }
        Log.d("pppp", "main: processName:" + processName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.unregister(this);
    }

    @Override
    public void onReceiverMessage(int messageId, String message) {
        Toast.makeText(this, "message:" + message, Toast.LENGTH_SHORT).show();
    }
}
