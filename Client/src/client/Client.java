package client;

import client.utility.AuthenticationManager;
import client.utility.ClientConsoleManager;
import common.communication.Response;
import common.communication.Request;
import common.communication.ResponseCode;
import common.communication.User;
import common.exceptions.ConnectionErrorException;
import common.exceptions.NotInDeclaredLimitsException;
import common.utility.Outputer;

import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Класс, описывающий работу клиента по подключению к серверу и передаче с ним данных
 */
public class Client {
    private String host;
    private int port;
    private int reconnectionTimeout;
    private int reconnectionAttempts;
    private int maxReconnectionAttempts;
    private ClientConsoleManager clientConsoleManager;
    private AuthenticationManager authenticationManager;
    private DatagramSocket clientSocket;
    private InetAddress IPAddress;
    private User user;

    public Client(String host, int port, int reconnectionTimeout, int maxReconnectionAttempts, ClientConsoleManager clientConsoleManager, AuthenticationManager authenticationManager) {
        this.host = host;
        this.port = port;
        this.reconnectionTimeout = reconnectionTimeout;
        this.maxReconnectionAttempts = maxReconnectionAttempts;
        this.clientConsoleManager = clientConsoleManager;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Метод, запускающий соединение клиента с сервером и передачу данных между ними с возможными попытками переподключения
     */
    public void run() {
        try {
            boolean workingStatus = true;
            while (workingStatus) {
                try {
                    connectToServer();
                    processAuthentification();
                    workingStatus = communicateWithServer();
                } catch (ConnectionErrorException exception) {
                    if (reconnectionAttempts >= maxReconnectionAttempts) {
                        Outputer.printerror("Превышено количество попыток подключения!");
                        break;
                    }
                    reconnectionAttempts++;
                    Outputer.println("Попытка повторного соединения № " + reconnectionAttempts );
                }
            }
            if (clientSocket != null) clientSocket.close();
            Outputer.println("Работа клиента завершена.");
        } catch (NotInDeclaredLimitsException exception) {
            Outputer.printerror("Клиент не может быть запущен!");
        }
    }

    /**
     * Метод, отвечающий за создание клиентского сокета для подключения к серверу по протоколу UDP
     * @throws ConnectionErrorException
     * @throws NotInDeclaredLimitsException
     */
    private void connectToServer() throws ConnectionErrorException, NotInDeclaredLimitsException {
        try {
            if (reconnectionAttempts >= 1) {
                Outputer.println("Повторное соединение с сервером...");
            }
            clientSocket = new DatagramSocket();
            clientSocket.setSoTimeout(reconnectionTimeout);
            IPAddress = InetAddress.getByName(host);
            Outputer.println("Создан клиентский сокет.");
        } catch (SocketException e) {
            Outputer.printerror("Произошла ошибка при соединении с сервером!");
            throw new ConnectionErrorException();
        } catch (UnknownHostException e) {
            Outputer.printerror("Адрес хоста сервера введён некорректно!");
            throw new NotInDeclaredLimitsException();
        }
    }

    /**
     * Метод, отвечающий за передачу данных между клиентом и сервером, а также за обработку этих данных.
     * @return boolean - условие, что коммуникация между клиентом и сервером проходит без ошибок
     * @throws ConnectionErrorException
     */
    private boolean communicateWithServer() throws ConnectionErrorException {
        Request requestToServer = null;
        Response serverResponse = null;
        byte[] sendingDataBuffer = new byte[65536];
        byte[] receivingDataBuffer = new byte[65536];
        do {
            try {
                requestToServer = serverResponse != null ? clientConsoleManager.handle(serverResponse.getResponseCode(), user) :
                        clientConsoleManager.handle(null, user);
                sendingDataBuffer = requestToServer.getBytes();
                DatagramPacket sendingPacket = new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length, IPAddress, port);
                clientSocket.send(sendingPacket);
                DatagramPacket receivingPacket = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);
                clientSocket.receive(receivingPacket);
                reconnectionAttempts = 0;
                String receivedData = new String(receivingPacket.getData(), 0, receivingPacket.getLength(), StandardCharsets.UTF_8);
                serverResponse = Response.outOfString(receivedData);
                Outputer.print(serverResponse.getResponseBody());
            } catch (IOException e) {
                if (requestToServer.getCommandName().equals("exit")) {
                    return false;
                }
                Outputer.printerror("Соединение с сервером разорвано!");
                throw new ConnectionErrorException();
            }
        } while (!requestToServer.getCommandName().equals("exit"));
        return false;
    }

    private void processAuthentification() throws ConnectionErrorException {
        Request requestToServer = null;
        Response serverResponse = null;
        byte[] sendingDataBuffer = new byte[65536];
        byte[] receivingDataBuffer = new byte[65536];
        do {
            try {
                requestToServer = authenticationManager.handle();
                if (requestToServer.isEmpty()) continue;
                sendingDataBuffer = requestToServer.getBytes();
                DatagramPacket sendingPacket = new DatagramPacket(sendingDataBuffer, sendingDataBuffer.length, IPAddress, port);
                clientSocket.send(sendingPacket);
                DatagramPacket receivingPacket = new DatagramPacket(receivingDataBuffer, receivingDataBuffer.length);
                clientSocket.receive(receivingPacket);
                reconnectionAttempts = 0;
                String receivedData = new String(receivingPacket.getData(), 0, receivingPacket.getLength(), StandardCharsets.UTF_8);
                serverResponse = Response.outOfString(receivedData);
                Outputer.print(serverResponse.getResponseBody());
            } catch (InvalidClassException | NotSerializableException exception) {
                Outputer.printerror("Произошла ошибка при отправке данных на сервер!");
            } catch (IOException exception) {
                Outputer.printerror("Соединение с сервером разорвано!");
                throw new ConnectionErrorException();
            }
        } while (serverResponse == null || !serverResponse.getResponseCode().equals(ResponseCode.OK));
        user = requestToServer.getUser();
    }
}