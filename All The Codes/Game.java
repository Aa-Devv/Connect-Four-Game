package com.example.connectfour;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class Game extends Activity {
    Boolean turn = false;
    ArrayList<Float> positionsX;
    ArrayList<Float> positionsY;
    String[][] gameMap;
    String colorOfPlayer;
    String humanColor;
    String botColor;
    int indexForY;
    Toast t0;
    can canvas;
    boolean newGame = false;
    int idForMoveAdders;
    boolean pickTheMostTolerable;
    ArrayList<String> colorsOfPlayers = new ArrayList();
    ArrayList<MoveAdder> moveAdders = new ArrayList<>();
    ArrayList<ArrayList<Integer>> winningMoves = new ArrayList<>();
    long winScore;
    long loseScore;
    long drawScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        colorsOfPlayers = new ArrayList();
        colorsOfPlayers.add("R");
        colorsOfPlayers.add("Y");
        winScore = 0;
        loseScore = 0;
        drawScore = 0;
        if (getIntent().getExtras().get("yellow").toString().equalsIgnoreCase("true")) {
            turn = false;
            humanColor = "Y";
            botColor = "R";
        } else {
            humanColor = "R";
            botColor = "Y";
            turn = true;
        }
        super.onCreate(savedInstanceState);
        Vault.getPositions();
        getPositions();
        canvas = new can(this, this);
        setContentView(canvas);
        startGame();
    }

    void getPositions() {
        Float x, y = 0f;
        positionsX = new ArrayList<>();
        positionsY = new ArrayList<>();
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        int width = Resources.getSystem().getDisplayMetrics().widthPixels;
        y = height / 24 + (height / 2) / 2.0f;
        for (int i = 0; i < 6; i++, y += height / 12) {
            positionsY.add(y);
        }
        x = 0f;
        for (int j = 0; j < 7; j++, x += width / 7) {
            Float pX = (width / 14) + x;
            positionsX.add(pX);
        }
    }

    void startGame() {

        gameMap = new String[7][6];
        for (int i = 0; i < moveAdders.size(); i++) {
            if (moveAdders.get(i).isAlive()) {
                moveAdders.get(i).interrupt();
            }
        }
        moveAdders = new ArrayList<>();
        winningMoves = new ArrayList<>();
        idForMoveAdders = 0;
        emptyTheFirstRow();
        canvas.moves = new ArrayList<>();
        turn = !getIntent().getExtras().get("yellow").toString().equalsIgnoreCase("true");
        botTurn();
        canvas.invalidate();
    }

    void sendGame() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                canvas.invalidate();
            }
        });

    }

    boolean checkColumn(int x) {

        for (int i = 0; i < gameMap[0].length; i++) {
            if (gameMap[x][i] != null && gameMap[x][i].equalsIgnoreCase("F")) {
                indexForY = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            double tradeOff = canvas.getPivotY() / 7.6176;
            if ((event.getY() - tradeOff < 155) && event.getX() < 155) {
                finish();
                return true;
            }

            if (newGame) {
                newGame = false;
                startGame();
                return true;
            }
            Thread humanPlayer = new Thread(new Runnable() {
                @Override
                public void run() {
                    humanTurn(event, tradeOff);
                }
            });
            try {
                if (turn) {
                    humanPlayer.start();
                    humanPlayer.join();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    boolean checkIfDraw() {
        boolean draw = true;
        for (int i = 0; i < gameMap.length; i++) {
            if (gameMap[i][0] == null || (gameMap[i][0] != null && gameMap[i][0].equalsIgnoreCase("F"))) {
                draw = false;
            }
        }
        if (draw) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (t0 != null) t0.cancel();
                    t0 = t0.makeText(Game.this, "It's a draw!", Toast.LENGTH_SHORT);
                    t0.show();
                    drawScore++;
                }
            });
            newGame = true;

        }
        return draw;
    }

    void humanTurn(MotionEvent motionEvent, double tradeOff) {
        if (!turn) return;
        if (!newGame && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            float[] rf = correctMove(motionEvent.getX(), motionEvent.getY() - tradeOff);
            int[] temp = translate(rf[1], rf[2]);
            if (turn && rf[0] == 1 && checkColumn(temp[0])) {
                MoveAdder ma = new MoveAdder() {
                    @Override
                    public void run() {
                        try {
                            if (this.id != 0)
                                while (moveAdders.get(this.id - 1).isAlive() || !moveAdders.get(this.id - 1).m.done) {
                                    if (Thread.interrupted()) {

                                        return;
                                    }
                                }

                            if (Thread.interrupted()) {

                                return;
                            }
                            canvas.moves.add(this.m);
                            sendGame();
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                            return;
                        }

                    }
                };
                moveAdders.add(ma);
                ma.id = idForMoveAdders++;
                ma.m = new Move(humanColor, rf[1], positionsY.get(indexForY));
                ma.start();
                turn = false;
                colorOfPlayer = humanColor;
                gameMap[temp[0]][indexForY] = colorOfPlayer;
                if ((indexForY - 1) >= 0) gameMap[temp[0]][indexForY - 1] = "F";
                if (checkTheGame(temp[0], indexForY)) {
                    winScore++;
                    this.runOnUiThread(new Runnable() {
                        public void run() {
                            if (t0 != null) t0.cancel();
                            t0 = t0.makeText(Game.this, "You won!", Toast.LENGTH_SHORT);
                            t0.show();
                        }
                    });

                    newGame = true;
                    return;
                }
            } else {

                if (motionEvent.getY() - tradeOff > canvas.getHeight() / 4 && motionEvent.getY() - tradeOff < (canvas.getHeight() / 2 + canvas.getHeight() / 4)) {
                    this.runOnUiThread(new Runnable() {
                        public void run() {
                            if (t0 != null) t0.cancel();
                            t0 = t0.makeText(Game.this, "This column is full.", Toast.LENGTH_SHORT);
                            t0.show();
                        }
                    });

                }


            }

        }
        if (checkIfDraw()) {
            return;
        }

        Thread botPlayer = new Thread(new Runnable() {
            @Override
            public void run() {
                botTurn();
            }
        });
        botPlayer.start();

        try {
            botPlayer.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        checkIfDraw();
        return;


    }


    void emptyTheFirstRow() {
        for (int i = 0; i < gameMap.length; i++)
            gameMap[i][5] = "F";
    }

    boolean checkTheGame(int x, int y) {
        winningMoves = new ArrayList<>();
        //set the position above the piece that was just played to F
        //declare winner
        int winningCounts = 0;
        for (int i = 0; i < gameMap.length; i++) {
            if (gameMap[i][y] != null && gameMap[i][y].equalsIgnoreCase(colorOfPlayer)) {
                winningCounts++;
                ArrayList<Integer> a = new ArrayList();
                a.add(i);
                a.add(y);
                winningMoves.add(a);
            } else {
                winningCounts = 0;
                winningMoves = new ArrayList<>();
            }
            if (winningCounts == 4)
                return true;
        }
        ArrayList<Integer> a = new ArrayList();
        a.add(x);
        a.add(y);
        winningMoves.add(a);
        winningCounts = 1;
        for (int i = 1; i < 4; i++) {
            if ((y + i) < gameMap[0].length && (gameMap[x][y + i] != null && gameMap[x][y + i].equalsIgnoreCase(colorOfPlayer))) {
                winningCounts++;
                a = new ArrayList();
                a.add(x);
                a.add(y + i);
                winningMoves.add(a);
            } else {
                winningCounts = 0;
                winningMoves = new ArrayList<>();
            }
            if (winningCounts == 4)
                return true;
        }
        winningMoves = new ArrayList<>();
        a = new ArrayList();
        a.add(x);
        a.add(y);
        winningMoves.add(a);
        winningCounts = 1;
        for (int i = 1, j = 1; x + i < gameMap.length && y + j < gameMap[0].length; i++, j++) {
            if (gameMap[x + i][y + j] != null && gameMap[x + i][y + j].equalsIgnoreCase(colorOfPlayer)) {
                winningCounts++;
                a = new ArrayList();
                a.add(x + i);
                a.add(y + j);
                winningMoves.add(a);
            } else break;

        }

        for (int i = 1, j = 1; x - i >= 0 && y - j >= 0; i++, j++) {
            if (gameMap[x - i][y - j] != null && gameMap[x - i][y - j].equalsIgnoreCase(colorOfPlayer)) {
                winningCounts++;
                a = new ArrayList();
                a.add(x - i);
                a.add(y - j);
                winningMoves.add(a);
            } else break;

        }
        if (winningCounts == 4)
            return true;
        winningMoves = new ArrayList<>();
        a = new ArrayList();
        a.add(x);
        a.add(y);
        winningMoves.add(a);
        winningCounts = 1;
        for (int i = 1, j = 1; x + i < gameMap.length && y - j >= 0; i++, j++) {
            if (gameMap[x + i][y - j] != null && gameMap[x + i][y - j].equalsIgnoreCase(colorOfPlayer)) {
                winningCounts++;
                a = new ArrayList();
                a.add(x + i);
                a.add(y - j);
                winningMoves.add(a);
            } else break;

        }
        for (int i = 1, j = 1; (x - i) >= 0 && y + j < gameMap[0].length; i++, j++) {
            if (gameMap[x - i][y + j] != null && gameMap[x - i][y + j].equalsIgnoreCase(colorOfPlayer)) {
                winningCounts++;
                a = new ArrayList();
                a.add(x - i);
                a.add(y + j);
                winningMoves.add(a);

            } else break;

        }
        if (winningCounts == 4)
            return true;
        else winningMoves = new ArrayList<>();
        return false;

    }

    boolean checkTheGame2(int x, int y) {
        //set the position above the piece that was just played to F
        //declare winner
        int winningCounts = 0;
        for (int i = 0; i < gameMap.length; i++) {
            if (gameMap[i][y] != null && gameMap[i][y].equalsIgnoreCase(colorOfPlayer))
                winningCounts++;
            else winningCounts = 0;
            if (winningCounts == 3)
                return true;

        }

        winningCounts = 1;
        for (int i = 1; i < 4; i++) {
            if ((y + i) < gameMap[0].length && (gameMap[x][y + i] != null && gameMap[x][y + i].equalsIgnoreCase(colorOfPlayer))) {
                winningCounts++;
            } else winningCounts = 0;
            if (winningCounts == 3)
                return true;
        }
        winningCounts = 1;
        for (int i = 1, j = 1; x + i < gameMap.length && y + j < gameMap[0].length; i++, j++) {
            if (gameMap[x + i][y + j] != null && gameMap[x + i][y + j].equalsIgnoreCase(colorOfPlayer))
                winningCounts++;
            else break;

        }

        for (int i = 1, j = 1; x - i >= 0 && y - j >= 0; i++, j++) {
            if (gameMap[x - i][y - j] != null && gameMap[x - i][y - j].equalsIgnoreCase(colorOfPlayer))
                winningCounts++;
            else break;

        }
        if (winningCounts == 3)
            return true;

        winningCounts = 1;
        for (int i = 1, j = 1; x + i < gameMap.length && y - j >= 0; i++, j++) {
            if (gameMap[x + i][y - j] != null && gameMap[x + i][y - j].equalsIgnoreCase(colorOfPlayer))
                winningCounts++;
            else break;

        }
        for (int i = 1, j = 1; (x - i) >= 0 && y + j < gameMap[0].length; i++, j++) {
            if (gameMap[x - i][y + j] != null && gameMap[x - i][y + j].equalsIgnoreCase(colorOfPlayer))
                winningCounts++;
            else break;

        }
        if (winningCounts == 3)
            return true;
        return false;

    }

    int[] translate(double x, double y) {
        int[] temp = new int[2];
        for (int i = 0; i < positionsX.size(); i++)
            if (String.valueOf(positionsX.get(i)).equalsIgnoreCase(String.valueOf(x))) {
                temp[0] = i;
                break;
            }
        for (int i = 0; i < positionsY.size(); i++)
            if (String.valueOf(positionsY.get(i)).equalsIgnoreCase(String.valueOf(y))) {
                temp[1] = i;
                break;
            }

        return temp;
    }

    float[] correctMove(double x, double y) {
        float[] rF = new float[3];
        for (int i = 0; i < positionsX.size(); i++) {
            if (x == positionsX.get(i) || (x <= (positionsX.get(i) + (canvas.getWidth() / 14)) && (((i == 0) && x >= 0) || ((i != 0) && x > positionsX.get(i - 1) + (canvas.getWidth() / 14))))) {
                for (int j = 0; j < positionsY.size(); j++)
                    if (y == positionsY.get(j) || ((y <= positionsY.get(j) + canvas.getHeight() / 24) && ((j == 0 && y >= canvas.getHeight() / 4) || (j != 0 && y > positionsY.get(j - 1) + canvas.getHeight() / 24)))) {
                        rF[0] = 1;
                        rF[1] = (float) positionsX.get(i);
                        rF[2] = (float) positionsY.get(j);
                        return rF;
                    }
            }
        }
        return rF;
    }


    boolean canBotPlayhere(int x, int y) {
        if (y == 0) return true;
        for (int i = 0; i < colorsOfPlayers.size(); i++) {
            colorOfPlayer = colorsOfPlayers.get(i);
            gameMap[x][y] = botColor;
            gameMap[x][y - 1] = colorsOfPlayers.get(i);

            if (checkTheGame(x, y - 1)) {
                gameMap[x][y] = "F";
                gameMap[x][y - 1] = null;
                return !thereIsAPlace();
            }
        }
        gameMap[x][y] = "F";
        gameMap[x][y - 1] = null;
        return true;
    }

    boolean thereIsAPlace() {
        //true = if there exist a place where neither red or yellow wins
        colorOfPlayer = botColor;
        boolean theyAllDontWin;
        for (int i = 0; i < gameMap.length; i++) {
            if (checkColumn(i)) {
                if (indexForY != 0) {
                    theyAllDontWin = true;
                    for (int j = 0; j < colorsOfPlayers.size(); j++) {
                        colorOfPlayer = colorsOfPlayers.get(j);
                        gameMap[i][indexForY] = botColor;
                        gameMap[i][indexForY - 1] = colorsOfPlayers.get(j);
                        if (checkTheGame(i, indexForY - 1)) {
                            theyAllDontWin = false;
                        }
                    }
                    gameMap[i][indexForY] = "F";
                    gameMap[i][indexForY - 1] = null;
                    if (theyAllDontWin) {
                        return true;
                    }
                } else return true;
            }
        }
        pickTheMostTolerable = true;
        return false;
    }

    private int[] pickTheMostTolerable() {
        pickTheMostTolerable = false;
        colorOfPlayer = humanColor;
        int[] temp = new int[3];
        for (int i = 0; i < gameMap.length; i++) {
            if (checkColumn(i)) {
                gameMap[i][indexForY] = botColor;
                gameMap[i][indexForY - 1] = humanColor;
                if (!checkTheGame(i, indexForY - 1)) {
                    gameMap[i][indexForY] = "F";
                    gameMap[i][indexForY - 1] = null;
                    temp[0] = 1;
                    temp[1] = i;
                    temp[2] = indexForY;
                    return temp;
                }

                gameMap[i][indexForY] = "F";
                gameMap[i][indexForY - 1] = null;


            }
        }
        return temp;

    }

    void botTurn() {
        if (turn) return;
        if (positionsX != null) {
            if (!newGame && !turn) {
                Random r = new Random();
                int x = 0;
                int y = 0;
                int[] temp = new int[2];
                boolean randomize = true;
                colorOfPlayer = humanColor;
                for (int i = 0; i < gameMap.length; i++) {
                    //if the human are about to connect three they get blocked
                    if (checkColumn(i)) {
                        gameMap[i][indexForY] = colorOfPlayer;
                        if (checkTheGame2(i, indexForY)) {
                            temp[0] = i;
                            temp[1] = indexForY;
                            x = i;
                            y = indexForY;
                            gameMap[i][indexForY] = "F";
                            randomize = !canBotPlayhere(temp[0], temp[1]);
                            break;
                        }
                        gameMap[i][indexForY] = "F";
                    }
                }
                if (pickTheMostTolerable) {
                    if (pickTheMostTolerable()[0] == 1) {
                        temp[0] = pickTheMostTolerable()[1];
                        temp[1] = pickTheMostTolerable()[2];
                        x = temp[0];
                        y = temp[1];
                    }
                }
                colorOfPlayer = humanColor;
                for (int i = 0; i < gameMap.length; i++) {
                    //if the human are about to connect four they get blocked
                    if (checkColumn(i)) {
                        gameMap[i][indexForY] = colorOfPlayer;
                        if (checkTheGame(i, indexForY)) {
                            temp[0] = i;
                            temp[1] = indexForY;
                            x = i;
                            y = indexForY;
                            gameMap[i][indexForY] = "F";
                            randomize = false;
                            break;
                        }
                        gameMap[i][indexForY] = "F";
                    }
                }
                colorOfPlayer = botColor;
                for (int i = 0; i < gameMap.length; i++) {
                    //if the bot is about to win they are ensured to win
                    if (checkColumn(i)) {
                        gameMap[i][indexForY] = colorOfPlayer;
                        if (checkTheGame(i, indexForY)) {
                            temp[0] = i;
                            temp[1] = indexForY;
                            x = i;
                            y = indexForY;
                            randomize = false;
                            break;
                        }
                        gameMap[i][indexForY] = "F";
                    }
                }


                if (randomize) {
                    if (checkColumn(3) && gameMap[3][indexForY] != null && gameMap[3][indexForY].equalsIgnoreCase("F")) {
                        //if the middle column is avaliable it get played in
                        temp[0] = 3;
                        temp[1] = indexForY;
                        x = 3;
                        y = indexForY;
                    }
                    while (positionsX != null) {
                        if ((gameMap[temp[0]][temp[1]]) != null && ((gameMap[temp[0]][temp[1]]).charAt(0) == 'F')) {
                            randomize = !canBotPlayhere(temp[0], temp[1]);
                            if (!randomize) break;
                        }
                        x = r.nextInt(positionsX.size());
                        y = r.nextInt(positionsY.size());
                        temp = translate(positionsX.get(x), positionsY.get(y));
                    }
                }

                MoveAdder ma = new MoveAdder() {
                    @Override
                    public void run() {
                        try {
                            if (this.id != 0)
                                while (moveAdders.get(this.id - 1).isAlive() || !moveAdders.get(this.id - 1).m.done) {
                                    if (Thread.interrupted()) {
                                        return;
                                    }
                                }

                            if (Thread.interrupted()) {
                                return;
                            }
                            canvas.moves.add(this.m);
                            sendGame();
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                            return;
                        }

                    }
                };
                moveAdders.add(ma);
                ma.id = idForMoveAdders++;
                ma.m = new Move(botColor, positionsX.get(x), positionsY.get(y));
                ma.start();
                turn = true;
                colorOfPlayer = botColor;
                gameMap[x][y] = colorOfPlayer;
                if ((y - 1) >= 0) gameMap[x][y - 1] = "F";
                if (checkTheGame(temp[0], temp[1])) {
                    loseScore++;
                    this.runOnUiThread(new Runnable() {
                        public void run() {
                            if (t0 != null) t0.cancel();
                            t0 = t0.makeText(Game.this, "Bot won.", Toast.LENGTH_SHORT);
                            t0.show();
                        }
                    });
                    newGame = true;

                } else {
                    turn = true;
                }

            }
        }
    }


}
