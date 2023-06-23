package server.commands;

import common.communication.User;
import common.data.LabWork;
import common.exceptions.DatabaseHandlingException;
import common.exceptions.ManualDatabaseEditException;
import common.exceptions.PermissionDeniedException;
import common.exceptions.WrongAmountOfElementsException;
import server.utility.CollectionManager;
import server.utility.DatabaseCollectionManager;
import server.utility.ResponseOutputer;

import java.util.TreeSet;

/**
 * Класс, представляющий команду clear, очищяющую коллекцию
 */
public class ClearCommand extends AbstractCommand {
    private CollectionManager collectionManager;
    private DatabaseCollectionManager databaseCollectionManager;

    public ClearCommand(CollectionManager collectionManager, DatabaseCollectionManager databaseCollectionManager) {
        super("clear", "", "очистить коллекцию");
        this.collectionManager = collectionManager;
        this.databaseCollectionManager = databaseCollectionManager;
    }

    /**
     * Метод очищает коллекцию, удаляя из неё все элементы
     * @param stringArgument,objectArgument
     */
    @Override
    public boolean execute(String stringArgument, Object objectArgument, User user) {
        try {
            if (!stringArgument.isEmpty() || objectArgument != null) throw new WrongAmountOfElementsException();
            TreeSet<LabWork> labWorks = collectionManager.getCollection();
            TreeSet<LabWork> clearing = new TreeSet<>();
            for (LabWork labWork : labWorks) {
                if (!labWork.getOwner().equals(user)) {
                    ResponseOutputer.appenderror("Элемент №" + labWork.getId() + " придалежит другому пользователю. Этот объект доступен только для чтения!");
                } else {
                    clearing.add(labWork);
                    databaseCollectionManager.deleteLabworkById(labWork.getId());
                }
            }
            for (LabWork clearingLabWork : clearing) {
                collectionManager.removeFromCollection(clearingLabWork);
            }
            ResponseOutputer.appendln("Коллекция очищена!");
            return true;
        } catch (WrongAmountOfElementsException exception) {
            ResponseOutputer.appendln("Аргумент этой команды должен быть пустым!");
        } catch (DatabaseHandlingException exception) {
            ResponseOutputer.appenderror("Произошла ошибка при обращении к базе данных!");
        }
        return false;
    }
}