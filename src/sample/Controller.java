package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    @FXML
    private String string;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Logger log = Logger.getLogger(Server.class.getCanonicalName());
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private GraphicsContext gc;

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
                    //"192.168.0.98"
                    , PORT);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

        }catch (IOException e){
            log.info("Can't setup client on this port number.");
        }

        getDimensionsFromServer();
        string = getStringFromServer();
    }
    @FXML
    public void handle(KeyEvent event) {
        try {
            outputStream.writeObject(new Command(Command.Type.CHANGE_DIRECTION, event.getCode()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void start(){
        try {
            outputStream.writeObject(new Command(Command.Type.START));
        } catch (IOException e) {
            e.printStackTrace();
        }

        TimerTask timerTask = new TimerTask() {

            @Override
            public void run() {
                string = getStringFromServer();
                drawShapes(gc);
            }
        };

        Timer timer = new Timer();//create a new Timer

        timer.scheduleAtFixedRate(timerTask, 30, 100);

        //executor.submit(this::gettingMapAndDrowing);
    }

    void gettingMapAndDrowing(){
        while (true){
            //pobierzMape
            string = getStringFromServer();
            drawShapes(gc);
        }

    }

    public void getDimensionsFromServer(){
        try{
            outputStream.writeObject(new Command(Command.Type.GET_DIMENSIONS));

            Command command = (Command) inputStream.readObject();
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

    public String getStringFromServer(){
        try{
            outputStream.writeObject(new Command(Command.Type.SEND_MAP_STRING));
            Command command = (Command) inputStream.readObject();
            return (String) command.getString();

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

    public void drawShapes(GraphicsContext gc) {
        this.gc = gc;
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