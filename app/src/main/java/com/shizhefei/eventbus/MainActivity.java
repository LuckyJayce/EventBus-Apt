package com.shizhefei.eventbus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.shizhefei.eventbus.demo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Person[] persons = new Person[1];
                persons[0] = new Person(11, "erfetet");
                Intent intent = new Intent(getApplicationContext(),SecondActivity.class);
                intent.putExtra("persons", persons);
                startActivity(intent);
            }
        });
    }
}
