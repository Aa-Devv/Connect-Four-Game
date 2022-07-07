package com.example.connectfour;

import android.content.res.Resources;

import java.util.ArrayList;

public class Vault {
   static ArrayList<Float> positionsX = new ArrayList<>();
  static ArrayList<Float> positionsY = new ArrayList<>();
     ArrayList<Move> moves = new ArrayList<>();
    static void getPositions(){
        Float x,y=0f;
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        y =  height / 24 + (height / 2) / 2.0f;
        for (int i = 0; i < 6; i++, y += height / 12) {
            positionsY.add(y);
        }
        x =  0f;
        for (int j = 0; j < 7; j++, x += width / 7) {
            Float pX = (width / 14) + x;
            positionsX.add(pX);
        }
    }
}
