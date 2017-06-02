package sample;

import com.sun.javafx.scene.traversal.Direction;
import javafx.scene.input.KeyCode;

import java.awt.*;
import java.util.Vector;

/**
 * Created by Maciek on 26.03.2017.
 */
public class Snake {

    public int snakeId;
    private static boolean[] isSnakeIdTaken = new boolean[3];
    private static SnakeChar snakeChar = new SnakeChar();
    Vector<Point> pieces = new Vector<Point>();
    Point point;
    int lenght, score;
    Direction direction;
    boolean enabled = false;
    boolean movingEnabled = true;

    public Snake(Point point){
        this.score = 0;
        this.point = point;
        this.lenght = 3;
        Point tmp = new Point(point);
        pieces.add(new Point(tmp));
        tmp.x-=1;
        pieces.add(new Point(tmp));
        tmp.x-=1;
        pieces.add(new Point(tmp));
        enabled = false;
        direction = Direction.RIGHT;

        for (int i=0;i<3;i++) {
            if(!isSnakeIdTaken[i]) {
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
                isSnakeIdTaken[i] = true;
                snakeId = i;
                break;
            }
        }
    }

    public void enlargeSnake(Point point){
        lenght++;
        score++;
        pieces.add(0, new Point(point));
    }

    public boolean collision(Point point, Map map){
        if(map.checkBlock(point)==' ')
            return false;
        else if (map.checkBlock(point)=='.') {
            enlargeSnake(point);
            map.makeDot();
            return true;
        }
        else return true;
    }

    public void changeDirection(KeyCode key){
        if(movingEnabled) {
            switch (key) {
                case A:
                    if (direction != Direction.RIGHT)
                        direction = Direction.LEFT;
                    break;
                case S:
                    if (direction != Direction.UP)
                        direction = Direction.DOWN;
                    break;
                case D:
                    if (direction != Direction.LEFT)
                        direction = Direction.RIGHT;
                    break;
                case W:
                    if (direction != Direction.DOWN)
                        direction = Direction.UP;
                    break;
                default:
                    break;
            }
        }
    }

    public void makeMove(Map map){
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

        if (!collision(tmp, map))
        {
            pieces.add(0, new Point(tmp));
            map.erasePiece(pieces.lastElement());
            pieces.removeElementAt(pieces.size()-1);
        }

        snakeUpdate(map);
    }

    public void snakeUpdate(Map map){
        int i = 0;
        for (Point piece: pieces){
            if(i == 0) {
                switch (direction) {
                    case RIGHT:
                        map.setSnakePiece(piece, snakeChar.get(direction, snakeId));
                        break;
                    case LEFT:
                        map.setSnakePiece(piece, snakeChar.get(direction, snakeId));
                        break;
                    case UP:
                        map.setSnakePiece(piece, snakeChar.get(direction, snakeId));
                        break;
                    case DOWN:
                        map.setSnakePiece(piece, snakeChar.get(direction, snakeId));
                        break;
                }
            }
            else
                map.setSnakePiece(piece, snakeChar.get(snakeId));
            ++i;
        }
    }

    public void deleteSnake(Map map){
        pieces.forEach(i->map.setSnakePiece(i, ' '));
        pieces.removeAllElements();
        snakeUpdate(map);
        isSnakeIdTaken[snakeId] = false;
    }

    public void enable(){
        enabled = true;
    }

    public void disable(){
        enabled = false;
    }

}
