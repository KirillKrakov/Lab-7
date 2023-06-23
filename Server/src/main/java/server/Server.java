package server;

import common.communication.Request;
import common.communication.Response;
import common.utility.Outputer;
import server.utility.CollectionManager;
import server.utility.CommandManager;
import server.utility.RequestManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Runs the server.
 */
public class Server {
    private int port;
    private RequestManager requestManager;

    class CommonResource {
        boolean flag = true;
    }

    public Server(int port, RequestManager requestManager) {
        this.port = port;
        this.requestManager = requestManager;
    }

    public void run() {
        try {
            DatagramSocket serverSocket = new DatagramSocket(port);
            serverSocket.setReuseAddress(true);
            Queue<DatagramPacket> received = new LinkedList<>();
            Queue<AbstractMap.SimpleEntry<SocketAddress, Request>> toHandle = new LinkedList<>();
            Queue<AbstractMap.SimpleEntry<SocketAddress, Response>> toSend = new LinkedList();
            Outputer.println("Прослушивание порта " + port);
            ServerApp.logger.info("Прослушивание порта " + port);
            while (true) {
                System.out.println(Thread.activeCount());
                byte[] buffer = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    serverSocket.receive(packet);
                } catch (IOException exception) {
                    Outputer.printerror("Произошла ошибка при попытке использовать порта '" + port + "'!");
                    ServerApp.logger.fatal("Произошла ошибка при попытке использовать порт '" + port + "'!");
                    System.exit(0);
                }
                received.add(packet);
                if (!received.isEmpty()) {
                    DatagramPacket receivedPoll = received.poll();
                    if (receivedPoll == null) {
                        continue;
                    } else {
                        Thread receiveThread = new Thread(() -> {
                            String newMes = new String(receivedPoll.getData(), StandardCharsets.UTF_8).trim();
                            Request request = Request.outOfString(newMes);
                            toHandle.add(new AbstractMap.SimpleEntry<>(receivedPoll.getSocketAddress(), request));
                        });
                        receiveThread.start();
                        do {
                            try{
                                receiveThread.join();
                            } catch (InterruptedException e) {}
                        } while (receiveThread.isAlive());
                        receiveThread.interrupt();
                    }
                }
                if (!toHandle.isEmpty()) {
                    AbstractMap.SimpleEntry<SocketAddress, Request> toHandlePoll = toHandle.poll();
                    if (toHandlePoll == null) {
                        continue;
                    } else {
                        Thread handleThread = new Thread(() -> {
                            Request request = toHandlePoll.getValue();
                            Response responseToUser = requestManager.handle(request);
                            toSend.add(new AbstractMap.SimpleEntry<>(toHandlePoll.getKey(), responseToUser));
                        });
                        handleThread.start();
                        do {
                            try{
                                handleThread.join();
                            } catch (InterruptedException e) {}
                        } while (handleThread.isAlive());
                        handleThread.interrupt();
                    }
                }
                if (!toSend.isEmpty()) {
                    AbstractMap.SimpleEntry<SocketAddress, Response> toSendPoll = toSend.poll();
                    if (toSendPoll != null) {
                        Thread sendThread = new Thread(() -> {
                            try {
                                byte[] response = toSendPoll.getValue().getBytes();
                                DatagramPacket responsePacket = new DatagramPacket(response, response.length, toSendPoll.getKey());
                                serverSocket.send(responsePacket);
                            } catch (IOException exception) {
                                Outputer.printerror("Непредвиденный разрыв соединения с клиентом!");
                                ServerApp.logger.warn("Непредвиденный разрыв соединения с клиентом!");
                            }
                        });
                        sendThread.start();
                        do {
                            try{
                                sendThread.join();
                            } catch (InterruptedException e) {}
                        } while (sendThread.isAlive());
                        sendThread.interrupt();
                    }
                }
            }

        } catch (IOException e) {
            Outputer.printerror("Произошла ошибка при попытке использовать порт '" + port + "'!");
            ServerApp.logger.fatal("Произошла ошибка при попытке использовать порт '" + port + "'!");
            Outputer.printerror("Сервер не может быть запущен!");
            ServerApp.logger.fatal("Сервер не может быть запущен!");
        }
    }
}