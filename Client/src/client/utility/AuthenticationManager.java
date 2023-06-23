package client.utility;

import common.communication.Request;
import common.communication.User;

import java.io.BufferedReader;
import java.util.Scanner;


public class AuthenticationManager {
    private final String loginCommand = "login";
    private final String registerCommand = "register";

    private BufferedReader reader;

    public AuthenticationManager(BufferedReader reader) {
        this.reader = reader;
    }

    /**
     * Handle user authentication.
     *
     * @return Request of user.
     */
    public Request handle() {
        AuthAsker authAsker = new AuthAsker(reader);
        String command = authAsker.askQuestion("У вас уже есть учетная запись?") ? loginCommand : registerCommand;
        User user = new User(authAsker.askLogin(), authAsker.askPassword());
        return new Request(command, "", user);
    }
}
