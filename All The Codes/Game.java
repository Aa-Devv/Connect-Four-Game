package com.example.connectfour;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class Game extends Activity {
    Boolean turn = false;
    String[][] gameMap;
    String colorOfPlayer;
    String humanColor;
    String botColor;
    int indexForY;
    Toast t0;
    Builder canvas;
    boolean newGame = false;
    int idForMoveAdders;
    boolean pickTheMostTolerable;
    ArrayList<String> colorsOfPlayers = new ArrayList();
    ArrayList<MoveAdder> moveAdders = new ArrayList<>();
    ArrayList<ArrayList<Integer>> winningMoves = new ArrayList<>();
    String colorOfWinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        colorsOfPlayers = new ArrayList();
        colorsOfPlayers.add("R");
        colorsOfPlayers.add("Y");
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
        canvas = new Builder(this, this);
        setContentView(canvas);
        (new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Vault.sealed) {

                }
                startGame();
            }
        })).start();
    }


    void startGame() {
        Thread a = new Thread(new Runnable() {
            @Override
            public void run() {
                if (moveAdders.size() != 0) while (!moveAdders.get(moveAdders.size() - 1).m.done) {
                }
            }
        });
        a.start();
        try {
            a.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

        gameMap = new String[7][6];
        moveAdders = new ArrayList<>();
        winningMoves = new ArrayList<>();
        idForMoveAdders = 0;
        emptyTheFirstRow();
        canvas.moves = new ArrayList<>();
        turn = !getIntent().getExtras().get("yellow").toString().equalsIgnoreCase("true");
        newGame = false;
        botTurn();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                canvas.invalidate();
            }
        });

        theViewIsBusy = false;
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

    boolean theViewIsBusy;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (theViewIsBusy) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            double tradeOff = Vault.width / 8;
            if ((event.getY() - tradeOff < Vault.width / 8) && event.getX() < Vault.width / 8) {
                Intent i = new Intent(this, MainActivity.class);
                i.putExtra("w", Vault.winScore);
                i.putExtra("l", Vault.loseScore);
                i.putExtra("d", Vault.drawScore);
                setResult(RESULT_OK, i);
                finish();
                return true;
            }

            if (newGame) {
                (new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startGame();
                    }
                })).start();
                theViewIsBusy = true;
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
                    Vault.drawScore++;
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
            int x = temp[0];
            if (turn && rf[0] == 1 && checkColumn(x)) {
                int y = indexForY;
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
                ma.m = new Move(humanColor, Vault.positionsX.get(x), Vault.positionsY.get(y));
                ma.start();
                turn = false;
                colorOfPlayer = humanColor;
                gameMap[x][y] = colorOfPlayer;
                if (y != 0) gameMap[x][y - 1] = "F";
                if (checkTheGame(x, y)) {
                    Vault.winScore++;
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

                if (motionEvent.getY() - tradeOff > Vault.height / 4 && motionEvent.getY() - tradeOff < (Vault.height / 2 + Vault.height / 4)) {
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
        //declare winner
        int winningCounts = 0;
        for (int i = 0; i < gameMap.length; i++) {
            if (gameMap[i][y] != null && gameMap[i][y].equalsIgnoreCase(colorOfPlayer)) {
                winningCounts++;
                ArrayList<Integer> a = new ArrayList();
                a.add(i);
                a.add(y);
                colorOfWinner = colorOfPlayer;
                winningMoves.add(a);
            } else {
                winningCounts = 0;
                colorOfWinner = "F";
                winningMoves = new ArrayList<>();
            }
            if (winningCounts == 4)
                return true;
        }
        ArrayList<Integer> a = new ArrayList();
        a.add(x);
        a.add(y);
        colorOfWinner = colorOfPlayer;
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
                colorOfWinner = "F";
                winningMoves = new ArrayList<>();
            }
            if (winningCounts == 4)
                return true;
        }
        winningMoves = new ArrayList<>();
        a = new ArrayList();
        a.add(x);
        a.add(y);
        colorOfWinner = colorOfPlayer;
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
        if (winningCounts >= 4)
            return true;
        winningMoves = new ArrayList<>();
        a = new ArrayList();
        a.add(x);
        a.add(y);
        colorOfWinner = colorOfPlayer;
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
        if (winningCounts >= 4)
            return true;
        else {
            colorOfWinner = "F";
            winningMoves = new ArrayList<>();
        }
        return false;

    }

    boolean checkTheGame2(int x, int y) {
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
        for (int i = 0; i < Vault.positionsX.size(); i++)
            if (String.valueOf(Vault.positionsX.get(i)).equalsIgnoreCase(String.valueOf(x))) {
                temp[0] = i;
                break;
            }
        for (int i = 0; i < Vault.positionsY.size(); i++)
            if (String.valueOf(Vault.positionsY.get(i)).equalsIgnoreCase(String.valueOf(y))) {
                temp[1] = i;
                break;
            }

        return temp;
    }

    float[] correctMove(double x, double y) {
        float[] rF = new float[3];
        for (int i = 0; i < Vault.positionsX.size(); i++) {
            if (x == Vault.positionsX.get(i) || (x <= (Vault.positionsX.get(i) + (Vault.width / 14)) && (((i == 0) && x >= 0) || ((i != 0) && x > Vault.positionsX.get(i - 1) + (Vault.width / 14))))) {
                for (int j = 0; j < Vault.positionsY.size(); j++)
                    if (y == Vault.positionsY.get(j) || ((y <= Vault.positionsY.get(j) + Vault.height / 24) && ((j == 0 && y >= Vault.height / 4) || (j != 0 && y > Vault.positionsY.get(j - 1) + Vault.height / 24)))) {
                        rF[0] = 1;
                        rF[1] = (float) Vault.positionsX.get(i);
                        rF[2] = (float) Vault.positionsY.get(j);
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
        if (Vault.positionsX != null) {
            if (!newGame && !turn) {
                Random r = new Random();
                int x = 0;
                int y = 0;
                boolean randomize = true;
                colorOfPlayer = humanColor;
                for (int i = 0; i < gameMap.length; i++) {
                    //if the human are about to connect three they get blocked
                    if (checkColumn(i)) {
                        gameMap[i][indexForY] = colorOfPlayer;
                        if (checkTheGame2(i, indexForY)) {
                            x = i;
                            y = indexForY;
                            gameMap[i][indexForY] = "F";
                            randomize = !canBotPlayhere(x, y);
                            break;
                        }
                        gameMap[i][indexForY] = "F";
                    }
                }
                if (pickTheMostTolerable) {
                    if (pickTheMostTolerable()[0] == 1) {
                        x = pickTheMostTolerable()[1];
                        y = pickTheMostTolerable()[2];
                    }
                }
                colorOfPlayer = humanColor;
                for (int i = 0; i < gameMap.length; i++) {
                    //if the human are about to connect four they get blocked
                    if (checkColumn(i)) {
                        gameMap[i][indexForY] = colorOfPlayer;
                        if (checkTheGame(i, indexForY)) {
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
                        x = 3;
                        y = indexForY;
                    }
                    while (Vault.positionsX != null) {
                        if ((gameMap[x][y]) != null && ((gameMap[x][y]).charAt(0) == 'F')) {
                            randomize = !canBotPlayhere(x, y);
                            if (!randomize) break;
                        }
                        x = r.nextInt(gameMap.length);
                        y = r.nextInt(gameMap[0].length);
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
                ma.m = new Move(botColor, Vault.positionsX.get(x), Vault.positionsY.get(y));
                ma.start();
                turn = true;
                colorOfPlayer = botColor;
                gameMap[x][y] = colorOfPlayer;
                if (y != 0) gameMap[x][y - 1] = "F";
                if (checkTheGame(x, y)) {
                    Vault.loseScore++;
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
