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
        snake1.changeDirection(event.getCode());
    }

    @FXML
    public void start(){
        map =new Map(sendCeckMapSignal());
        snake1 = new Snake(new Point(5, 5));
        string = new String();
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++)
                string += map.chceckBlock(new sample.Point(j, i));
            string+="\n";
        }
        setLabel(string);
        executor.submit(this::dzialaj);
    }

    @FXML
    public void start1(){
        map =new Map(sendCeckMapSignal());
        snake1 = new Snake(new Point(7, 7));
        string = new String();
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++)
                string += map.chceckBlock(new sample.Point(j, i));
            string+="\n";
        }
        setLabel(string);
        executor.submit(this::dzialaj);
    }

    public void setLabel(String string){
        label.setText(string);
    }

    void dzialaj(){

        while (true){
            try {

                //pobierzMape
                map.setMap(sendCeckMapSignal());
                //porusza sie
                snake1.makeMove(map);
                //wysyła mape
                sendMap();
                string = "";
                for (int i = 0; i < 20; i++) {
                    for (int j = 0; j < 20; j++)
                        string += map.chceckBlock(new Point(j, i));
                    string+="\n";
                }
                Platform.runLater(()->setLabel(string));
                Thread.sleep(200);
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

    public void sendMap(){
        try{
            outputStream.writeObject(new Command(Command.Type.MAP_TAB, map.getmap()));
        }catch (IOException e){
            log.info("Can't send map.");
        }
    }
}