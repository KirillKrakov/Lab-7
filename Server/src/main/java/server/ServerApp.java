package server;

import common.exceptions.NotInDeclaredLimitsException;
import common.exceptions.WrongAmountOfElementsException;
import common.utility.Outputer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import server.utility.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import static java.lang.System.getenv;

public class ServerApp {
    private static final int MAX_CLIENTS = 1000;
    private static String databaseUsername;
    private static int port = 8080;
    private static String databasePassword;
    private static String databaseAddress;
    public static Logger logger = LogManager.getLogger(ServerApp.class.getCanonicalName());

    public static void main(String[] args) {
        if (args.length < 1) {
            databaseAddress = "jdbc:mysql://localhost:3306/test2";
            databaseUsername = "root";
            databasePassword = "Qx349h0i@";
        } else {
            initialize(args[0]);
        }
        DatabaseHandler databaseHandler = new DatabaseHandler(databaseAddress, databaseUsername, databasePassword);
        DatabaseUserManager databaseUserManager = new DatabaseUserManager(databaseHandler);
        DatabaseCollectionManager databaseCollectionManager = new DatabaseCollectionManager(databaseHandler, databaseUserManager);
        CollectionManager collectionManager = new CollectionManager(databaseCollectionManager);
        CommandManager commandManager = new CommandManager(collectionManager, databaseCollectionManager, databaseUserManager);
        ServerConsoleManager consoleManager = new ServerConsoleManager(collectionManager);
        RequestManager requestManager = new RequestManager(commandManager);
        Thread myThread = new Thread(consoleManager);
        myThread.start();
        Server server = new Server(port, requestManager);
        server.run();
        databaseHandler.closeConnection();
    }

    private static void initialize(String filepath) {
        try (Scanner scanner = new Scanner(new BufferedReader(new FileReader(filepath)))){
            String[] lines = scanner.nextLine().split(":");
            databaseUsername = lines[3];
            databasePassword = lines[4];
            databaseAddress = "jdbc:postgresql://localhost:5432/studs";
        } catch (FileNotFoundException exception) {
            ServerApp.logger.fatal("Файл с таким именем не найден!");
        }
    }
}
