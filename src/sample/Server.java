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

/**
 * Created by Maciek on 27.03.2017.
 */
public class Server{

    static int canvasX = 1200, canvasY = 600, N = 31;
    static double rozmiar_bloku = canvasY / N;
    static int ilosc = (int) (canvasX/rozmiar_bloku);

    private static Logger log = Logger.getLogger(Server.class.getCanonicalName());
    static ExecutorService executor = Executors.newFixedThreadPool(3);
    private static final int PORT = 1337;

    private static Map map;

    private static Socket socket;

    private static int numberOfSnakes = 0;
    private static Snake snake1, snake2, snake3;

    public static void main(String[] args)throws IOException{
        map = new Map(N, ilosc);

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
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            log.info("Client connected.");
            switch (numberOfSnakes){
                case 0:
                    snake1 = new Snake(new Point(5,5));
                    executor.submit(()->receiveCommands(inputStream, outputStream, snake1));
                    break;
                case 1:
                    snake2 = new Snake(new Point(7,7));
                    executor.submit(()->receiveCommands(inputStream, outputStream, snake2));
                    break;
                case 2:
                    snake3 = new Snake(new Point(5,9));
                    executor.submit(()->receiveCommands(inputStream, outputStream, snake3));
                    break;
                default:
                    log.info("Server has arleady 3 players.");
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveCommands(ObjectInputStream inputStream, ObjectOutputStream outputStream, Snake snake){

        try {
            while (true) {
                //oczekiwanie na kolejną komendę
                Command command = (Command) inputStream.readObject();

                switch (command.getType()){
                    case GET_DIMENSIONS:
                        outputStream.writeObject(new Command(ilosc, N, rozmiar_bloku));
                        break;
                    case MAP_TAB:
                        map.setMap(command.getTab());
                        break;
                    case CHANGE_DIRECTION:
                        snake.changeDirection(command.getKeyCode());
                        break;
                    case START:
                        log.info("Snake enabled.");
                        snake.enable();
                        break;
                    case SEND_MAP_STRING:
                        String string = new String();
                        for (int i = 0; i < N; i++) {
                            for (int j = 0; j < ilosc; j++)
                                string += map.chceckBlock(new sample.Point(j, i));
                        }
                        log.info("Sending map");
                        outputStream.writeObject(new Command(string));
                }
                if(snake.enabled) {
                    //log.info("Making move");
                    snake.makeMove(map);
                }
            }
        } catch (EOFException | SocketException e) {
            log.info("Nastąpiło rozłączenie");
            //disconnect();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


}
