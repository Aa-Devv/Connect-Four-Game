package com.example.connectfour;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.ArrayList;

public class Builder extends View {
    ArrayList<Move> moves = new ArrayList<>();
    Game game;
    Paint t = new Paint();

    public Builder(Context context,Game game) {
        super(context);
        this.game = game;

    }

String translateNumber(long n){
        if(n==0) return " ";
        long numberOfFives = n/5;
        String str = "";
        for(long i=0;i<numberOfFives;i++){
            str = str+"卌";
        }
    for(long i=0;i<n%5;i++){
        str = str+"|";
    }
        return str;
}
    void drawTheBackground(Canvas canvas) {
        float x = 0;
        float y = 0;
        t = new Paint();
        int height = canvas.getHeight();
        int width = canvas.getWidth();
        canvas.drawColor(Color.rgb(39, 69, 139));
        Rect r = new Rect();
        r.set(0, 0, width, height / 4);
        Paint bb = new Paint();
        bb.setColor(Color.BLACK);
        canvas.drawRect(r, bb);
        r.set(0, height / 2 + height / 4, width, height);
        canvas.drawRect(r, bb);
        t.setColor(Color.BLACK);
        t.setStrokeWidth(5);
        canvas.drawLine(0, (height / 2) / 2, width, (height / 2) / 2, t);
        x = width - 2;
        y = (height / 2) / 2;
        canvas.drawLine(x, y, x, y * 2 + y, t);
        x = width;
        y = y * 2 + y;
        canvas.drawLine(x, y, 0, y, t);
        x = 0 + 2;
        canvas.drawLine(x, y, x, (height / 2) / 2, t);
        x = width / 7;
        y = (height / 2) / 2;
        for (int i = 0; i < 6; i++, x += width / 7)
            canvas.drawLine(x, y, x, y * 2 + y, t);
        x = width;
        y = height / 12 + (height / 2) / 2;
        for (int i = 0; i < 5; i++, y += height / 12)
            canvas.drawLine(x, y, 0, y, t);
        y = height / 24 + (height / 2) / 2;
        t.setColor(Color.rgb(22, 40, 80));
        for (int i = 0; i < 6; i++, y += height / 12) {
            x = 0;
            for (int j = 0; j < 7; j++, x += width / 7) {
                Float pX = (width / 14) + x;
                canvas.drawCircle((float) pX, y, (float) ((float) (width / 14) * .8), t);
            }
        }
        Drawable dr = getResources().getDrawable(R.drawable._40);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
// Scale it to 50 x 50
        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 150, 150, true));
// Set your new, scaled drawable "d"
        Bitmap arrowback = ((BitmapDrawable) d).getBitmap();
        r = new Rect();
        Paint pforg = new Paint();
        pforg.setColor(Color.rgb(255, 255, 255));
        canvas.drawBitmap(arrowback, 5, 5, pforg);
        pforg.setTextSize(50);
        canvas.drawText("Wins: "+translateNumber(game.winScore),getWidth()/4,155,pforg);
        canvas.drawText("Losses: "+translateNumber(game.loseScore),getWidth()/4,255,pforg);
        canvas.drawText("Draws: "+translateNumber(game.drawScore),getWidth()/4,355,pforg);
    }

    void drawMoves(Canvas canvas) {
        int width = canvas.getWidth();
        t = new Paint();
        for (int i = 0; i < moves.size() - 1; i++) {
            if (moves.get(i).colorName.equalsIgnoreCase("R")) t.setColor(Color.rgb(195, 29, 43));
            else t.setColor(Color.rgb(255, 254, 13));
            t.setStrokeWidth(5);
            canvas.drawCircle(moves.get(i).x, moves.get(i).y, (float) ((float) (width / 14) * .8), t);
            if(game.winningMoves.size()!=0){
               for(int j=0;j<game.winningMoves.size();j++){
                   if(Vault.positionsX.get(game.winningMoves.get(j).get(0))==moves.get(i).x&&Vault.positionsY.get(game.winningMoves.get(j).get(1))==moves.get(i).y){
                       Paint w = new Paint();
                       w.setStyle(Paint.Style.STROKE);
                       w.setColor(Color.rgb(255,215,0));
                       canvas.drawCircle(moves.get(i).x, moves.get(i).y, (float) ((float) (width / 14) * 1), w);
                   }
               }

            }
        }

    }

    float changingY = Vault.positionsY.get(0);
    @Override
    protected void onDraw(Canvas canvas) {
        int width = canvas.getWidth();
        super.onDraw(canvas);
        drawTheBackground(canvas);
        drawMoves(canvas);
        t = new Paint();
        if (moves.size() != 0) {
            if( moves.get(moves.size()-1).done) return;
            if (moves.get(moves.size() - 1).colorName.equalsIgnoreCase("R"))
                t.setColor(Color.rgb(195, 29, 43));
            else t.setColor(Color.rgb(255, 254, 13));
            t.setStrokeWidth(5);
            canvas.drawCircle(moves.get(moves.size() - 1).x, changingY, (float) ((float) (width / 14) * .8), t);
            if(changingY>=moves.get(moves.size() - 1).y){
                moves.get(moves.size()-1).done = true;
                if(game.winningMoves.size()!=0){
                    for(int j=0;j<game.winningMoves.size();j++){
                        if(Vault.positionsX.get(game.winningMoves.get(j).get(0))==moves.get(moves.size()-1).x&&Vault.positionsY.get(game.winningMoves.get(j).get(1))==moves.get(moves.size()-1).y){
                            Paint w = new Paint();
                            w.setStyle(Paint.Style.STROKE);
                            w.setColor(Color.rgb(255,215,0));
                            canvas.drawCircle(moves.get(moves.size()-1).x, moves.get(moves.size()-1).y, (float) ((float) (width / 14) * 1), w);
                        }
                    }

                }
                changingY = Vault.positionsY.get(0);
            }
            else {
                changingY+=((moves.get(moves.size() - 1).y-Vault.positionsY.get(0))/8);
                invalidate();
                try{
                Thread.sleep(50);
            }catch (Exception e){
                }
            }

        }
    }

}