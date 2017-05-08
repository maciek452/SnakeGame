package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Vector;
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

    int width, height;
    double blockSize;
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
            width = command.ilosc;
            height = command.N;
            blockSize = command.blockSize;

            log.info(format("width = %d, matrix= %d, rozmiar_b = %f", width, height, blockSize));

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

    public void makeCommand(Command.Type type){
        try {
            byte[] message = Serializer.serialize(new Command(type));
            outputStream.writeInt(message.length);
            outputStream.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendShutdownSignal(){
        makeCommand(Command.Type.SHUTDOWN);
    }

    @FXML
    public void start(){
        makeCommand(Command.Type.START);

            try {
                //pobieramy mapę
                int length = inputStream.readInt();
                byte[] message = new byte[length];
                inputStream.readFully(message, 0, message.length);

                Command command = (Command) Serializer.deserialize(message);
                string = command.getString();

                log.info("String recived");
            } catch (IOException e) {
                log.info("Can't send point.");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                log.info("Can't find class inputStream.");
            }

        drawAllShapes(gc);

        gc.drawImage(wall_pic, 2*blockSize, 2*blockSize, blockSize, blockSize);
        executor.submit(() -> getChangesFromServer());
    }

    public synchronized void getChangesFromServer(){
        try{
            while(true) {
                //og.info("Odebrało kurwa");
                int length = inputStream.readInt();
                byte[] message = new byte[length];
                inputStream.readFully(message, 0, message.length);
                Command command = (Command) Serializer.deserialize(message);
                drawShapes(gc, command.getVector());
            }
        }catch (IOException e){
            log.info("Can't send point.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.info("Can't find class inputStream.");
        }
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

    public void setImage(Payload payload){

        log.info("jest");
        switch(payload.getChar())
        {
            case '#':
                gc.drawImage(wall_pic, payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case ' ':
                gc.drawImage(earth_pic,payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case '>':
                gc.drawImage(headRight_pic, payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case '<':
                gc.drawImage(headLeft_pic, payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case '^':
                gc.drawImage(headUp_pic, payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case 'V':
                gc.drawImage(headDown_pic, payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case 'O':
                gc.drawImage(tail_pic, payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case '.':
                gc.drawImage(apple_pic, payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
        }
    }

    public  synchronized void drawShapes(GraphicsContext gc, Vector<Payload> vector) {

        //log.info("fun 2");
//        gc.setFill(Color.BLACK);
//        gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
//        gc.setFill(Color.GREEN);
//        gc.setStroke(Color.DARKGREEN);
//        gc.drawImage(earth_pic, 0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        vector.forEach(i->setImage(i));
//        String string = "";
//        log.info("dupa: "+vector.size());
//        for (Payload i:vector) {
//            string+=i.getChar();
//        }
        //log.info(string);
    }

    public synchronized void drawAllShapes(GraphicsContext gc) {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        gc.setFill(Color.GREEN);
        gc.setStroke(Color.DARKGREEN);
        double x = 0;
        double y = 0;
       // gc.drawImage(earth_pic, 0, 0, this.canvas.getWidth(), this.canvas.getHeight());

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                switch(string.charAt(i*width+j))
                {
                    case '#':
                        gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                        gc.drawImage(wall_pic, x, y, blockSize, blockSize);
                        break;
                    case ' ':
                        gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                        break;
                    case '>':
                        gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                        gc.drawImage(headRight_pic, x, y, blockSize, blockSize);
                        break;
                    case '<':
                        gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                        gc.drawImage(headLeft_pic, x, y, blockSize, blockSize);
                        break;
                    case '^':
                        gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                        gc.drawImage(headUp_pic, x, y, blockSize, blockSize);
                        break;
                    case 'V':
                        gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                        gc.drawImage(headDown_pic, x, y, blockSize, blockSize);
                        break;
                    case 'O':
                        gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                        gc.drawImage(tail_pic, x, y, blockSize, blockSize);
                        break;
                    case '.':
                        gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                        gc.drawImage(apple_pic, x, y, blockSize, blockSize);
                        break;
                }
                x += blockSize;
            }
            x = 0;
            y += blockSize;
        }
    }
}