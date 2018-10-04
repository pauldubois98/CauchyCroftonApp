package com.the.unknown.cauchycroftonapp;

public class Point {
    public int mx;
    public int my;
    Point(int x, int y){
        mx = x;
        my = y;
    }
    double distTo(int x, int y){
        return Math.sqrt(squareDistTo(x,y));
    }
    double distTo(Point point){
        return Math.sqrt(squareDistTo(point));
    }
    int squareDistTo(int x, int y){
        return (x-mx)*(x-mx)+(y-my)*(y-my);
    }
    int squareDistTo(Point point){
        return (point.mx-mx)*(point.mx-mx)+(point.my-my)*(point.my-my);
    }
}
