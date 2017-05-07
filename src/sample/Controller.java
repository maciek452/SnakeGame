package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.lang.String.format;

public class Controller implements Initializable{

    @FXML
    public Canvas canvas;
    @FXML
    public Label label;
    private String string;
    public Map map;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Logger log = Logger.getLogger(Server.class.getCanonicalName());
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private GraphicsContext gc;

    int ilosc, matrix_size;
    double rozmiar_bloku;

    private static final int PORT = 1337;

    @Override
    public void initialize(URL location, ResourceBundle resources){

        try {
            socket = new Socket(/*"127.0.0.1"/*/
                     "192.168.0.98", PORT);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

        }catch (IOException e){
            log.info("Can't setup client on this port number.");
        }


        //map = new Map(sendCeckMapSignal());
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
        executor.submit(this::dzialaj);
    }

    public void setLabel(String string){
        //label.setText(string);
    }

    void dzialaj(){
        while (true){
            try {
                //pobierzMape
                //map.setMap(sendCeckMapSignal());
                string = getStringFromServer();

                drawShapes(gc);
                Platform.runLater(()->setLabel(string));
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void sendPoint(Point point){
        try{
            outputStream.writeObject(new Command(point));
        }catch (IOException e){
            log.info("Can't send point.");
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


    public char[][] sendCeckMapSignal(){
        try{
            outputStream.writeObject(new Command(Command.Type.SEND_MAP));

            Command command = (Command) inputStream.readObject();
            return (char[][]) command.getTab();
        }catch (IOException e){
            log.info("Can't send point.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.info("Can't find class inputStream.");
        }
        return null;
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

    public void sendMap(){
        try{
            outputStream.writeObject(new Command(Command.Type.MAP_TAB, map.getmap()));
        }catch (IOException e){
            log.info("Can't send map.");
        }
    }

    public void drawShapes(GraphicsContext gc) {
        this.gc = gc;
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        Image earth_pic = new Image( "File:src/Graphics/Grass.png" );
        Image wall_pic = new Image( "File:src/Graphics/wall.png" );
        Image tail_pic = new Image( "File:src/Graphics/snake.png" );
        Image head_pic = new Image( "File:src/Graphics/snakeHead.png" );
        Image apple_pic = new Image( "File:src/Graphics/Apple.png" );
        gc.setFill(Color.GREEN);
        gc.setStroke(Color.DARKGREEN);
        double x = 0;
        double y = 0;
        gc.drawImage(earth_pic, 0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        for(int i = 0; i < matrix_size; i++)
        {
            for(int j = 0; j < ilosc; j++)
            {
                if(string.charAt(i*ilosc+j)=='#')
                {
                    gc.drawImage(wall_pic, x, y, rozmiar_bloku, rozmiar_bloku);
                }
                if(string.charAt(i*ilosc+j)==' ')
                {
                    gc.strokeRect(x, y, rozmiar_bloku, rozmiar_bloku);
                }
                if(string.charAt(i*ilosc+j)=='O')
                {
                    gc.drawImage(head_pic, x, y, rozmiar_bloku, rozmiar_bloku);
                }
                if(string.charAt(i*ilosc+j)=='.')
                {
                    gc.drawImage(apple_pic, x, y, rozmiar_bloku, rozmiar_bloku);
                }
                x += rozmiar_bloku;
            }
            x = 0;
            y += rozmiar_bloku;
        }
    }
}