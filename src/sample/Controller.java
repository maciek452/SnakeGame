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

import java.awt.*;
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
    @FXML
    public Label player1, player2, player3, Stoper;
    private String mapa;
    private ExecutorService executor = Executors.newFixedThreadPool(2);
    private static Logger log = Logger.getLogger(Server.class.getCanonicalName());
    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;
    public GraphicsContext gc;
    int width, height, number_of_players = 0;
    double blockSize;
    Image earth_pic, wall_pic, apple_pic;
    Image[] tail_pic, headUp_pic, headDown_pic, headLeft_pic, headRight_pic;
    String ip;
    static long endTime;
    static boolean clock = false;
    private static final int PORT = 1337;

    @Override
    public void initialize(URL location, ResourceBundle resources){

        try {
            socket = new Socket(ip, PORT);
            outputStream = new DataOutputStream(socket.getOutputStream());
            inputStream = new DataInputStream(socket.getInputStream());

        }catch (IOException e){
            log.info("Can't setup client on this port number.");
        }

        try{
            //pobieramy rozmiary
            int length = inputStream.readInt();
            byte [] message = new byte[length];
            inputStream.readFully(message, 0, message.length);

            Command command = (Command) Serializer.deserialize(message);
            width = command.M;
            height = command.N;
            blockSize = command.blockSize;
            log.info(format("width = %d, matrix= %d, block size = %f", width, height, blockSize));

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
    public void start() {
        endTime = System.currentTimeMillis() + 15 * 60 * 1000;
        Stoper.setOpacity(1.0);
        mapa = "";
        makeCommand(Command.Type.START);
        getNumberOfPlayers();
        executor.submit(() -> getChangesFromServer());
        executor.submit(() -> countdown());
    }

    private void getNumberOfPlayers(){
        try{
            number_of_players = inputStream.readInt();
            log.info("Number of players: "+number_of_players);
        }catch (IOException e){
            log.info("Can't send point.");
        }
    }

    public void countdown() {
        while (true) {
            if (clock == true) {
                long timeLeft = endTime - System.currentTimeMillis();
                Platform.runLater(() -> 
                        Stoper.setText(format("%02d:%02d:%03d", timeLeft / 60000, (timeLeft / 1000) % 60, timeLeft % 1000)));
            }
            try {
                Thread.sleep(9);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void setScores(Command command){
        Platform.runLater(()->player1.setText( format("%d",command.score1*100)));
        Platform.runLater(()->player2.setText( format("%d",command.score2*100)));
        Platform.runLater(()->player3.setText( format("%d",command.score3*100)));
    }

    public synchronized void getChangesFromServer(){
        try{
            while(true) {
                int length = inputStream.readInt();
                byte[] message = new byte[length];
                inputStream.readFully(message, 0, message.length);
                Command command = (Command) Serializer.deserialize(message);
                setScores(command);
                if(mapa == ""){
                    mapa = command.getString();
                    drawAllShapes(gc);
                    clock = true;
                }else {
                    mapa = updateString(mapa, command.getString());
                }
            }
        }catch (IOException e){
            log.info("Can't send point.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.info("Can't find class inputStream.");
        }
    }

    public String updateString(String oldString, String newString){
        char[] oldArray = oldString.toCharArray();
        int i = 0;
        Vector<Payload> vector = new Vector<>();
        for(char c : oldArray){
            if(c != newString.charAt(i)){
                vector.add(new Payload(new Point(i%width, i/width), newString.charAt(i)));
            }
            i++;
        }
        Platform.runLater(()->vector.forEach(this::setImage));
        return newString;
    }

    public void LoadGraphics() {
        String tab[] = new String[]{"red", "aqua", "green"};
        tail_pic = new Image[3];
        headDown_pic = new Image[3];
        headUp_pic = new Image[3];
        headLeft_pic = new Image[3];
        headRight_pic = new Image[3];
        for (int i = 0; i < 3; i++) {
            tail_pic[i] = new Image("File:src/Graphics/"+tab[i]+"/snake.png");
            headUp_pic[i] = new Image("File:src/Graphics/"+tab[i]+"/snakeHeadUp.png");
            headDown_pic[i] = new Image("File:src/Graphics/"+tab[i]+"/snakeHeadDown.png");
            headLeft_pic[i] = new Image("File:src/Graphics/"+tab[i]+"/snakeHeadLeft.png");
            headRight_pic[i] = new Image("File:src/Graphics/"+tab[i]+"/snakeHeadRight.png");
        }
        earth_pic = new Image("File:src/Graphics/Grass.png");
        wall_pic = new Image("File:src/Graphics/wall.png");
        apple_pic = new Image("File:src/Graphics/Apple.png");
    }

    public void setImage(Payload payload){

        switch(payload.getChar())
        {
            case '#':
                gc.drawImage(wall_pic, payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case ' ':
                gc.drawImage(earth_pic,payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case '>':
                gc.drawImage(headRight_pic[0], payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case '<':
                gc.drawImage(headLeft_pic[0], payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case '^':
                gc.drawImage(headUp_pic[0], payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case 'V':
                gc.drawImage(headDown_pic[0], payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case 'O':
                gc.drawImage(tail_pic[0], payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case 'd':
                gc.drawImage(headRight_pic[1], payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case 'a':
                gc.drawImage(headLeft_pic[1], payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case 'w':
                gc.drawImage(headUp_pic[1], payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case 's':
                gc.drawImage(headDown_pic[1], payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case 'Q':
                gc.drawImage(tail_pic[1], payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case 'h':
                gc.drawImage(headRight_pic[2], payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case 'f':
                gc.drawImage(headLeft_pic[2], payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case 't':
                gc.drawImage(headUp_pic[2], payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case 'g':
                gc.drawImage(headDown_pic[2], payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case 'Y':
                gc.drawImage(tail_pic[2], payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
            case '.':
                gc.drawImage(apple_pic, payload.getPoint().getX()*blockSize, payload.getPoint().getY()*blockSize, blockSize, blockSize);
                break;
        }
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
                switch(mapa.charAt(i*width+j))
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
                    gc.drawImage(headRight_pic[0], x, y, blockSize, blockSize);
                    break;
                case '<':
                    gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                    gc.drawImage(headLeft_pic[0], x, y, blockSize, blockSize);
                    break;
                case '^':
                    gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                    gc.drawImage(headUp_pic[0], x, y, blockSize, blockSize);
                    break;
                case 'V':
                    gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                    gc.drawImage(headDown_pic[0], x, y, blockSize, blockSize);
                    break;
                case 'O':
                    gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                    gc.drawImage(tail_pic[0], x, y, blockSize, blockSize);
                    break;
                case 'd':
                    gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                    gc.drawImage(headRight_pic[1], x, y, blockSize, blockSize);
                    break;
                case 'a':
                    gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                    gc.drawImage(headLeft_pic[1], x, y, blockSize, blockSize);
                    break;
                case 'w':
                    gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                    gc.drawImage(headUp_pic[1], x, y, blockSize, blockSize);
                    break;
                case 's':
                    gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                    gc.drawImage(headDown_pic[1], x, y, blockSize, blockSize);
                    break;
                case 'Q':
                    gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                    gc.drawImage(tail_pic[1], x, y, blockSize, blockSize);
                    break;
                case 'h':
                    gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                    gc.drawImage(headRight_pic[2], x, y, blockSize, blockSize);
                    break;
                case 'f':
                    gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                    gc.drawImage(headLeft_pic[2], x, y, blockSize, blockSize);
                    break;
                case 't':
                    gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                    gc.drawImage(headUp_pic[2], x, y, blockSize, blockSize);
                    break;
                case 'g':
                    gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                    gc.drawImage(headDown_pic[2], x, y, blockSize, blockSize);
                    break;
                case 'Y':
                    gc.drawImage(earth_pic, x, y, blockSize, blockSize);
                    gc.drawImage(tail_pic[2], x, y, blockSize, blockSize);
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