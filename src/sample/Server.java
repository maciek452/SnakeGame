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
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by Maciek on 27.03.2017.
 */
public class Server{

    private static int canvasX = 1200, canvasY = 600, height = 30;
    private static double blockSize = canvasY / height;
    private static int width = (int) (canvasX/ blockSize);
    private static Logger log = Logger.getLogger(Server.class.getCanonicalName());
    private static ExecutorService executor = Executors.newFixedThreadPool(7);
    private static final int PORT = 1337;
    private static Map map;
    private static Socket socket;
    private static ServerSocket serverSock;
    private static int numberOfSnakes = 0;
    private static Snake snake1, snake2, snake3;
    private static long time = 30*1000*1, endTime;
    private static boolean gameFinishStatus = false;
    private static boolean gameFinishing = false;
    private static boolean gameStartStatus = false;
    private static Timer timer = new Timer();
    private static int timersFinished = 0;

    public static void main(String[] args)throws IOException{


        if(args.length>0)
            time = Long.parseLong(args[0])*60*1000;
        map = new Map(height, width);
        log.info("Server starts.");
        endTime = System.currentTimeMillis() + time;
        while(!isGameFinished()) {
            try(ServerSocket serverSocket = new ServerSocket(PORT)) {
                serverSock = serverSocket;
                socket = serverSocket.accept();
                if(isGameFinished()) break;
                waitForClient();
            } catch (IOException e) {
                log.info("Can't setup server on this port number.");
            }
        }

        socket.close();
        executor.shutdownNow();
        timer.cancel();
        timer.purge();

        System.exit(0);

    }

    private synchronized static TimerTask runSnake(Snake snake){
        return new TimerTask() {
            @Override
            public void run() {
                if(isGameFinished()){
                    this.cancel();
                }
                if(snake.enabled)
                    snake.makeMove(map);
            }
        };
    }

    private static void waitForClient() {
        try{
            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
            log.info("Client "+numberOfSnakes+" connected.");
            sendDimensions(outputStream);

            switch (numberOfSnakes){
                case 0:
                    snake1 = new Snake(new Point(5,5));
                    executor.submit(()->receiveCommands(inputStream, outputStream, snake1));
                    timer.scheduleAtFixedRate(runSnake(snake1), 30, 100);
                    numberOfSnakes++;
                    break;
                case 1:
                    snake2 = new Snake(new Point(5,7));
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
                    log.info("Server has already 3 players.");
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
                    byte[] message;
                    if(isGameFinishing()){
                        message = Serializer.serialize(new Command(Command.Type.SHUTDOWN));
                        outputStream.writeInt(message.length);
                        outputStream.write(message);
                        synchronized (this){
                            timersFinished++;
                        }
                        this.cancel();
                    }
                    String string = new String();
                    for (int i = 0; i < height; i++) {
                        for (int j = 0; j < width; j++)
                            string += map.checkBlock(new Point(j, i));
                    }

                    switch (numberOfSnakes){
                        case 1:
                            message = Serializer.serialize(new Command(Command.Type.MAP, string, snake1.score, 0, 0));
                            break;
                        case 2:
                            message = Serializer.serialize(new Command(Command.Type.MAP,string, snake1.score, snake2.score, 0));
                            break;
                        case 3:
                            message = Serializer.serialize(new Command(Command.Type.MAP,string, snake1.score, snake2.score, snake3.score));
                            break;
                        default:
                            message = Serializer.serialize(new Command(Command.Type.MAP,string, 0, 0, 0));
                            break;
                    }
                    outputStream.writeInt(message.length);
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
    private static void terminate()
    {
        Timer clock = new Timer();
        clock.schedule(new TimerTask() {
            @Override
            public void run() {

                int players = numberOfSnakes;
                setGameFinishingStatus();

                while (getTimersFinished() != players);

                setGameFinishStatus();
                try {
                    serverSock.close();
                }catch (IOException e){
                    log.info("zamykanie glownego socketa sie nie powiodlo");
                }
            }
        }, time);
    }


    private static void receiveCommands(DataInputStream inputStream, DataOutputStream outputStream, Snake snake){
        byte[] message;
        int length;
        try {
            log.info("Receiving from client"+numberOfSnakes);
            boolean exit  = false;
            while (!exit) {
                if(isGameFinished()) exit = true;
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
                        if(!isGameStarted()){
                            endTime = System.currentTimeMillis() + time;
                            terminate();
                        }
                        setGameStartStatus();
                        sendData(outputStream);
                        log.info("Player starts game");
                        snake.enable();
                        Timer timer = new Timer();
                        timer.scheduleAtFixedRate(sendWholeMap(outputStream, snake), 100, 100);
                        sendWholeMap(outputStream, snake);
                        map.startAppleTask();
                        break;
                    case SHUTDOWN:
//                        log.info("Player disconnected.");
//                        snake.disable();
//                        snake.deleteSnake(map);
//                        numberOfSnakes--;
                        exit = true;
                        break;
                }
            }
        } catch (EOFException e){
            log.info("Server listener EOFException");
        } catch (SocketException e) {
            log.info("Disconnection took place.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            inputStream.close();
            outputStream.close();
        }catch (IOException e){
            log.info(e.getMessage());
        }
        return;
    }

    private static void sendData(DataOutputStream outputStream){
        try {
            byte [] message = Serializer.serialize(new Command(Command.Type.DATA, endTime, numberOfSnakes));
            outputStream.writeInt(message.length);
            outputStream.write(message);
            return;
        }catch(IOException e){
            log.info(e.getMessage());
        }
    }


    private static synchronized boolean isGameFinished(){
        return gameFinishStatus;
    }
    private static synchronized void setGameFinishStatus(){
        gameFinishStatus = true;
    }

    private static synchronized boolean isGameFinishing(){
        return gameFinishing;
    }
    private static synchronized void setGameFinishingStatus(){
        gameFinishing = true;
    }
    private static synchronized boolean isGameStarted(){
        return gameStartStatus;
    }

    private static synchronized void setGameStartStatus(){
        gameStartStatus = true;
    }

    private static synchronized int getTimersFinished(){
        return timersFinished;
    }
}