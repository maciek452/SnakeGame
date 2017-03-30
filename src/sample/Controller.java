package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;

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
    public Label label;
    private Snake snake1;
    private String string;
    private Map map;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Logger log = Logger.getLogger(Server.class.getCanonicalName());
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

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
    }
    @FXML
    public void handle(KeyEvent event) {
        //snake1.changeDirection(event.getCode());
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
        label.setText(string);
    }

    void dzialaj(){

        while (true){
            try {
                //pobierzMape
                //map.setMap(sendCeckMapSignal());
                string = getStringFromServer();
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
}