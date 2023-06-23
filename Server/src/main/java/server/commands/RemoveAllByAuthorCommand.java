package server.commands;

import common.communication.AuthorForRequest;
import common.communication.User;
import common.data.LabWork;
import common.data.Person;
import common.exceptions.*;
import server.utility.CollectionManager;
import server.utility.DatabaseCollectionManager;
import server.utility.ResponseOutputer;

import java.util.NavigableSet;


/**
 * Класс, представляет команду remove_all_by_author, которая удаляет из коллекции все элементы, значение поля author которого эквивалентно заданному
 */
public class RemoveAllByAuthorCommand extends AbstractCommand{
    private CollectionManager collectionManager;
    private DatabaseCollectionManager databaseCollectionManager;
    public RemoveAllByAuthorCommand(CollectionManager collectionManager, DatabaseCollectionManager databaseCollectionManager) {
        super("remove_all_by_author", "<author>", "удалить из коллекции все элементы, значение поля author которого эквивалентно заданному");
        this.collectionManager = collectionManager;
        this.databaseCollectionManager = databaseCollectionManager;
    }

    /**
     * Метод удаляет из коллекции все элементы, значение поля author которого эквивалентно заданному
     * @param stringArgument, objectArgument, user
     */
    @Override
    public boolean execute(String stringArgument, Object objectArgument, User user) {
        try {
            if(!stringArgument.isEmpty() || objectArgument == null) throw new WrongCommandArgumentException();
            if(collectionManager.collectionSize() == 0) throw new CollectionIsEmptyException();
            AuthorForRequest authorForRequest = (AuthorForRequest) objectArgument;
            Person checkingAuthor = new Person(
                    authorForRequest.getName(),
                    authorForRequest.getBirthday(),
                    authorForRequest.getPassportID()
            );
            NavigableSet<LabWork> labWorksToRemove = collectionManager.getAllByAuthor(checkingAuthor);
            for (LabWork labWorkToRemove : labWorksToRemove) {
                if (!labWorkToRemove.getOwner().equals(user)) throw new PermissionDeniedException();
                if (!databaseCollectionManager.checkLabWorkUserId(labWorkToRemove.getId(), user)) throw new ManualDatabaseEditException();
                databaseCollectionManager.deleteLabworkById(labWorkToRemove.getId());
            }
            collectionManager.removeAllByAuthor(checkingAuthor);
            ResponseOutputer.appendln("Элементы, имеющие такого же автора, успешно удалены!");
            return true;
        } catch (WrongCommandArgumentException ex) {
            ResponseOutputer.appenderror("Аргумент этой команды должен быть пустым!");
        } catch (CollectionIsEmptyException ex) {
            ResponseOutputer.appenderror("В коллекции нет элементов!");
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
