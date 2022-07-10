package com.example.connectfour;


import java.util.ArrayList;

public class Vault {
    static ArrayList<Float> positionsX = new ArrayList<>();
    static ArrayList<Float> positionsY = new ArrayList<>();
    static int height;
    static int width;
    static long winScore;
    static long loseScore;
    static long drawScore;
    static boolean sealed;

    static void setPositions(int height, int width) {
        Float x, y = 0f;
        Vault.height = height;
        Vault.width = width;
        y = height / 24 + (height / 2) / 2.0f;
        for (int i = 0; i < 6; i++, y += height / 12) {
            positionsY.add(y);
        }
        x = 0f;
        for (int j = 0; j < 7; j++, x += width / 7) {
            Float pX = (width / 14) + x;
            positionsX.add(pX);
        }
        sealed = true;
    }
}
