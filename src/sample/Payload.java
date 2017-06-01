package sample;

import java.awt.*;
import java.io.Serializable;

/**
 * Created by Maciek on 27.03.2017.
 */
public class Payload implements Serializable {
    private char x;
    private Point point;

    public Payload(Point point, char c){
        this.point = point;
        this.x = c;
    }

    public char getChar(){
        return x;
    }

    public Point getPoint(){
        return point;
    }
}

