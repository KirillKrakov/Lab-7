package server.utility;

import java.util.Scanner;

/**
 * Класс, управляющий чтением введённых в консоль команд - нам не нужон
 */
public class ServerConsoleManager implements Runnable {
    private CollectionManager collectionManager;
    private Scanner scanner;

    public ServerConsoleManager(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        this.scanner = new Scanner(System.in);
    }

    public void run()
    {
        while(true)
        {
            if (scanner.hasNext()) {
                String input = scanner.nextLine();
                System.out.println(input.equals(""));
                if (input.equals("server_exit")) {
                    System.out.println("Работа сервера успешно завершена.");
                    System.exit(1);
                }
            }
        }
    }
}
