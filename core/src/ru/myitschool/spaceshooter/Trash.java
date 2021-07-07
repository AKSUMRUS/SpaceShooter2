package ru.myitschool.spaceshooter;

import com.badlogic.gdx.math.MathUtils;

public class Trash extends SpaceObject {
    float aRotation, vRotation; // угол и скорость вращения

    // конструктор пнинимает объект, чтобы обломок появлялся в координатах этого объекта
    Trash(SpaceObject o) {
        float a; // угол, под которым полетит обломок
        float v; // скорость
        x = o.x;
        y = o.y;
        width = MathUtils.random(10, 20);
        height = width;
        a = MathUtils.random(0, 360);
        v = MathUtils.random(1f, 10f);
        vx = v * MathUtils.sinDeg(a);
        vy = v * MathUtils.cosDeg(a);
        vRotation = MathUtils.random(-10, 10);
    }

    @Override
    void move() {
        super.move();

        // проверка вылета за экран
        if (x < 0 - width || x > SpaceShooter.SCR_WIDTH ||
                y < 0 - height || y > SpaceShooter.SCR_HEIGHT)
            isAlive = false;

        // вращение
        aRotation += vRotation;
    }
}
