package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.lang.String.format;

public class Controller implements Initializable{

    @FXML
    public Canvas canvas;
    private String string;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Logger log = Logger.getLogger(Server.class.getCanonicalName());
    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    public GraphicsContext gc;

    int ilosc, matrix_size;
    double rozmiar_bloku;
    Image earth_pic, wall_pic, tail_pic, headUp_pic,
            headDown_pic, headLeft_pic, headRight_pic, apple_pic;

    private static final int PORT = 1337;

    @Override
    public void initialize(URL location, ResourceBundle resources){

        try {
            socket = new Socket(
                    "127.0.0.1"
                    //"192.168.0.68"
                    //"192.168.0.97"
                    , PORT);
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());

        }catch (IOException e){
            log.info("Can't setup client on this port number.");
        }

        try{
            //pobieramy rozmiary
            int lenght = inputStream.readInt();
            byte [] message = new byte[lenght];
            inputStream.readFully(message, 0, message.length);

            Command command = (Command) Serializer.deserialize(message);
            ilosc = command.ilosc;
            matrix_size = command.N;
            rozmiar_bloku = command.rozmiar_bloku;

            log.info(format("ilosc = %d, matrix= %d, rozmiar_b = %f", ilosc, matrix_size, rozmiar_bloku));

        }catch (IOException e){
            log.info("Can't send point.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.info("Can't find class inputStream.");
        }


    }
    @FXML
    public void handle(KeyEvent event) throws IOException{
        try {
            byte[] message = Serializer.serialize(new Command(Command.Type.CHANGE_DIRECTION, event.getCode()));
            outputStream.writeInt(message.length);
            outputStream.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void start(){
        try {
            byte[] message = Serializer.serialize(new Command(Command.Type.START));
            outputStream.writeInt(message.length);
            outputStream.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.submit(() -> getStringFromServer());
//        TimerTask timerTaskDrowingMap = new TimerTask() {
//
//            @Override
//            public void run() {
//                drawShapes(gc);
//            }
//        };
//
//        Timer timer = new Timer();//create a new Timer
//
//        timer.scheduleAtFixedRate(timerTaskDrowingMap, 30, 30);

        //executor.submit(this::gettingMapAndDrowing);
    }

//    public void getDimensionsFromServer(){
//        try{
//            byte[] message = Serializer.serialize(new Command(Command.Type.GET_DIMENSIONS));
//            outputStream.writeInt(message.length);
//            outputStream.write(message);
//
//            int lenght = inputStream.readInt();
//            if(lenght>0){
//                message = new byte[lenght];
//                inputStream.readFully(message, 0, message.length);
//            }
//            Command command = (Command) Serializer.deserialize(message);
//            ilosc = command.ilosc;
//            matrix_size = command.N;
//            rozmiar_bloku = command.rozmiar_bloku;
//
//            log.info(format("ilosc = %d, matrix= %d, rozmiar_b = %f", ilosc, matrix_size, rozmiar_bloku));
//        }catch (IOException e){
//            log.info("Can't send point.");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//            log.info("Can't find class inputStream.");
//        }
//    }

    public synchronized String getStringFromServer(){
        byte[] message;
        try{
            while(true) {
                int length = inputStream.readInt();
                message = new byte[length];
                inputStream.readFully(message, 0, message.length);
                Command command = (Command) Serializer.deserialize(message);
                string = command.getString();
                drawShapes(gc);
            }

        }catch (IOException e){
            log.info("Can't send point.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.info("Can't find class inputStream.");
        }
        return null;
    }

    public void LoadGraphics() {
        earth_pic = new Image( "File:src/Graphics/Grass.png" );
        wall_pic = new Image( "File:src/Graphics/wall.png" );
        tail_pic = new Image( "File:src/Graphics/snake.png" );
        headUp_pic = new Image( "File:src/Graphics/snakeHeadUp.png" );
        headDown_pic = new Image( "File:src/Graphics/snakeHeadDown.png" );
        headLeft_pic = new Image( "File:src/Graphics/snakeHeadLeft.png" );
        headRight_pic = new Image( "File:src/Graphics/snakeHeadRight.png" );
        apple_pic = new Image( "File:src/Graphics/Apple.png" );
    }

    public  void drawShapes(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        gc.setFill(Color.GREEN);
        gc.setStroke(Color.DARKGREEN);
        double x = 0;
        double y = 0;
        gc.drawImage(earth_pic, 0, 0, this.canvas.getWidth(), this.canvas.getHeight());

        for(int i = 0; i < matrix_size; i++)
        {
            for(int j = 0; j < ilosc; j++)
            {
                switch(string.charAt(i*ilosc+j))
                {
                    case '#':
                        gc.drawImage(wall_pic, x, y, rozmiar_bloku, rozmiar_bloku);
                        break;
                    case ' ':
                        gc.strokeRect(x, y, rozmiar_bloku, rozmiar_bloku);
                        break;
                    case '>':
                        gc.drawImage(headRight_pic, x, y, rozmiar_bloku, rozmiar_bloku);
                        break;
                    case '<':
                        gc.drawImage(headLeft_pic, x, y, rozmiar_bloku, rozmiar_bloku);
                        break;
                    case '^':
                        gc.drawImage(headUp_pic, x, y, rozmiar_bloku, rozmiar_bloku);
                        break;
                    case 'V':
                        gc.drawImage(headDown_pic, x, y, rozmiar_bloku, rozmiar_bloku);
                        break;
                    case 'O':
                        gc.drawImage(tail_pic, x, y, rozmiar_bloku, rozmiar_bloku);
                        break;
                    case '.':
                        gc.drawImage(apple_pic, x, y, rozmiar_bloku, rozmiar_bloku);
                        break;
                }
                x += rozmiar_bloku;
            }
            x = 0;
            y += rozmiar_bloku;
        }
    }
}