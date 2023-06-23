package server.utility;

import common.data.LabWork;
import common.data.Person;
import common.exceptions.DatabaseHandlingException;
import common.utility.Outputer;
import server.ServerApp;

import java.time.LocalDateTime;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Класс, управляющий самой коллекцией
 */
public class CollectionManager {
    private TreeSet<LabWork> labWorksCollection;
    private LocalDateTime lastInitTime;
    private DatabaseCollectionManager databaseCollectionManager;

    public CollectionManager(DatabaseCollectionManager databaseCollectionManager){
        this.databaseCollectionManager = databaseCollectionManager;

        loadCollection();
    }

    /**
     * Метод возвращает коллекцию, с которой работает пользователь
     * @return labWorksCollection
     */
    public TreeSet<LabWork> getCollection() {
        return labWorksCollection;
    }

    /**
     * Метод задаёт коллекцию, с которой будет работать пользователь
     * @param collection
     */
    public void setCollection(TreeSet<LabWork> collection) {
        this.labWorksCollection = collection;
    }

    /**
     * Метод возвращает дату и время инициализации коллекции
     * @return lastInitialisationTime
     */
    public LocalDateTime getlastInitTime() {
        return lastInitTime;
    }

    /**
     * Метод возвращает тип коллекции
     * @return collectionType
     */
    public String collectionType(){
        return labWorksCollection.getClass().getName();
    }

    /**
     * Метод возвращает размер коллекции (количество элементов в нём)
     * @return collectionSize
     */
    public int collectionSize(){
        return labWorksCollection.size();
    }

    /**
     * Метод возвращает первый элемент в коллекции
     * @return first
     */
    public LabWork getFirst(){
        return labWorksCollection.stream().findFirst().orElse(null);
    }

    /**
     * Возвращает элемент коллекции с таким же ID
     * @param id
     * @return labWork
     */
    public LabWork getSameId(int id){
        return labWorksCollection.stream().filter(labWork -> (labWork.getId() ==id)).findFirst().orElse(null);
    }

    /**
     * @param labWorkToFind A marine who's value will be found.
     * @return A marine by his value or null if marine isn't found.
     */
    public LabWork getByValue(LabWork labWorkToFind) {
        return labWorksCollection.stream().filter(labWork -> labWork.equals(labWorkToFind)).findFirst().orElse(null);
    }

    /**
     * @return Collection content or corresponding string if collection is empty.
     */
    public String showCollection() {
        if (labWorksCollection.isEmpty()) return "Коллекция пуста!";
        return labWorksCollection.stream().reduce("", (sum, m) -> sum += m + "\n\n", (sum1, sum2) -> sum1 + sum2).trim();
    }

    /**
     * Метод добавляет элемент в коллекцию
     * @param labWork
     */
    public void addToCollection(LabWork labWork) {
        labWorksCollection.add(labWork);
    }

    /**
     * Метод удаляет элемент из коллекции
     * @param labWork
     */
    public void removeFromCollection(LabWork labWork) {
        labWorksCollection.remove(labWork);
    }

    /**
     * Remove marines lower than the selected one.
     *
     * @param labWorkToCompare A marine to compare with.
     * @return Greater marines list.
     */
    public NavigableSet<LabWork> getLower(LabWork labWorkToCompare) {
        return labWorksCollection.stream().filter(marine -> marine.compareTo(labWorkToCompare) < 0).collect(
                TreeSet::new,
                TreeSet::add,
                TreeSet::addAll
        );
    }

    /**
     * Метод удаляет из коллекции все элементы, меньшие заданного
     * @param comparableLabWork
     */
    public void removeLower(LabWork comparableLabWork){
        labWorksCollection.removeIf(labWork -> labWork.compareTo(comparableLabWork) < 0);
    }

    /**
     * Метод очищает коллекцию (удаляет все элементы)
     */
    public void clearCollection(){
        labWorksCollection.clear();
    }

    /**
     * Метод генерирует значение ID для нового элемента в коллекции
     * @return lastID
     */
    public int generateNextId() {
        if (labWorksCollection.isEmpty()) {
            return 1;
        } else {
            int id = 0;
            for (LabWork labWork : labWorksCollection) {
                if (labWork.getId() > id) {
                    id = labWork.getId();
                }
            }
            return id + 1;
        }
    }

    /**
     * Метод удаляет из коллекции все элементы, имеющие того же автора
     * @param author
     */
    public void removeAllByAuthor(Person author) {
        labWorksCollection.removeIf(labWork -> labWork.getAuthor().equals(author));
    }
    public NavigableSet<LabWork> getAllByAuthor(Person author) {
        //return (NavigableSet<LabWork>) labWorksCollection.stream().filter(labWork -> labWork.getAuthor().equals(author)).collect(Collectors.toSet());
        NavigableSet<LabWork> sameLabWorks = new TreeSet<>();
        for (LabWork labWork : labWorksCollection) {
            if (labWork.getAuthor().equals(author)) {
                sameLabWorks.add(labWork);
            }
        }
        return sameLabWorks;
    }
    /**
     * Метод возвращает количество элементов, у которых минимальный балл меньше заданного
     * @param point
     * @return
     */
    public int countLessThanMinimalPoint(int point) {
        return (int) labWorksCollection.stream().filter(labWork -> labWork.getMinimalPoint() < point).count();
    }

    public String filterContainsName(String name) {
        return labWorksCollection.stream().filter(labWork -> labWork.getName().contains(name))
                .reduce("", (sum, m) -> sum += m + "\n\n", (sum1, sum2) -> sum1 + sum2).trim();
    }

    /**
     * Loads the collection from file.
     */
    private void loadCollection() {
        try {
            labWorksCollection = databaseCollectionManager.getCollection();
            lastInitTime = LocalDateTime.now();
            Outputer.println("Коллекция загружена");
            ServerApp.logger.info("Коллекция загружена.");
        } catch (DatabaseHandlingException exception) {
            labWorksCollection = new TreeSet<>();
            Outputer.printerror("Коллекция не может быть загружена!");
            ServerApp.logger.error("Коллекция не может быть загружена!");
        }
    }

    /**
     * Метод выводить коллекцию в строковом формате
     * @return CollectionToString
     */
    @Override
    public String toString() {
        if (labWorksCollection.isEmpty()) return "Колекция пуста!";
        String str = "Все элементы коллекции:";
        int x = 0;
        for (LabWork labWork : labWorksCollection) {
            str = str + "\n\n" + labWork;
            x++;
            if (x >= 12) {
                break;
            }
        }
        return str;
    }
}
