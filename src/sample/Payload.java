package sample;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Maciek on 27.03.2017.
 */
public class Payload implements Serializable {
    private char x;
    private Point point;

    public Payload(Point point, char x){
        this.point = point;
        this.x = x;
    }

    public char getChar(){
        return x;
    }

    public Point getPoint(){
        return point;
    }
}

