package ru.myitschool.spaceshooter;

// класс звёздного неба в качестве фона

public class Space extends SpaceObject {
    Space(){
        width = SpaceShooter.SCR_WIDTH;
        height = SpaceShooter.SCR_HEIGHT;
        vy = -2;
    }

    @Override
    void move() {
        super.move(); // вызываем метод move из родительского класса

        if(y<=-height) y=height;
    }
}
