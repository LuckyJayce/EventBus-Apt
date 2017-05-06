package com.shizhefei.eventbus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.shizhefei.eventbus.demo.R;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Person[] persons = (Person[]) getIntent().getSerializableExtra("persons");
        Log.d("pppp", " persons[0].getName():" + persons[0].getName());
    }
}
