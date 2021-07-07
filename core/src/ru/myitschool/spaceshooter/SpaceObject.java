package ru.myitschool.spaceshooter;

// базовый класс для всех игровых объектов

public class SpaceObject {
    float x, y; // координаты
    int width, height;
    boolean isAlive=true;
    float vx, vy; // скорость

    // конструктор не обязателен
    SpaceObject(){
    }

    // метод перемещения
    void move(){
        x+=vx;
        y+=vy;
    }

    // метод определения пересечения 2-х объектов
    boolean overlaps(SpaceObject o){
        return (x>o.x && x<o.x + o.width || o.x>x && o.x<x + width) &&
                (y>o.y && y<o.y + o.height || o.y>y && o.y<y + height);
    }
}
