package client.utility;

import client.ClientApp;
import common.exceptions.MustBeNotEmptyException;
import common.exceptions.NotInDeclaredLimitsException;
import common.utility.Outputer;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Asks user a login and password.
 */
public class AuthAsker {
    private BufferedReader reader;

    public AuthAsker(BufferedReader reader) {
        this.reader = reader;
    }

    /**
     * Asks user a login.
     *
     * @return login.
     */
    public String askLogin() {
        String login;
        while (true) {
            try {
                Outputer.println("Введите логин:");
                Outputer.print(ClientApp.PS2);
                login = reader.readLine().trim();
                if (login.equals("")) throw new MustBeNotEmptyException();
                break;
            } catch (NoSuchElementException exception) {
                Outputer.printerror("Данного логина не существует!");
            } catch (MustBeNotEmptyException exception) {
                Outputer.printerror("Имя не может быть пустым!");
            } catch (IllegalStateException exception) {
                Outputer.printerror("Непредвиденная ошибка!");
                System.exit(0);
            } catch (IOException e) {
                Outputer.printerror("Операция ввода была прервана!");
                System.exit(0);
            }
        }
        return login;
    }

    /**
     * Asks user a password.
     *
     * @return password.
     */
    public String askPassword() {
        String password = null;
        while (true) {
            try {
                Outputer.println("Введите пароль:");
                Outputer.print(ClientApp.PS2);
                try {
                    Console console = System.console();
                    char[] chars = console.readPassword();
                    password = new String(chars);
                } catch (NullPointerException exception) {
                    try {
                        password = reader.readLine().trim();
                    } catch (IOException e) {
                        Outputer.printerror("Операция ввода была прервана!");
                        System.exit(0);
                    }
                }
                break;
            } catch (NoSuchElementException exception) {
                Outputer.printerror("Неверный логин или пароль!");
            } catch (IllegalStateException exception) {
                Outputer.printerror("Непредвиденная ошибка!");
                System.exit(0);
            }
        }
        return password;
    }

    /**
     * Asks a user a question.
     *
     * @param question A question.
     * @return Answer (true/false).
     */
    public boolean askQuestion(String question) {
        String finalQuestion = question + " (+/-):";
        String answer;
        while (true) {
            try {
                Outputer.println(finalQuestion);
                Outputer.print(ClientApp.PS2);
                answer = reader.readLine().trim();
                if (!answer.equals("+") && !answer.equals("-")) throw new NotInDeclaredLimitsException();
                break;
            } catch (NoSuchElementException exception) {
                Outputer.printerror("Ответ не распознан!");
            } catch (NotInDeclaredLimitsException exception) {
                Outputer.printerror("Ответ должен быть представлен знаками '+' или '-'!");
            } catch (IllegalStateException exception) {
                Outputer.printerror("Непредвиденная ошибка!");
                System.exit(0);
            } catch (IOException e) {
                Outputer.printerror("Операция ввода была прервана!");
                System.exit(0);
            }
        }
        return answer.equals("+");
    }


}
