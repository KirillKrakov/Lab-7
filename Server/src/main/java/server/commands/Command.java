package server.commands;

import common.communication.User;

/**
 * Общий интерфейс для всех команд
 */
public interface Command {
    String getName();

    String getUsage();

    String getDescription();

    boolean execute(String commandStringArgument, Object commandObjectArgument, User user);
}
