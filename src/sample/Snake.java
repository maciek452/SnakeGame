package sample;

import com.sun.javafx.scene.traversal.Direction;
import javafx.scene.input.KeyCode;

import java.awt.*;
import java.util.Vector;

/**
 * Created by Maciek on 26.03.2017.
 */
public class Snake {

    Vector<Point> pieces = new Vector<Point>();
    Point point;
    int lenght;
    Map map;
    Direction direction;

    public Snake(Map map, Point point){
        this.map = map;
        this.point = point;
        this.lenght = 3;

        Point tmp = new Point(point);
        pieces.add(new Point(tmp));
        tmp.x-=1;
        pieces.add(new Point(tmp));
        tmp.x-=1;
        pieces.add(new Point(tmp));

        direction = Direction.RIGHT;
        snakeUpdate();
    }

    public void enlargeSnake(Point point){
        lenght++;
        pieces.add(0, new Point(point));
    }

    public boolean collision(Point point){
        if(map.chceckBlock(point)==' ')
            return false;
        else if (map.chceckBlock(point)=='.') {
            enlargeSnake(point);
            map.makeDot();
            return true;
        }
        else return true;
    }

    public void changeDirection(KeyCode key){
        switch (key){
            case A:
                direction = Direction.LEFT;
                break;
            case S:
                direction = Direction.DOWN;
                break;
            case D:
                direction = Direction.RIGHT;
                break;
            case W:
                direction = Direction.UP;
                break;
            default:
                break;
        }
    }

    public void makeMove(){
        Point tmp = new Point(pieces.firstElement());
        switch (direction)
        {
            case RIGHT:
                tmp.x += 1;
                break;
            case UP:
                tmp.y -= 1;
                break;
            case LEFT:
                tmp.x -= 1;
                break;
            case DOWN:
                tmp.y += 1;
                break;
            default:
                break;
        }

        //collision(tmp);
        if (!collision(tmp))
        {
            pieces.add(0, new Point(tmp));
            map.erasePiece(pieces.lastElement());
            pieces.removeElementAt(pieces.size()-1);
        }

        snakeUpdate();
    }

    public void snakeUpdate(){
        pieces.forEach(i->map.setSnakePiece(i));
    }

    public int getLenght(){
        return lenght;
    }

    public Point getPiece(int index){
        return pieces.get(index);
    }
}
