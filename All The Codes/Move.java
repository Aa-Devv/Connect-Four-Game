package com.example.connectfour;

public class Move {
    Move(String colorName,float x, float y){
        this.colorName = colorName;
        this.x = x;
        this.y=y;
        done = false;
    }
    String colorName;
    float x;
    float y;
    boolean done;
}
