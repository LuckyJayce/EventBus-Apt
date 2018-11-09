package com.shizhefei.eventbusdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shizhefei.eventbus.EventBus;
import com.shizhefei.eventbus.Subscribe;
import com.shizhefei.eventbus.demo.R;
import com.shizhefei.eventbusdemo.events.IAccountEventHAHAHAHAHHALLLLL;
import com.shizhefei.eventbusdemo.events.IMessageEvent;

/**
 * Created by LuckyJayce on 2017/3/20.
 */
@Subscribe(receiveThreadMode = Subscribe.MAIN)
public class ReceiverFragment extends Fragment implements IMessageEvent, IAccountEventHAHAHAHAHHALLLLL {
    private TextView textView;
    private StringBuilder stringBuilder = new StringBuilder();

    private IMessageEvent iMessageEvent_main = new MyIMessageEvent();
    private IMessageEvent iMessageEvent_posting = new MyIMessageEvent2();
    private IMessageEvent iMessageEvent_background = new MyIMessageEvent3();
    private IMessageEvent iMessageEvent_async = new MyIMessageEvent4();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receiver, container, false);
        textView = (TextView) view.findViewById(R.id.textView);
        EventBus.register(iMessageEvent_main);
        EventBus.register(iMessageEvent_posting);
        EventBus.register(iMessageEvent_background);
        EventBus.register(iMessageEvent_async);
        EventBus.register(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.unregister(iMessageEvent_main);
        EventBus.unregister(iMessageEvent_posting);
        EventBus.unregister(iMessageEvent_background);
        EventBus.unregister(iMessageEvent_async);
        EventBus.unregister(this);
    }

    @Override
    public void onReceiverMessage(int messageId, String message) {
        stringBuilder.insert(0,"messageId:"+messageId+" message:"+message+"\n");
        textView.setText(stringBuilder);
    }

    @Override
    public void logout() {
        stringBuilder.insert(0, "注销登陆\n");
        textView.setText(stringBuilder);
    }

    @Override
    public void login() {
        stringBuilder.insert(0, "正在登陆\n");
        textView.setText(stringBuilder);
    }

    @Subscribe(receiveThreadMode = Subscribe.MAIN)
    private class MyIMessageEvent implements IMessageEvent {
        @Override
        public void onReceiverMessage(int messageId, String message) {
            Log.d("aaaa","Subscribe.MAIN : "+Thread.currentThread().toString());
        }
    };

    @Subscribe(receiveThreadMode = Subscribe.POSTING)
    private class MyIMessageEvent2 implements IMessageEvent {
        @Override
        public void onReceiverMessage(int messageId, String message) {
            Log.d("aaaa","Subscribe.POSTING : "+Thread.currentThread().toString());
        }
    };

    @Subscribe(receiveThreadMode = Subscribe.BACKGROUND)
    private class MyIMessageEvent3 implements IMessageEvent {
        @Override
        public void onReceiverMessage(int messageId, String message) {
            Log.d("aaaa","Subscribe.BACKGROUND : "+Thread.currentThread().toString());
        }
    };

    @Subscribe(receiveThreadMode = Subscribe.ASYNC)
    private class MyIMessageEvent4 implements IMessageEvent {
        @Override
        public void onReceiverMessage(int messageId, String message) {
            Log.d("aaaa","Subscribe.ASYNC : "+Thread.currentThread().toString());
        }
    };
}
