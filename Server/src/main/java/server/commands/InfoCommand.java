package server.commands;

import common.communication.User;
import common.exceptions.WrongAmountOfElementsException;
import server.utility.CollectionManager;
import server.utility.ResponseOutputer;

import java.time.LocalDateTime;

/**
 * Command 'info'. Prints information about the collection.xml.
 */
public class InfoCommand extends AbstractCommand {
    private CollectionManager collectionManager;

    public InfoCommand(CollectionManager collectionManager) {
        super("info", "", "вывести информацию о коллекции");
        this.collectionManager = collectionManager;
    }

    /**
     * Executes the command.
     *
     * @return Command exit status.
     */
    @Override
    public boolean execute(String stringArgument, Object objectArgument, User user) {
        try {
            if (!stringArgument.isEmpty() || objectArgument != null) throw new WrongAmountOfElementsException();
            LocalDateTime lastInitTime = collectionManager.getlastInitTime();
            String lastInitTimeString = (lastInitTime == null) ? "инициализации еще не происходило" :
                    lastInitTime.toString();

            ResponseOutputer.appendln("Информация о коллекции:" +
                    "\nТип: " + collectionManager.collectionType() +
                    "\nДата инициализации: " + lastInitTimeString +
                    "\nКоличество элементов: " + collectionManager.collectionSize());
            return true;
        } catch (WrongAmountOfElementsException exception) {
            ResponseOutputer.appenderror("Использование: '" + getName() + " " + getUsage() + "'");
        }
        return false;
    }
}
