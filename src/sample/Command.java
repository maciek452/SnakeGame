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
        //komenda rysowania
        SEND_MAP,
        //przesłanie mapy
        MAP_TAB,
        //rozpoczęcie gry
        START,
        //zmiana kierunku
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

    public Type getType() {
        return type;
    }

    public Payload getPayload() {
        return payload;
    }

    public char[][] getTab(){
        return tab;
    }

    public String getString(){
        return string;
    }

    public Point getPoint(){
        return point;
    }

    public KeyCode getKeyCode(){
        return keyCode;
    }
}
