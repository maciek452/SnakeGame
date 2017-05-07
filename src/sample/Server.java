package sample;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;
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
    static ExecutorService executor = Executors.newFixedThreadPool(6);
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

    private static TimerTask runSnake(Snake snake){
        return new TimerTask() {

            @Override
            public void run() {
                snake.makeMove(map);
            }
        };
    }

    private static void waitForClient() {
        try {
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            log.info("Client connected.");

            Timer timer = new Timer();//create a new Timer
            switch (numberOfSnakes){
                case 0:
                    snake1 = new Snake(new Point(5,5));
                    executor.submit(()->receiveCommands(inputStream, outputStream, snake1));
                    timer.scheduleAtFixedRate(runSnake(snake1), 30, 100);
                    break;
                case 1:
                    snake2 = new Snake(new Point(7,7));
                    executor.submit(()->receiveCommands(inputStream, outputStream, snake2));
                    timer.scheduleAtFixedRate(runSnake(snake2), 30, 100);
                    break;
                case 2:
                    snake3 = new Snake(new Point(5,9));
                    executor.submit(()->receiveCommands(inputStream, outputStream, snake3));
                    timer.scheduleAtFixedRate(runSnake(snake3), 30, 100);
                    break;
                default:
                    log.info("Server has arleady 3 players.");
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized static void receiveCommands(DataInputStream inputStream, DataOutputStream outputStream, Snake snake){

        try {
            while (true) {
                //oczekiwanie na kolejną komendę
                byte[] message = new byte[1];
                int length = inputStream.readInt();
                if(length>0){
                    message = new byte[length];
                    inputStream.readFully(message, 0, message.length);
                }
                Command command = (Command) Serializer.deserialize(message);

                switch (command.getType()){
                    case GET_DIMENSIONS:
                        message = Serializer.serialize(new Command(ilosc, N, rozmiar_bloku));
                        outputStream.writeInt(message.length);
                        outputStream.write(message);
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
                        message = Serializer.serialize(new Command(string));
                        outputStream.writeInt(message.length);
                        outputStream.write(message);
                }
                if(snake.enabled) {
                    //log.info("Making move");
                    //snake.makeMove(map);
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
