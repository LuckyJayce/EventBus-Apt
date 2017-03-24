package com.shizhefei.eventbus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.shizhefei.eventbus.demo.R;
import com.shizhefei.eventbus.events.IAccountEvent;
import com.shizhefei.eventbus.events.IMessageEvent;

/**
 * Created by LuckyJayce on 2017/3/20.
 */

public class SenderFragment extends Fragment {
    private Button sendButton;
    private EditText editText;
    private int messageId = 0;
    private Button logoutButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sender, container, false);
        sendButton = (Button) view.findViewById(R.id.button);
        editText = (EditText) view.findViewById(R.id.editText);
        logoutButton = (Button) view.findViewById(R.id.logout_button);
        sendButton.setOnClickListener(onClickListener);
        logoutButton.setOnClickListener(onClickListener);
        return view;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v == sendButton) {
                String s = editText.getText().toString();

                EventBus.get(IMessageEvent.class).onReceiverMessage(messageId++, s);

                editText.getText().clear();
            } else if (v == logoutButton) {
                EventBus.get(IAccountEvent.class).logout();
            }
        }
    };
}
