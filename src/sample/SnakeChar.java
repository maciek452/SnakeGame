package sample;


import com.sun.javafx.scene.traversal.Direction;

/**
 * Created by Zając on 30.05.2017.
 */
public class SnakeChar {
    char[][] tab;

    SnakeChar(){

        tab = new char[][]{
                {'>', 'd', 'h' },
                {'<', 'a', 'f' },
                {'^', 'w', 't' },
                {'V', 's', 'g' },
                {'O', 'Q', 'Y' }
        };
    }

    public char get(Direction dir, int snakeId){
        switch(dir){
            case RIGHT:
                return tab[0][snakeId];
            case LEFT:
                return tab[1][snakeId];
            case UP:
                return tab[2][snakeId];
            case DOWN:
                return tab[3][snakeId];
            default:
                return ' ';
        }
    }
    public char get(int snakeId){
        return tab[4][snakeId];
    }


}
