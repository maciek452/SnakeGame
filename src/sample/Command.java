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
        DRAW,
        //komenda rozłączenia
        DISCONNECT
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

    public Command(Type type, Payload payload) {
        this.type = type;
        this.payload = payload;
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

    public Point getPoint(){
        return point;
    }
}
