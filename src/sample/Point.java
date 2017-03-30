package sample;

/**
 * Created by Maciek on 27.03.2017.
 */
public class Point implements Payload {
    int x;
    int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public Point() {
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
