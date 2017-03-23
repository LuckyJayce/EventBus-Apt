package com.shizhefei.eventbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shizhefei.eventbus.events.IAccountEvent;
import com.shizhefei.eventbus.events.IMessageEvent;

/**
 * Created by LuckyJayce on 2017/3/20.
 */

public class ReceiverFragment extends Fragment implements IMessageEvent ,IAccountEvent {
    private TextView textView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receiver, container, false);
        textView = (TextView) view.findViewById(R.id.textView);
        EventBus.register(this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.unregister(this);
    }

    private StringBuilder stringBuilder = new StringBuilder();

    @Override
    public void onReceiverMessage(int messageId, String message) {
        stringBuilder.insert(0,"messageId:"+messageId+" message:"+message+"\n");
        textView.setText(stringBuilder);
    }

    @Override
    public void logout() {
        stringBuilder.insert(0,"正在执行登出操作\n");
        textView.setText(stringBuilder);
    }

    @Override
    public void login() {
        stringBuilder.insert(0,"正在登录中..\n");
        textView.setText(stringBuilder);
    }
}
