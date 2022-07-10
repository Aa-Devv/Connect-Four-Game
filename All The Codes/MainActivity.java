package com.example.connectfour;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    boolean yellow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        yellow = true;
        Vault.winScore = 0;
        Vault.loseScore = 0;
        Vault.drawScore = 0;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void buttonP(View view) {
        Intent intent = new Intent(this, Game.class);
        intent.putExtra("yellow", yellow);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 0 && resultCode == RESULT_OK) {
            TextView tx = (TextView) findViewById(R.id.textView2);
            tx.setText("W: " + data.getExtras().get("w"));
            findViewById(R.id.textView2).setVisibility(View.VISIBLE);

            tx = (TextView) findViewById(R.id.textView3);
            tx.setText("L: " + data.getExtras().get("l"));
            findViewById(R.id.textView3).setVisibility(View.VISIBLE);
            tx = (TextView) findViewById(R.id.textView4);
            tx.setText("D: " + data.getExtras().get("d"));
            findViewById(R.id.textView4).setVisibility(View.VISIBLE);
        }
        super.onActivityResult(requestCode, resultCode, data);

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
