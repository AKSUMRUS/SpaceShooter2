package ru.myitschool.spaceshooter;

public class SsButton {
    float x, y;
    float width, height;
    SsButton(float x,float y,float width, float height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    boolean isTouched(float tx,float ty){
        if(tx > x && ty > y && tx < x+width && ty < y+height){
            return true;
        }
        else {
            return false;
        }
    }
}
