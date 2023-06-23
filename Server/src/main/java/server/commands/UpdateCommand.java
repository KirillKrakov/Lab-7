package server.commands;

import common.communication.LabWorkForRequest;
import common.communication.User;
import common.data.Coordinates;
import common.data.Difficulty;
import common.data.LabWork;
import common.data.Person;
import common.exceptions.*;
import server.utility.CollectionManager;
import server.utility.DatabaseCollectionManager;
import server.utility.ResponseOutputer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Класс, представляющий команду update id, обновляющую значение элемента коллекции, id которого равен заданному
 */
public class UpdateCommand extends AbstractCommand {
    private CollectionManager collectionManager;
    private DatabaseCollectionManager databaseCollectionManager;

    public UpdateCommand(CollectionManager collectionManager, DatabaseCollectionManager databaseCollectionManager) {
        super("update", "<ID> {element}", "обновить значение элемента коллекции по ID");
        this.collectionManager = collectionManager;
        this.databaseCollectionManager = databaseCollectionManager;
    }

    /**
     * Метод обновляет значение элемента коллекции, id которого равен заданному
     * @param stringArgument - заданное значение id
     */
    @Override
    public boolean execute(String stringArgument, Object objectArgument, User user) {
        try {
            if (stringArgument.isEmpty() || objectArgument == null) throw new WrongAmountOfElementsException();
            if (collectionManager.collectionSize() == 0) throw new CollectionIsEmptyException();
            int id = Integer.parseInt(stringArgument);
            if (id <= 0) throw new NumberFormatException();
            LabWork oldLabWork = collectionManager.getSameId(id);
            if (oldLabWork == null) throw new LabWorkIsNotFoundException();
            if (!oldLabWork.getOwner().equals(user)) throw new PermissionDeniedException();
            if (!databaseCollectionManager.checkLabWorkUserId(oldLabWork.getId(), user)) throw new ManualDatabaseEditException();

            LabWorkForRequest labWorkForRequest = (LabWorkForRequest) objectArgument;

            databaseCollectionManager.updateLabWorkById(id, labWorkForRequest);

            String name = labWorkForRequest.getName().equals("null") ? oldLabWork.getName() : labWorkForRequest.getName();
            Coordinates coordinates = labWorkForRequest.getCoordinates() == null ? oldLabWork.getCoordinates() : labWorkForRequest.getCoordinates();
            LocalDateTime creationDate = oldLabWork.getCreationDate();
            int minimalPoint = labWorkForRequest.getMinimalPoint() == -1 ? oldLabWork.getMinimalPoint() : labWorkForRequest.getMinimalPoint();
            Float personalQualitiesMinimum = labWorkForRequest.getPersonalQualitiesMinimum() == -1 ? oldLabWork.getPersonalQualitiesMinimum() : labWorkForRequest.getPersonalQualitiesMinimum();
            long averagePoint = labWorkForRequest.getAveragePoint() == -1 ? oldLabWork.getAveragePoint() : labWorkForRequest.getAveragePoint();
            Difficulty difficulty = labWorkForRequest.getDifficulty() == null ? oldLabWork.getDifficulty() : labWorkForRequest.getDifficulty();
            Person author = labWorkForRequest.getAuthor() == null ? oldLabWork.getAuthor() : labWorkForRequest.getAuthor();

            collectionManager.removeFromCollection(oldLabWork);
            collectionManager.addToCollection(new LabWork(
                    id,
                    name,
                    coordinates,
                    creationDate,
                    minimalPoint,
                    personalQualitiesMinimum,
                    averagePoint,
                    difficulty,
                    author,
                    user
            ));
            ResponseOutputer.appendln("Элемент коллекции успешно обновлён!");
            return true;
        } catch (WrongAmountOfElementsException exception) {
            ResponseOutputer.appendln("В аргументе этой команды должен быть указан ID!");
        } catch (CollectionIsEmptyException exception) {
            ResponseOutputer.appenderror("В этой коллекции нет элементов!");
        } catch (NumberFormatException exception) {
            ResponseOutputer.appenderror("ID должен быть представлен положительным числом!");
        } catch (LabWorkIsNotFoundException exception) {
            ResponseOutputer.appenderror("Элемент с таким ID в коллекции нет!");
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
