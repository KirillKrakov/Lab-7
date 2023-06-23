package server.utility;

import common.communication.User;
import common.exceptions.HistoryIsEmptyException;
import server.commands.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Operates the commands.
 */
public class CommandManager {
    private final int COMMAND_HISTORY_SIZE = 15;

    private String[] commandHistory = new String[COMMAND_HISTORY_SIZE];
    private List<Command> commands = new ArrayList<>();
    private Command helpCommand;
    private Command infoCommand;
    private Command showCommand;
    private Command addCommand;
    private Command updateCommand;
    private Command removeByIdCommand;
    private Command clearCommand;
    private Command exitCommand;
    private Command executeScriptCommand;
    private Command addIfMinCommand;
    private Command removeLowerCommand;
    private Command historyCommand;
    private Command removeAllByAuthorCommand;
    private Command countLessThanMinimalPointCommand;
    private Command filterContainsNameCommand;
    private Command loginCommand;
    private Command registerCommand;
    private Scanner scanner;
    private CollectionManager collectionManager;
    private ReentrantLock historylocker = new ReentrantLock();
    private ReentrantLock collectionLocker = new ReentrantLock();

    public CommandManager(CollectionManager collectionManager, DatabaseCollectionManager databaseCollectionManager, DatabaseUserManager databaseUserManager) {
        this.helpCommand = new HelpCommand();
        this.infoCommand = new InfoCommand(collectionManager);
        this.showCommand = new ShowCommand(collectionManager);
        this.addCommand = new AddCommand(collectionManager, databaseCollectionManager);
        this.updateCommand = new UpdateCommand(collectionManager, databaseCollectionManager);
        this.removeByIdCommand = new RemoveByIdCommand(collectionManager, databaseCollectionManager);
        this.clearCommand = new ClearCommand(collectionManager, databaseCollectionManager);
        this.exitCommand = new ExitCommand();
        this.executeScriptCommand = new ExecuteScriptCommand();
        this.addIfMinCommand = new AddIfMinCommand(collectionManager, databaseCollectionManager);
        this.removeLowerCommand = new RemoveLowerCommand(collectionManager, databaseCollectionManager);
        this.historyCommand = new HistoryCommand();
        this.removeAllByAuthorCommand = new RemoveAllByAuthorCommand(collectionManager, databaseCollectionManager);
        this.countLessThanMinimalPointCommand = new CountLessThanMinimalPointCommand(collectionManager);
        this.filterContainsNameCommand = new FilterContainsNameCommand(collectionManager);
        this.loginCommand = new LoginCommand(databaseUserManager);
        this.registerCommand = new RegisterCommand(databaseUserManager);

        this.collectionManager = collectionManager;
        this.scanner = new Scanner(System.in);

        commands.add(helpCommand);
        commands.add(infoCommand);
        commands.add(showCommand);
        commands.add(addCommand);
        commands.add(updateCommand);
        commands.add(removeByIdCommand);
        commands.add(clearCommand);
        commands.add(exitCommand);
        commands.add(executeScriptCommand);
        commands.add(addIfMinCommand);
        commands.add(removeLowerCommand);
        commands.add(historyCommand);
        commands.add(removeAllByAuthorCommand);
        commands.add(countLessThanMinimalPointCommand);
        commands.add(filterContainsNameCommand);
    }

    /**
     * @return List of manager's commands.
     */
    public List<Command> getCommands() {
        return commands;
    }

    /**
     * Adds command to command history.
     *
     * @param commandToStore Command to add.
     */
    public void addToHistory(String commandToStore, User user) {
        historylocker.lock();
        try {
            for (Command command : commands) {
                if (command.getName().equals(commandToStore)) {
                    for (int i = COMMAND_HISTORY_SIZE - 1; i > 0; i--) {
                        commandHistory[i] = commandHistory[i - 1];
                    }
                    commandHistory[0] = commandToStore + " (" + user.getUsername() + ")";
                }
            }
        }finally {
            historylocker.unlock();
        }
    }

    /**
     * Prints info about the all commands.
     *
     * @param stringArgument Its string argument.
     * @param objectArgument Its object argument.
     * @return Command exit status.
     */
    public boolean help(String stringArgument, Object objectArgument, User user) {
        if (helpCommand.execute(stringArgument, objectArgument, user)) {
            for (Command command : commands) {
                ResponseOutputer.appendtable(command.getName() + " " + command.getUsage(), command.getDescription());
            }
            return true;
        } else return false;
    }

    /**
     * Executes needed command.
     *
     * @param stringArgument Its string argument.
     * @param objectArgument Its object argument.
     * @return Command exit status.
     */
    public boolean info(String stringArgument, Object objectArgument, User user) {
        collectionLocker.lock();
        try {
            return infoCommand.execute(stringArgument, objectArgument, user);
        } finally {
            collectionLocker.unlock();
        }
    }

    /**
     * Executes needed command.
     *
     * @param stringArgument Its string argument.
     * @param objectArgument Its object argument.
     * @return Command exit status.
     */
    public boolean show(String stringArgument, Object objectArgument, User user) {
        collectionLocker.lock();
        try {
            return showCommand.execute(stringArgument, objectArgument, user);
        } finally {
            collectionLocker.unlock();
        }
    }

