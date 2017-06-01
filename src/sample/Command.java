package sample;

import javafx.scene.input.KeyCode;

import java.io.Serializable;

/**
 * Created by Maciek on 27.03.2017.
 */
public class Command implements Serializable {
    public enum Type{
        START,
        CHANGE_DIRECTION,
        MAP,
        DATA,
        SHUTDOWN
    }

    Type type;
    String string;
    KeyCode keyCode;
    int M, N;
    double blockSize;
    long time;
    int numberOfPlayers;
    int score1, score2, score3;

    public Command(Type type, String string, int score1, int score2, int score3){
        this.type =type;
        this.string = string;
        this.score1 = score1;
        this.score2 = score2;
        this.score3 = score3;
    }

    public Command(Type type, long time, int numberOfPlayers){
        this.type = type;
        this.time = time;
        this.numberOfPlayers = numberOfPlayers;
    }

    public Command(Type type, KeyCode keyCode){
        this.type = type;
        this.keyCode = keyCode;
    }

    public Command(int M, int N, double block_size){
        this.M = M;
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
