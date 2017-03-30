package sample;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * Created by Maciek on 27.03.2017.
 */
public class Server{

    private static Logger log = Logger.getLogger(Server.class.getCanonicalName());
    static ExecutorService executor = Executors.newFixedThreadPool(3);
    private static final int PORT = 1337;
    private static final int PORT1 = 1338;

    private static Map map;
    private static String string;


    private static Socket socket;

    private ObjectInputStream inputStream;

    public static void main(String[] args)throws IOException{

        map = new Map();

        log.info("Server starts.");
        while(true) {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {

                socket = serverSocket.accept();

                //Oczekiwania na połączenie od klienta po stronie serwerowej
                executor.submit(() -> waitForClient());

            } catch (IOException e) {
                log.info("Can't setup server on this port number.");
            }
        }
    }

    private static void waitForClient() {
        try {
            //Oczekiwania na połączenie od klienta po stronie serwerowej
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

            executor.submit(()->receiveCommands(inputStream, outputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveCommands(ObjectInputStream inputStream, ObjectOutputStream outputStream){

        try {
            while (true) {
                //oczekiwanie na kolejną komendę
                //log.info("Waiting for point.");
                Command command = (Command) inputStream.readObject();
                if(command.getType()== Command.Type.SEND_MAP){
                    outputStream.writeObject(new Command(Command.Type.MAP_TAB, map.getmap()));
                }
                if(command.getType() == Command.Type.POINT){
                    Point point = (Point) command.getPayload();
                    log.info(format("Receiving point: %s, %s", point.x, point.y));
                }
                if(command.getType() == Command.Type.MAP_TAB){
                    map.setMap(command.getTab());
                    log.info(format("Map updated."));
                }

            }
        } catch (EOFException | SocketException e) {
            System.out.println("Nastąpiło rozłączenie");
            //disconnect();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

}
