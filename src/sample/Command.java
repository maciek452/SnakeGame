package sample;

import javafx.scene.input.KeyCode;

import java.io.Serializable;

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

    /**
     * Wybrana komenda
     */
    Type type;

    /**
     * Dodatkowe dane dla odbiorcy
     */
    Point point;
    String string;
    KeyCode keyCode;
    int ilosc, N;
    double blockSize;
    char[][] tab;


    public Command(String string){
        this.string = string;
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
