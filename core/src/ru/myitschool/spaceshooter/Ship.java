package ru.myitschool.spaceshooter;

// класс нашего корабля

public class Ship extends SpaceObject {
    Ship(){
        width = 64;
        height = 64;
        x = SpaceShooter.SCR_WIDTH/2f - width/2;
        y = 20;
    }

    // переопределяем метод move, так как корабль не должен умирать при вылете за пределы экрана
    void move(){
        x+=vx;
        y+=vy;
        if(x<0) {
            x = 0;
            vx=0;
        }
        if(x>SpaceShooter.SCR_WIDTH-width) {
            x=SpaceShooter.SCR_WIDTH-width;
            vx=0;
        }
    }
}
