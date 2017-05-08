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

        SEND_MAP,

        START,

        CHANGE_DIRECTION,

        SEND_MAP_STRING,

        GET_DIMENSIONS

    }

    /**
     * Wybrana komenda
     */
    Type type;

    /**
     * Dodatkowe dane dla odbiorcy
     */
    Payload payload;
    Point point;
    String string;
    KeyCode keyCode;
    int ilosc, N;
    double rozmiar_bloku;
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
        this.rozmiar_bloku = block_size;
    }

    public Command(Type type) {
        this.type = type;
    }

    public synchronized Type getType() {
        return type;
    }

    public synchronized char[][] getTab(){
        return tab;
    }

    public synchronized String getString(){
        return string;
    }

    public synchronized Point getPoint(){
        return point;
    }

    public synchronized KeyCode getKeyCode(){
        return keyCode;
    }
}
