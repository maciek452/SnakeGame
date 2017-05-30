package sample;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
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

    static int canvasX = 1200, canvasY = 600, height = 30;
    static double blockSize = canvasY / height;
    static int width = (int) (canvasX/ blockSize);

    private static Logger log = Logger.getLogger(Server.class.getCanonicalName());
    static ExecutorService executor = Executors.newFixedThreadPool(6);
    private static final int PORT = 1337;

    private static Map map;
    private static char[][] oldMap;

    private static Socket socket;

    private static int numberOfSnakes = 0;
    private static Snake snake1, snake2, snake3;

    public static void main(String[] args)throws IOException{
        map = new Map(height, width);
        oldMap = new char[height][width];
        for (int i = 0; i< height; i++){
            for (int j = 0; j< width; j++)
            oldMap[i][j] = map.chceckBlock(new Point(j, i));
        }
        log.info("Server starts.");
        while(true) {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                socket = serverSocket.accept();
                //Oczekiwania na połączenie od klienta po stronie serwerowej
                waitForClient();
            } catch (IOException e) {
                log.info("Can't setup server on this port number.");
            }
        }
    }

    private synchronized static TimerTask runSnake(Snake snake){
        return new TimerTask() {
            @Override
            public void run() {
                if(snake.enabled)
                snake.makeMove(map);
            }
        };
    }



    private static void waitForClient() {
        try {
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            log.info("Client "+numberOfSnakes+" connected.");
            sendDimensions(outputStream);

            Timer timer = new Timer();

            switch (numberOfSnakes){
                case 0:
                    log.info("Client "+numberOfSnakes+" starts game.");
                    snake1 = new Snake(new Point(5,5));
                    executor.submit(()->receiveCommands(inputStream, outputStream, snake1));
                    timer.scheduleAtFixedRate(runSnake(snake1), 30, 100);
                    numberOfSnakes++;
                    break;
                case 1:
                    snake2 = new Snake(new Point(7,7));
                    executor.submit(()->receiveCommands(inputStream, outputStream, snake2));
                    timer.scheduleAtFixedRate(runSnake(snake2), 30, 100);
                    numberOfSnakes++;
                    break;
                case 2:
                    snake3 = new Snake(new Point(5,9));
                    executor.submit(()->receiveCommands(inputStream, outputStream, snake3));
                    timer.scheduleAtFixedRate(runSnake(snake3), 30, 100);
                    numberOfSnakes++;
                    break;
                default:
                    log.info("Server has arleady 3 players.");
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }
    private static void sendDimensions(DataOutputStream outputStream){
        try {
            byte [] message = Serializer.serialize(new Command(width, height, blockSize));
            outputStream.writeInt(message.length);
            outputStream.write(message);
            return;
        }catch(IOException e){
            log.info(e.getMessage());
        }
    }

    private static synchronized TimerTask sendWholeMap(DataOutputStream outputStream, Snake snake) {
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    String string = new String();
                    for (int i = 0; i < height; i++) {
                        for (int j = 0; j < width; j++)
                            string += map.chceckBlock(new Point(j, i));
                        //string += "\n";
                    }
                    byte[] message = Serializer.serialize(new Command(string));
                    //outputStream.writeInt(message.length);
                    outputStream.write(message);
                } catch (IOException e) {
                    log.info(e.getMessage());
                    snake.disable();
                    snake.deleteSnake(map);
                    numberOfSnakes--;
                    this.cancel();
                }
            }
        };
    }

    private static void receiveCommands(DataInputStream inputStream, DataOutputStream outputStream, Snake snake){
        byte[] message;
        int length;
        try {
            log.info("Reciving from client"+numberOfSnakes);

            while (true) {
                //oczekiwanie na kolejną komendę

                length = inputStream.readInt();
                message = new byte[length];
                inputStream.readFully(message, 0, message.length);

                Command command = (Command) Serializer.deserialize(message);

                switch (command.getType()) {
                    case CHANGE_DIRECTION:
                        snake.changeDirection(command.getKeyCode());
                        break;
                    case START:
                        log.info("Player starts game");
                        snake.enable();
                        Timer timer = new Timer();
                        timer.scheduleAtFixedRate(sendWholeMap(outputStream, snake), 100, 100);
                        sendWholeMap(outputStream, snake);
                        map.startAppleTask();
                        break;
                    case SHUTDOWN:
                        log.info("Player disconected.");
//                        snake.disable();
//                        snake.deleteSnake(map);
//                        numberOfSnakes--;
                        break;
                    case SEND_MAP_STRING:
                        String string = new String();
                        for (int i = 0; i < height; i++) {
                            for (int j = 0; j < width; j++)
                                string += map.chceckBlock(new Point(j, i));
                        }
                        //log.info("Sending map");
                        message = Serializer.serialize(new Command(string));
                        outputStream.writeInt(message.length);
                        outputStream.write(message);
                }
            }
        } catch (EOFException e){
            log.info("tu sie wyjebuje");
        } catch (SocketException e) {
            log.info("Nastąpiło rozłączenie");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


}
