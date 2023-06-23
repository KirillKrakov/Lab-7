package server.commands;

import common.communication.User;
import common.data.LabWork;
import common.exceptions.*;
import server.utility.CollectionManager;
import server.utility.DatabaseCollectionManager;
import server.utility.ResponseOutputer;

/**
 * Класс, представляющий команду remove_by_id, удаляющую элемент из коллекции по его id
 */
public class RemoveByIdCommand extends AbstractCommand {
    private CollectionManager collectionManager;
    private DatabaseCollectionManager databaseCollectionManager;

    public RemoveByIdCommand(CollectionManager collectionManager, DatabaseCollectionManager databaseCollectionManager) {
        super("remove_by_id", "<ID>", "удалить элемент из коллекции по его id");
        this.collectionManager = collectionManager;
        this.databaseCollectionManager = databaseCollectionManager;
    }

    /**
     * Метод удаляет элемент из коллекции по его id
     * @param stringArgument - заданное значение id
     */
    @Override
    public boolean execute(String stringArgument, Object objectArgument, User user) {
        try {
            if (stringArgument.isEmpty() || objectArgument != null) throw new WrongAmountOfElementsException();
            if (collectionManager.collectionSize() == 0) throw new CollectionIsEmptyException();
            int id = Integer.parseInt(stringArgument);
            LabWork removingLabWork = collectionManager.getSameId(id);
            if (removingLabWork == null) throw new LabWorkIsNotFoundException();
            if (!removingLabWork.getOwner().equals(user)) throw new PermissionDeniedException();
            if (!databaseCollectionManager.checkLabWorkUserId(removingLabWork.getId(), user)) throw new ManualDatabaseEditException();
            databaseCollectionManager.deleteLabworkById(id);
            collectionManager.removeFromCollection(removingLabWork);
            ResponseOutputer.appendln("Элемент успешно удален из коллекции!");
            return true;
        } catch (WrongAmountOfElementsException exception) {
            ResponseOutputer.appendln("В аргументе этой команды должен быть указан ID!");
        } catch (CollectionIsEmptyException exception) {
            ResponseOutputer.appenderror("В этой коллекции нет элементов!");
        } catch (NumberFormatException exception) {
            ResponseOutputer.appenderror("В аргументе команды должно быть указано число формата Integer!");
        } catch (LabWorkIsNotFoundException exception) {
            ResponseOutputer.appenderror("Элемент с таким ID не найден!");
        } catch (DatabaseHandlingException exception) {
            ResponseOutputer.appenderror("Произошла ошибка при обращении к базе данных!");
        } catch (PermissionDeniedException exception) {
            ResponseOutputer.appenderror("Недостаточно прав для выполнения данной команды!");
            ResponseOutputer.appendln("Принадлежащие другим пользователям объекты доступны только для чтения.");
        } catch (ManualDatabaseEditException exception) {
            ResponseOutputer.appenderror("Произошло прямое изменение базы данных!");
            ResponseOutputer.appendln("Перезапустите клиент для избежания возможных ошибок.");
        }
        return false;
    }
}
