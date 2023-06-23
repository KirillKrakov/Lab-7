package server.commands;

import common.communication.LabWorkForRequest;
import common.communication.User;
import common.data.LabWork;
import common.exceptions.DatabaseHandlingException;
import common.exceptions.WrongCommandArgumentException;
import server.utility.CollectionManager;
import server.utility.DatabaseCollectionManager;
import server.utility.ResponseOutputer;

/**
 * Класс, представляющий команду add_if_min, добавляющую новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции
 */
public class AddIfMinCommand extends AbstractCommand{
    private CollectionManager collectionManager;
    private DatabaseCollectionManager databaseCollectionManager;

    public AddIfMinCommand(CollectionManager collectionManager, DatabaseCollectionManager databaseCollectionManager) {
        super("add_if_min", "{element}", "добавить новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции");
        this.collectionManager = collectionManager;
        this.databaseCollectionManager = databaseCollectionManager;
    }

    /**
     * Метод добавляет новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции
     * @param argument
     */
    @Override
    public boolean execute(String argument, Object objectArgument, User user) {
        try {
            if (!argument.isEmpty() || objectArgument == null) throw new WrongCommandArgumentException();
            LabWorkForRequest labWorkForRequest = (LabWorkForRequest) objectArgument;
            LabWork addingLabWork = databaseCollectionManager.insertLabwork(labWorkForRequest,user);
            if (collectionManager.collectionSize() == 0 || addingLabWork.compareTo(collectionManager.getFirst()) < 0) {
                collectionManager.addToCollection(addingLabWork);
                ResponseOutputer.appendln("Элемент добавлен в коллекцию");
            } else {
                ResponseOutputer.appendln("Значение элемента не является наименьшим среди элементов коллекции");
            }
            return true;
        } catch (WrongCommandArgumentException ex) {
            ResponseOutputer.appenderror("Аргумент этой команды должен быть пустым!");
        } catch (DatabaseHandlingException exception) {
            ResponseOutputer.appenderror("Произошла ошибка при обращении к базе данных!");
        }
        return false;
    }
}
