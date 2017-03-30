package sample;

import java.util.Random;

/**
 * Created by Maciek on 26.03.2017.
 */


public class Map {

    static int N = 20, M = 20;

    char[][] map = new char[N][M];

    Random rand = new Random();

    public Map(){
        for(int i = 0; i < N; i++ ) {
            for (int j = 0; j < M; j++) {
                if (i == 0 || j == 0 || i == N - 1 || j == M - 1)
                    map[i][j] = '#';
                else map[i][j]=' ';
            }
        }
        makeDot();

    }

    public char chceckBlock(sample.Point point){
        return map[point.y][point.x];
    }

    public void makeDot(){
        Point tmp = new Point();
        do{
            tmp.x = rand.nextInt(M-2)+1;
            tmp.y = rand.nextInt(N-2)+1;

        }while (chceckBlock(tmp)!=' ');
        map[tmp.y][tmp.x] = '.';
    }

    public void erasePiece(Point point){
        map[point.y][point.x] = ' ';
    }

    public void setSnakePiece(Point point){
        map[point.y][point.x] = 'O';
    }
    public char[][] getmap(){
        return map;
    }
}
