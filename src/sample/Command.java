package sample;

import javafx.scene.input.KeyCode;

import java.awt.*;
import java.io.Serializable;
import java.util.Vector;

/**
 * Created by Maciek on 27.03.2017.
 */
public class Command implements Serializable {
    /**
     * Dostępne komendy
     */
    public enum Type{
        START,
        CHANGE_DIRECTION,
        SEND_MAP_STRING,
        SHUTDOWN
    }

    Type type;

    Point point;
    String string;
    KeyCode keyCode;
    Vector<Payload> vector;
    int ilosc, N;
    double blockSize;

    public Command(String string){
        this.string = string;
    }

    public Command(Vector<Payload> vector){
        this.vector = vector;
    }

    public Command(Type type, KeyCode keyCode){
        this.type = type;
        this.keyCode = keyCode;
    }

    public Command(int ilosc, int N, double block_size){
        this.ilosc = ilosc;
        this.N = N;
        this.blockSize = block_size;
    }

    public Command(Type type) {
        this.type = type;
    }

    public synchronized Vector<Payload> getVector(){
        return vector;
    }

    public synchronized Type getType() {
        return type;
    }

    public synchronized String getString(){
        return string;
    }

    public synchronized KeyCode getKeyCode(){
        return keyCode;
    }
}
