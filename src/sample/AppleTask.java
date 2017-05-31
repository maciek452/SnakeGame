package sample;

import java.awt.*;

/**
 * Created by Dzejkob on 28.05.2017.
 */
public class AppleTask implements Runnable {
    Map map;
    long time;

    AppleTask(Map map){
        this.map = map;
    }

    public void run(){
        long newTime;
        resetTime();
        while(true){
            newTime = System.currentTimeMillis();
            if(newTime - getTime() > 5000){
                map.erasePiece(new Point(map.dotX, map.dotY));
                map.makeDot();
            }
        }
    }

    public synchronized void resetTime(){
        time = System.currentTimeMillis();
    }

    public synchronized long getTime() {
        return time;
    }

}
