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

    private static final int PORT = 1337;

    @Override
    public void initialize(URL location, ResourceBundle resources){

        try {
            socket = new Socket("127.0.0.1", PORT);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

        }catch (IOException e){
            log.info("Can't setup client on this port number.");
        }

        map = new Map(sendCeckMapSignal());
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
        final int matrix_size = 30;
        Image earth = new Image( "File:src/Graphics/Grass.png" );
        Image wall = new Image( "File:src/Graphics/wall.png" );
        Image tail = new Image( "File:src/Graphics/snake.png" );
        Image head = new Image( "File:src/Graphics/snakeHead.png" );
        Image kox = new Image( "File:src/Graphics/kox.png" );
        double rozmiar_bloku = this.canvas.getHeight() / matrix_size;
        int ilosc = (int)(this.canvas.getWidth()/rozmiar_bloku);
        gc.setFill(Color.GREEN);
        gc.setStroke(Color.DARKGREEN);
        double x = (this.canvas.getWidth()-ilosc*rozmiar_bloku)/2;
        double y = 0;
        gc.drawImage(earth, 0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        for(int i = 0; i < 20; i++)
        {
            for(int j = 0; j < 20; j++)
            {
                //if(map.chceckBlock(new Point(i,j)) == '#')
                if(string.charAt(i*20+j)=='#')
                {
                    gc.drawImage(wall, x, y, rozmiar_bloku, rozmiar_bloku);
                }
                if(string.charAt(i*20+j)==' ')
                {
                    gc.strokeRect(x, y, rozmiar_bloku, rozmiar_bloku);
                }
                if(string.charAt(i*20+j)=='O')
                {
                    gc.drawImage(head, x, y, rozmiar_bloku, rozmiar_bloku);
                }
                if(string.charAt(i*20+j)=='.')
                {
                    gc.drawImage(kox, x, y, rozmiar_bloku, rozmiar_bloku);
                }
                x += rozmiar_bloku;
            }
            x = (this.canvas.getWidth() - ilosc * rozmiar_bloku)/2;
            y += rozmiar_bloku;
        }
    }
}