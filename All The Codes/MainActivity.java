package com.example.connectfour;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    boolean yellow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        yellow = true;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void buttonP(View view) {
        Intent intent = new Intent(this, Game.class);
        intent.putExtra("yellow",yellow);
        startActivity(intent);
    }

    public void buttonC(View view) {
        if (view.isPressed()) {
            if (yellow) {
                view.setBackgroundColor(Color.RED);
                yellow = false;
            } else {
                view.setBackgroundColor(Color.YELLOW);
                yellow = true;
            }
            view.setPressed(false);
        }
    }

    @Override
    public void onBackPressed() {
        //your code when back button pressed
    }

}
