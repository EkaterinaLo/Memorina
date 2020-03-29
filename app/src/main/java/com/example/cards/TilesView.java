package com.example.cards;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;

class Card {
    Paint p = new Paint();

    public Card(float x, float y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    int color, backColor = Color.GRAY;
    boolean isOpen = false; // цвет карты
    float x, y;
    float cardWidth, cardHeight;

    public void draw(Canvas c) {
        if (isOpen) {
            p.setColor(color);
        }
        else {
            p.setColor(backColor);
        }

        cardWidth = c.getWidth()/4;
        cardHeight = c.getHeight()/4;
        c.drawRect(x*cardWidth,y*cardHeight, x*cardWidth + cardWidth, y*cardHeight + cardHeight, p); //отрисовываем карту
    }

    public boolean flip (float touch_x, float touch_y) {
        if (touch_x >= x*cardWidth && touch_x <= x*cardWidth + cardWidth && touch_y >= y*cardHeight && touch_y <= y*cardHeight + cardHeight) { //проверяем, коснулись ли карты
            isOpen = ! isOpen;
            return true;
        } else {
            return false;
        }
    }
}

public class TilesView extends View {
    View myView;
    int n = 8; // количество цветов
    ArrayList<Card> cards = new ArrayList<>(); // список карточек
    int amountOpenedCard = 0; //количество открытых карт
    Card openedCard1, openedCard2;


    final int PAUSE_LENGTH = 1; // длительность паузы в секундах
    boolean isOnPauseNow = false;


    public TilesView(Context context) {
        super(context);
    }

    public TilesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setColors();
    }

    public void setColors(){
        int [] cardColors = new int[n]; // цвета
        int [] colorAmount = new int[n]; // количество цветов
        int[][] tiles = new int[4][(n*2)/4]; //поле цветов


        for (int i = 0; i < n; i++) {
            Random rnd = new Random();
            cardColors[i] = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)); //получаем n случайных цветов
        }


        for (int i = 0; i < n; i++) {
            colorAmount[i] = 2; //заполняем массив количества цветов - цвета по парам, т. е. везде 2
        }

        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[0].length; j++) {
                int k = (int) (Math.random()*n);
                while (colorAmount[k] == 0){
                    k = (int) (Math.random()*n);
                }
                tiles[i][j] = cardColors[k]; //расставляем цвета на поле
                cards.add(new Card(j, i, tiles[i][j]));
                colorAmount[k] --;
            }
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Card c: cards) {
            c.draw(canvas); //отрисовываем карты
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (Card c : cards) {
                if (amountOpenedCard == 0) {
                    if (c.flip(x, y) == true) { // переворачиваем карту, которой коснулись
                        openedCard1 = c;
                        amountOpenedCard++;
                        invalidate();
                        return true;
                    }
                }
                if (c.equals(openedCard1) == false) { //если эта карта ещё не открыта
                    if (amountOpenedCard == 1) {
                        if (c.flip(x, y) == true) {
                            openedCard2 = c;
                            amountOpenedCard++;
                            if ((checkOpenedCards(openedCard1, openedCard2) == true)) {
                                invalidate();
                                cards.remove(openedCard1); // если карты одинаковые, удаляем их
                                cards.remove(openedCard2);
                                if (cards.isEmpty() == true) {
                                    Log.d("mytag", "Вы выиграли!");
                                    myView = findViewById(R.id.myView);
                                    Toast toast = Toast.makeText(myView.getContext(), "Вы выиграли!", Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                } else {
                                    amountOpenedCard = 0;
                                }
                                return true;
                            } else {
                                invalidate();
                                PauseTask task = new PauseTask();
                                task.execute(PAUSE_LENGTH);
                                isOnPauseNow = true;
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }


    public boolean checkOpenedCards (Card card1, Card card2){
        if (card1.equals(card2) == false) {
            if (card1.color == card2.color) {
                return true;
            }
        }
        return false;
    }

    public void newGame() {
        cards.clear();

    }


    class PauseTask extends AsyncTask<Integer, Void, Void> {
        protected Void doInBackground(Integer... integers) {
            //Log.d("mytag", "Pause started");
            try {
                Thread.sleep(integers[0] * 1000); // передаём число секунд ожидания
            } catch (InterruptedException e) {}
            //Log.d("mytag", "Pause finished");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            for (Card c: cards) {
                if (c.isOpen) {
                    c.isOpen = false;
                }
            }
            amountOpenedCard = 0;
            isOnPauseNow = false;
            invalidate();
        }
    }
}


