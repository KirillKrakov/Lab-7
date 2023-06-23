package server.commands;

import common.communication.LabWorkForRequest;
import common.communication.User;
import common.exceptions.DatabaseHandlingException;
import common.exceptions.WrongCommandArgumentException;
import server.utility.CollectionManager;
import server.utility.DatabaseCollectionManager;
import server.utility.ResponseOutputer;

/**
 * Класс, представляющий команду add, которая добавляет новый элемент в коллекцию
 */
public class AddCommand extends AbstractCommand{
    private CollectionManager collectionManager;
    private DatabaseCollectionManager databaseCollectionManager;

    public AddCommand(CollectionManager collectionManager, DatabaseCollectionManager databaseCollectionManager) {
        super("add", "{element}","добавить новый элемент в коллекцию");
        this.collectionManager = collectionManager;
        this.databaseCollectionManager = databaseCollectionManager;
    }

    /**
     * Метод возвращает используемый командой менеджер коллекций
     * @return collectionManager
     */
    public CollectionManager getCollectionManager() {
        return collectionManager;
    }


    /**
     * Метод добавляет новый элемент в коллекцию
     * @param stringArgument,objectArgument
     */
    @Override
    public boolean execute(String stringArgument, Object objectArgument, User user) {
        try {
            if (!stringArgument.isEmpty() || objectArgument == null)  throw new WrongCommandArgumentException();
            LabWorkForRequest labWorkForRequest = (LabWorkForRequest) objectArgument;
            collectionManager.addToCollection(databaseCollectionManager.insertLabwork(labWorkForRequest,user));
            ResponseOutputer.appendln("Элемент добавлен в коллекцию");
            return true;
        } catch (WrongCommandArgumentException exception) {
            ResponseOutputer.appenderror("Аргумент этой команды должен быть пустым!");
        } catch (ClassCastException exception) {
            ResponseOutputer.appenderror("Переданный клиентом объект неверен!");
        } catch (DatabaseHandlingException exception) {
            System.out.println("(((((");
            ResponseOutputer.appenderror("произошла ошибка при обращении к базе данных");
        }
        return false;
    }
}
