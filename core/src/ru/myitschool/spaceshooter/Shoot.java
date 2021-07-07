package ru.myitschool.spaceshooter;

// класс выстрела

public class Shoot extends SpaceObject {

    // конструктор принимает класс корабля, чтобы выстрел появлялся там же
    Shoot(Ship ship){
        width = 64;
        height = 64;
        x = ship.x;
        y = ship.y;
        vy = 5;
    }
}
