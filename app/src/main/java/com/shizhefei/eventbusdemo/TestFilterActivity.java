package com.shizhefei.eventbusdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.shizhefei.eventbus.EventBus;
import com.shizhefei.eventbus.Filters;
import com.shizhefei.eventbus.demo.R;
import com.shizhefei.eventbusdemo.events.TestFilterEvent;

public class TestFilterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_filter);
        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.postMain(TestFilterEvent.class).onWrite(null, "aaaaa");
            }
        });
        findViewById(R.id.sendButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.postMain(TestFilterEvent.class).onWrite(Filters.deliver(eventReceiver1), "aaaaa");
            }
        });

        EventBus.register(eventReceiver1);
        EventBus.register(eventReceiver2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.unregister(eventReceiver1);
        EventBus.unregister(eventReceiver2);
    }

    private TestFilterEvent eventReceiver1 = new TestFilterEvent() {

        @Override
        public void onWrite(Filter filter, String text) {
            Toast.makeText(getApplicationContext(), "eventReceiver1:" + text, Toast.LENGTH_SHORT).show();
            Log.d("tttt", "eventReceiver1:" + text);
        }
    };

    private TestFilterEvent eventReceiver2 = new TestFilterEvent() {

        @Override
        public void onWrite(Filter filter, String text) {
            Toast.makeText(getApplicationContext(), "eventReceiver2:" + text, Toast.LENGTH_SHORT).show();
            Log.d("tttt", "eventReceiver2:" + text);
        }
    };
}
