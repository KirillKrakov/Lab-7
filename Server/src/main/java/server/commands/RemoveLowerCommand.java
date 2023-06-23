package server.commands;

import common.communication.LabWorkForRequest;
import common.communication.User;
import common.data.LabWork;
import common.exceptions.*;
import server.utility.CollectionManager;
import server.utility.DatabaseCollectionManager;
import server.utility.ResponseOutputer;

import java.time.LocalDateTime;

/**
 * Класс, представляющий команду remove_lower, удаляющую из коллекции все элементы, меньшие, чем заданный
 */
public class RemoveLowerCommand extends AbstractCommand {
    private CollectionManager collectionManager;
    private DatabaseCollectionManager databaseCollectionManager;

    public RemoveLowerCommand(CollectionManager collectionManager, DatabaseCollectionManager databaseCollectionManager) {
        super("remove_greater", "<element>", "удалить из коллекции все элементы, превышающие заданный");
        this.collectionManager = collectionManager;
        this.databaseCollectionManager = databaseCollectionManager;
    }

    /**
     * Метод удаляет из коллекции все элементы, меньшие, чем заданный
     * @param stringArgument
     */
    @Override
    public boolean execute(String stringArgument, Object objectArgument, User user) {
        try {
            if (!stringArgument.isEmpty() || objectArgument == null) throw new WrongAmountOfElementsException();
            if (collectionManager.collectionSize() == 0) throw new CollectionIsEmptyException();
            LabWorkForRequest labWorkForRequest = (LabWorkForRequest) objectArgument;
            LabWork comparableLabWork = new LabWork(
                    collectionManager.generateNextId(),
                    labWorkForRequest.getName(),
                    labWorkForRequest.getCoordinates(),
                    LocalDateTime.now(),
                    labWorkForRequest.getMinimalPoint(),
                    labWorkForRequest.getPersonalQualitiesMinimum(),
                    labWorkForRequest.getAveragePoint(),
                    labWorkForRequest.getDifficulty(),
                    labWorkForRequest.getAuthor(),
                    user
            );
            for (LabWork labWork : collectionManager.getLower(comparableLabWork)) {
                if (!labWork.getOwner().equals(user)) throw new PermissionDeniedException();
                if (!databaseCollectionManager.checkLabWorkUserId(labWork.getId(),user)) throw new ManualDatabaseEditException();
            }
            for (LabWork labWork : collectionManager.getLower(comparableLabWork)) {
               databaseCollectionManager.deleteLabworkById(labWork.getId());
               collectionManager.removeFromCollection(labWork);
            }
            ResponseOutputer.appendln("Элементы, меньшие заданного, успешно удалены!");
            return true;
        } catch (WrongAmountOfElementsException exception) {
            ResponseOutputer.appendln("Аргумент этой команды должен быть пустым!");
        } catch (CollectionIsEmptyException exception) {
            ResponseOutputer.appenderror("Коллекция пуста!");
        } catch (ClassCastException exception) {
            ResponseOutputer.appenderror("Переданный клиентом объект неверен!");
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