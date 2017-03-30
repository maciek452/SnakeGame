package sample;

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
        //komenda rozłączenia
        POINT,

        MAP_TAB
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
    char[][] tab;

    public Command(Type type, Payload payload) {
        this.type = type;
        this.payload = payload;
    }

    public Command(Type type, char[][] tab){
        this.type = type;
        this.tab = tab;
    }

    public Command(Type type, String string){
        this.type = type;
        this.string = string;
    }

    public Command(Payload payload) {
        this.payload = payload;
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
}
