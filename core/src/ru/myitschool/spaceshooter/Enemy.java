package ru.myitschool.spaceshooter;

import com.badlogic.gdx.math.MathUtils;

// класс вражеского корабля

public class Enemy extends SpaceObject {
    Enemy(){
        width = 64;
        height = 64;

        // случайное появление по х за пределами экрана по y
        x = MathUtils.random(0, SpaceShooter.SCR_WIDTH-width);
        y = SpaceShooter.SCR_HEIGHT;
        vy = MathUtils.random(-5-SpaceShooter.level,-1);
    }

    @Override
    void move() {
        super.move();

        // проверка вылета за экран
        if(x<0-width || x>SpaceShooter.SCR_WIDTH ||
                y<0-height || y>SpaceShooter.SCR_HEIGHT)
            isAlive = false;
    }
}
