package sample;

import java.awt.*;
import java.util.Random;

/**
 * Created by Maciek on 26.03.2017.
 */


public class Map {


    char[][] map;
    int N, M;
    Random rand = new Random();

    public Map(){

    }

    public Map(int height, int M){
        map  =new char[height][M];
        this.N = height;
        this.M = M;
        for(int i = 0; i < height; i++ ) {
            for (int j = 0; j < M; j++) {
                if (i == 0 || j == 0 || i == height - 1 || j == M - 1)
                    map[i][j] = '#';
                else map[i][j]=' ';
            }
        }
        makeDot();
    }

    public char chceckBlock(Point point){
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

    public void setSnakePiece(Point point, char x){
        map[point.y][point.x] = x;
    }

    public char[][] getmap(){
        return map;
    }

    public void setMap(char[][] tab){
        this.map = tab;
    }
}