    /**
     * Executes needed command.
     *
     * @param stringArgument Its string argument.
     * @param objectArgument Its object argument.
     * @return Command exit status.
     */
    public boolean add(String stringArgument, Object objectArgument, User user) {
        collectionLocker.lock();
        try {
            return addCommand.execute(stringArgument, objectArgument, user);
        } finally {
            collectionLocker.unlock();
        }
    }

    /**
     * Executes needed command.
     *
     * @param stringArgument Its string argument.
     * @param objectArgument Its object argument.
     * @return Command exit status.
     */
    public boolean update(String stringArgument, Object objectArgument, User user) {
        collectionLocker.lock();
        try {
            return updateCommand.execute(stringArgument, objectArgument, user);
        } finally {
            collectionLocker.unlock();
        }
    }

    /**
     * Executes needed command.
     *
     * @param stringArgument Its string argument.
     * @param objectArgument Its object argument.
     * @return Command exit status.
     */
    public boolean removeById(String stringArgument, Object objectArgument, User user) {
        collectionLocker.lock();
        try {
            return removeByIdCommand.execute(stringArgument, objectArgument, user);
        } finally {
            collectionLocker.unlock();
        }
    }

    /**
     * Executes needed command.
     *
     * @param stringArgument Its string argument.
     * @param objectArgument Its object argument.
     * @return Command exit status.
     */
    public boolean clear(String stringArgument, Object objectArgument, User user) {
        collectionLocker.lock();
        try {
            return clearCommand.execute(stringArgument, objectArgument, user);
        } finally {
            collectionLocker.unlock();
        }
    }



    /**
     * Executes needed command.
     *
     * @param stringArgument Its string argument.
     * @param objectArgument Its object argument.
     * @return Command exit status.
     */
    public boolean exit(String stringArgument, Object objectArgument, User user) {
        return exitCommand.execute(stringArgument, objectArgument, user);
    }

    /**
     * Executes needed command.
     *
     * @param stringArgument Its string argument.
     * @param objectArgument Its object argument.
     * @return Command exit status.
     */
    public boolean executeScript(String stringArgument, Object objectArgument, User user) {
        return executeScriptCommand.execute(stringArgument, objectArgument, user);
    }

    /**
     * Executes needed command.
     *
     * @param stringArgument Its string argument.
     * @param objectArgument Its object argument.
     * @return Command exit status.
     */
    public boolean addIfMin(String stringArgument, Object objectArgument, User user) {
        collectionLocker.lock();
        try {
            return addIfMinCommand.execute(stringArgument, objectArgument, user);
        } finally {
            collectionLocker.unlock();
        }
    }

    /**
     * Executes needed command.
     *
     * @param stringArgument Its string argument.
     * @param objectArgument Its object argument.
     * @return Command exit status.
     */
    public boolean removeLower(String stringArgument, Object objectArgument, User user) {
        collectionLocker.lock();
        try {
            return removeLowerCommand.execute(stringArgument, objectArgument, user);
        } finally {
            collectionLocker.unlock();
        }
    }

    /**
     * Prints the history of used commands.
     *
     * @param stringArgument Its string argument.
     * @param objectArgument Its object argument.
     * @return Command exit status.
     */
    public boolean history(String stringArgument, Object objectArgument, User user) {
        if (historyCommand.execute(stringArgument, objectArgument, user)) {
            historylocker.lock();
            try {
                if (commandHistory.length == 0) throw new HistoryIsEmptyException();

                ResponseOutputer.appendln("Последние использованные команды:");
                for (String command : commandHistory) {
                    if (command != null) ResponseOutputer.appendln(" " + command);
                }
                return true;
            } catch (HistoryIsEmptyException exception) {
                ResponseOutputer.appendln("Ни одной команды еще не было использовано!");
            } finally {
                historylocker.unlock();
            }
        }
        return false;
    }

    /**
     * Executes needed command.
     *
     * @param stringArgument Its string argument.
     * @param objectArgument Its object argument.
     * @return Command exit status.
     */
    public boolean removeAllByAuthor(String stringArgument, Object objectArgument, User user) {
        collectionLocker.lock();
        try {
            return removeAllByAuthorCommand.execute(stringArgument, objectArgument, user);
        } finally {
            collectionLocker.unlock();
        }
    }

    /**
     * Executes needed command.
     *
     * @param stringArgument Its string argument.
     * @param objectArgument Its object argument.
     * @return Command exit status.
     */
    public boolean countLessThanMinimalPoint(String stringArgument, Object objectArgument, User user) {
        collectionLocker.lock();
        try {
            return countLessThanMinimalPointCommand.execute(stringArgument, objectArgument, user);
        } finally {
            collectionLocker.unlock();
        }
    }

    /**
     * Executes needed command.
     *
     * @param stringArgument Its string argument.
     * @param objectArgument Its object argument.
     * @return Command exit status.
     */
    public boolean filterContainsName(String stringArgument, Object objectArgument, User user) {
        collectionLocker.lock();
        try {
            return filterContainsNameCommand.execute(stringArgument, objectArgument, user);
        } finally {
            collectionLocker.unlock();
        }
    }

    public boolean login(String stringArgument, Object objectArgument, User user) {
        return loginCommand.execute(stringArgument, objectArgument, user);
    }
    public boolean register(String stringArgument, Object objectArgument, User user) {
        return registerCommand.execute(stringArgument, objectArgument, user);
    }
}
