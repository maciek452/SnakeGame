package sample;

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

    static int canvasX = 1200, canvasY = 600, N = 31;
    static double rozmiar_bloku = canvasY / N;
    static int ilosc = (int) (canvasX/rozmiar_bloku);

    private static Logger log = Logger.getLogger(Server.class.getCanonicalName());
    static ExecutorService executor = Executors.newFixedThreadPool(8);
    private static final int PORT = 1337;

    private static Map map;
    private static String oldString;

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

                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < ilosc; j++)
                        oldString += map.chceckBlock(new sample.Point(j, i));
                }

            } catch (IOException e) {
                log.info("Can't setup server on this port number.");
            }
        }
    }

    private static TimerTask runSnake(Snake snake){
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
    }
    private static void sendingMap(DataOutputStream outputStream){

        while (true) {
            log.info("starting map sending");
            String string = new String();
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < ilosc; j++)
                    string += map.chceckBlock(new sample.Point(j, i));
            }
            boolean changed = false;
            for (int i = 0; i < string.length(); i++) {
                if (string.charAt(i) != oldString.charAt(i))
                    changed = true;
            }
            if (changed || !changed) {
                try {
                    byte[] message = Serializer.serialize(new Command(string));
                    outputStream.writeInt(message.length);
                    outputStream.write(message);
                    log.info("map sent");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private static void receiveCommands(DataInputStream inputStream, DataOutputStream outputStream, Snake snake){

        try {
            log.info("Reciving from client" + numberOfSnakes);

            while (true) {
                //oczekiwanie na kolejną komendę

                int length = inputStream.readInt();
                byte[] message = new byte[length];
                inputStream.readFully(message, 0, message.length);

                if(message.length > 0) {
                    Command command = (Command) Serializer.deserialize(message);

                    switch (command.getType()) {
                        case GET_DIMENSIONS:
                            synchronized (message) {
                                message = Serializer.serialize(new Command(ilosc, N, rozmiar_bloku));
                                outputStream.writeInt(message.length);
                                outputStream.write(message);
                            }
                            break;
                        case CHANGE_DIRECTION:
                            snake.changeDirection(command.getKeyCode());
                            break;
                        case START:
                            log.info("Snake enabled.");
                            snake.enable();
                            executor.submit(()->sendingMap(outputStream));
                            break;
                    }
                }
            }
        } catch (EOFException e) {
            log.info("Tu sie wyjebuje");
            //disconnect();
        } catch (SocketException e){
            log.info("Nastąpiło rozłączenie222");
            //disconnect();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


}
