package sample;

import javafx.application.Platform;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
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

    //private Map map;

    private ObjectInputStream inputStream;


    public static void main(String[] args)throws IOException{
        log.info("Server starts.");
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            final Socket socket = serverSocket.accept();
            //Oczekiwania na połączenie od klienta po stronie serwerowej
            executor.submit(()->waitForClient(serverSocket, socket));

        }catch (IOException e){
            log.info("Can't setup server on this port number.");
        }
    }

    private static void waitForClient(ServerSocket serverSocket, Socket socket) {
        try {
            //Oczekiwania na połączenie od klienta po stronie serwerowej
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            executor.submit(()->receiveCommands(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveCommands(ObjectInputStream inputStream){

        try {
            while (true) {
                //oczekiwanie na kolejną komendę
                //log.info("Waiting for point.");
                Command command = (Command) inputStream.readObject();
                Point point = (Point) command.getPayload();
                log.info(format("Receiving point: %s, %s", point.x, point.y));
            }
        } catch (EOFException | SocketException e) {
            System.out.println("Nastąpiło rozłączenie");
            //disconnect();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

}
