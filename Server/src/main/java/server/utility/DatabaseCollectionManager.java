package server.utility;

import common.communication.LabWorkForRequest;
import common.communication.User;
import common.data.Coordinates;
import common.data.Difficulty;
import common.data.LabWork;
import common.data.Person;
import common.exceptions.DatabaseHandlingException;
import common.utility.Outputer;
import server.ServerApp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.NavigableSet;
import java.util.TreeSet;

public class DatabaseCollectionManager {
    //LABWORK_TABLE
    private final String SELECT_ALL_LABWORKS = "SELECT * FROM " + DatabaseHandler.LABWORK_TABLE;
    private final String SELECT_LABWORK_BY_ID = SELECT_ALL_LABWORKS + " WHERE " +
            DatabaseHandler.LABWORK_TABLE_ID_COLUMN + " = ?";
    private final String SELECT_LABWORK_BY_ID_AND_USER_ID = SELECT_LABWORK_BY_ID + " AND " +
            DatabaseHandler.LABWORK_TABLE_USER_ID_COLUMN + " = ?";
    private final String INSERT_LABWORK = "INSERT INTO " +
            DatabaseHandler.LABWORK_TABLE + " (" +
            DatabaseHandler.LABWORK_TABLE_ID_COLUMN + ", " +
            DatabaseHandler.LABWORK_TABLE_NAME_COLUMN + ", " +
            DatabaseHandler.LABWORK_TABLE_CREATION_DATE_COLUMN + ", " +
            DatabaseHandler.LABWORK_TABLE_MINIMAL_POINT_COLUMN + ", " +
            DatabaseHandler.LABWORK_TABLE_PERSONAL_QUALITIES_MINIMUM_COLUMN + ", " +
            DatabaseHandler.LABWORK_TABLE_AVERAGE_POINT_COLUMN + ", " +
            DatabaseHandler.LABWORK_TABLE_DIFFICULTY_COLUMN + ", " +
            DatabaseHandler.LABWORK_TABLE_AUTHOR_ID_COLUMN + ", " +
            DatabaseHandler.LABWORK_TABLE_USER_ID_COLUMN + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private final String DELETE_LABWORK_BY_ID = "DELETE FROM " + DatabaseHandler.LABWORK_TABLE +
            " WHERE " + DatabaseHandler.LABWORK_TABLE_ID_COLUMN + " = ?";
    private final String UPDATE_LABWORK_NAME_BY_ID = "UPDATE " + DatabaseHandler.LABWORK_TABLE + " SET " +
            DatabaseHandler.LABWORK_TABLE_NAME_COLUMN + " = ?" + " WHERE " +
            DatabaseHandler.LABWORK_TABLE_ID_COLUMN + " = ?";
    private final String UPDATE_LABWORK_MINIMAL_POINT_BY_ID = "UPDATE " + DatabaseHandler.LABWORK_TABLE + " SET " +
            DatabaseHandler.LABWORK_TABLE_MINIMAL_POINT_COLUMN + " = ?" + " WHERE " +
            DatabaseHandler.LABWORK_TABLE_ID_COLUMN + " = ?";
    private final String UPDATE_LABWORK_PERSONAL_QUALITIES_MINIMUM_BY_ID = "UPDATE " + DatabaseHandler.LABWORK_TABLE + " SET " +
            DatabaseHandler.LABWORK_TABLE_PERSONAL_QUALITIES_MINIMUM_COLUMN + " = ?" + " WHERE " +
            DatabaseHandler.LABWORK_TABLE_ID_COLUMN + " = ?";
    private final String UPDATE_LABWORK_AVERAGE_POINT_BY_ID = "UPDATE " + DatabaseHandler.LABWORK_TABLE + " SET " +
            DatabaseHandler.LABWORK_TABLE_AVERAGE_POINT_COLUMN + " = ?" + " WHERE " +
            DatabaseHandler.LABWORK_TABLE_ID_COLUMN + " = ?";
    private final String UPDATE_LABWORK_DIFFICULTY_BY_ID = "UPDATE " + DatabaseHandler.LABWORK_TABLE + " SET " +
            DatabaseHandler.LABWORK_TABLE_DIFFICULTY_COLUMN + " = ?" + " WHERE " +
            DatabaseHandler.LABWORK_TABLE_ID_COLUMN + " = ?";
    // COORDINATES_TABLE
    private final String SELECT_ALL_COORDINATES = "SELECT * FROM " + DatabaseHandler.COORDINATES_TABLE;
    private final String SELECT_COORDINATES_BY_LABWORK_ID = SELECT_ALL_COORDINATES +
            " WHERE " + DatabaseHandler.COORDINATES_TABLE_LABWORK_ID_COLUMN + " = ?";
    private final String INSERT_COORDINATES = "INSERT INTO " +
            DatabaseHandler.COORDINATES_TABLE + " (" +
            DatabaseHandler.COORDINATES_TABLE_LABWORK_ID_COLUMN + ", " +
            DatabaseHandler.COORDINATES_TABLE_X_COLUMN + ", " +
            DatabaseHandler.COORDINATES_TABLE_Y_COLUMN + ") VALUES (?, ?, ?)";
    private final String UPDATE_COORDINATES_BY_LABWORK_ID = "UPDATE " + DatabaseHandler.COORDINATES_TABLE + " SET " +
            DatabaseHandler.COORDINATES_TABLE_X_COLUMN + " = ?, " +
            DatabaseHandler.COORDINATES_TABLE_Y_COLUMN + " = ?" + " WHERE " +
            DatabaseHandler.COORDINATES_TABLE_LABWORK_ID_COLUMN + " = ?";
    private final String DELETE_COORDINATES_BY_LABWORK_ID = "DELETE FROM " + DatabaseHandler.COORDINATES_TABLE +
            " WHERE " + DatabaseHandler.COORDINATES_TABLE_LABWORK_ID_COLUMN + " = ?";
    // AUTHOR_TABLE
    private final String SELECT_ALL_AUTHORS = "SELECT * FROM " + DatabaseHandler.AUTHOR_TABLE;
    private final String SELECT_AUTHOR_BY_ID = SELECT_ALL_AUTHORS +
            " WHERE " + DatabaseHandler.AUTHOR_TABLE_ID_COLUMN + " = ?";
    private final String INSERT_AUTHOR = "INSERT INTO " +
            DatabaseHandler.AUTHOR_TABLE + " (" +
            DatabaseHandler.AUTHOR_TABLE_NAME_COLUMN + ", " +
            DatabaseHandler.AUTHOR_TABLE_BIRTHDAY_COLUMN + ", " +
            DatabaseHandler.AUTHOR_TABLE_PASSPORT_ID_COLUMN + ") VALUES (?, ?, ?)";
    private final String UPDATE_AUTHOR_BY_ID = "UPDATE " + DatabaseHandler.AUTHOR_TABLE + " SET " +
            DatabaseHandler.AUTHOR_TABLE_NAME_COLUMN + " = ?, " +
            DatabaseHandler.AUTHOR_TABLE_NAME_COLUMN + " = ?, " +
            DatabaseHandler.AUTHOR_TABLE_PASSPORT_ID_COLUMN + " = ?" + " WHERE " +
            DatabaseHandler.AUTHOR_TABLE_ID_COLUMN + " = ?";
    private final String DELETE_AUTHOR_BY_ID = "DELETE FROM " + DatabaseHandler.AUTHOR_TABLE +
            " WHERE " + DatabaseHandler.AUTHOR_TABLE_ID_COLUMN + " = ?";
    private DatabaseHandler databaseHandler;
    private DatabaseUserManager databaseUserManager;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DatabaseCollectionManager(DatabaseHandler databaseHandler, DatabaseUserManager databaseUserManager) {
        this.databaseHandler = databaseHandler;
        this.databaseUserManager = databaseUserManager;
    }

    private LabWork createLabWork(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt(DatabaseHandler.LABWORK_TABLE_ID_COLUMN);
        String name = resultSet.getString(DatabaseHandler.LABWORK_TABLE_NAME_COLUMN);
        Coordinates coordinates = getCoordinatesByLabWorkId(id);
        LocalDateTime creationDate = resultSet.getTimestamp(DatabaseHandler.LABWORK_TABLE_CREATION_DATE_COLUMN).toLocalDateTime();
        int minimalPoint = resultSet.getInt(DatabaseHandler.LABWORK_TABLE_MINIMAL_POINT_COLUMN);
        Float personalQualitiesMinimum = resultSet.getFloat(DatabaseHandler.LABWORK_TABLE_PERSONAL_QUALITIES_MINIMUM_COLUMN);
        long averagePoint = resultSet.getLong(DatabaseHandler.LABWORK_TABLE_AVERAGE_POINT_COLUMN);
        Difficulty difficulty = Difficulty.valueOf(resultSet.getString(DatabaseHandler.LABWORK_TABLE_DIFFICULTY_COLUMN));
        Person author = getAuthorById(resultSet.getLong(DatabaseHandler.LABWORK_TABLE_AUTHOR_ID_COLUMN));
        User owner = databaseUserManager.getUserById(resultSet.getLong(DatabaseHandler.LABWORK_TABLE_USER_ID_COLUMN));
        return new LabWork(
                id,
                name,
                coordinates,
                creationDate,
                minimalPoint,
                personalQualitiesMinimum,
                averagePoint,
                difficulty,
                author,
                owner
        );
    }

    public TreeSet<LabWork> getCollection() throws DatabaseHandlingException {
        TreeSet<LabWork> labWorkList = new TreeSet<>();
        PreparedStatement preparedSelectAllStatement = null;
        try {
            preparedSelectAllStatement = databaseHandler.getPreparedStatement(SELECT_ALL_LABWORKS, false);
            ResultSet resultSet = preparedSelectAllStatement.executeQuery();
            while (resultSet.next()) {
                labWorkList.add(createLabWork(resultSet));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new DatabaseHandlingException();
        } finally {
            databaseHandler.closePreparedStatement(preparedSelectAllStatement);
        }
        return labWorkList;
    }

    private long getAuthorIdByLabWorkId(long labWorkId) throws SQLException {
        long authorId;
        PreparedStatement preparedSelectLabworkByIdStatement = null;
        try {
            preparedSelectLabworkByIdStatement = databaseHandler.getPreparedStatement(SELECT_LABWORK_BY_ID, false);
            preparedSelectLabworkByIdStatement.setLong(1, labWorkId);
            ResultSet resultSet = preparedSelectLabworkByIdStatement.executeQuery();
            ServerApp.logger.info("Выполнен запрос SELECT_LABWORK_BY_ID.");
            if (resultSet.next()) {
                authorId = resultSet.getLong(DatabaseHandler.LABWORK_TABLE_AUTHOR_ID_COLUMN);
            } else throw new SQLException();
        } catch (SQLException exception) {
            exception.printStackTrace();
            ServerApp.logger.error("Произошла ошибка при выполнении запроса SELECT_LABWORK_BY_ID!");
            throw new SQLException(exception);
        } finally {
            databaseHandler.closePreparedStatement(preparedSelectLabworkByIdStatement);
        }
        return authorId;
    }

    private Coordinates getCoordinatesByLabWorkId(long labWorkId) throws SQLException {
        Coordinates coordinates;
        PreparedStatement preparedSelectCoordinatesByLabworkIdStatement = null;
        try {
            preparedSelectCoordinatesByLabworkIdStatement =
                    databaseHandler.getPreparedStatement(SELECT_COORDINATES_BY_LABWORK_ID, false);
            preparedSelectCoordinatesByLabworkIdStatement.setLong(1, labWorkId);
            ResultSet resultSet = preparedSelectCoordinatesByLabworkIdStatement.executeQuery();
            ServerApp.logger.info("Выполнен запрос SELECT_COORDINATES_BY_LABWORK_ID.");
            if (resultSet.next()) {
                coordinates = new Coordinates(
                        resultSet.getDouble(DatabaseHandler.COORDINATES_TABLE_X_COLUMN),
                        resultSet.getLong(DatabaseHandler.COORDINATES_TABLE_Y_COLUMN)
                );
            } else throw new SQLException();
        } catch (SQLException exception) {
            ServerApp.logger.error("Произошла ошибка при выполнении запроса SELECT_COORDINATES_BY_LABWORK_ID!");
            throw new SQLException(exception);
        } finally {
            databaseHandler.closePreparedStatement(preparedSelectCoordinatesByLabworkIdStatement);
        }
        return coordinates;
    }

    private Person getAuthorById(long authorId) throws SQLException {
        Person author;
        PreparedStatement preparedSelectAuthorByIdStatement = null;
        try {
            preparedSelectAuthorByIdStatement = databaseHandler.getPreparedStatement(SELECT_AUTHOR_BY_ID, false);
            preparedSelectAuthorByIdStatement.setLong(1, authorId);
            ResultSet resultSet = preparedSelectAuthorByIdStatement.executeQuery();
            ServerApp.logger.info("Выполнен запрос SELECT_AUTHOR_BY_ID.");
            if (resultSet.next()) {
                author = new Person(
                        resultSet.getString(DatabaseHandler.AUTHOR_TABLE_NAME_COLUMN),
                        resultSet.getTimestamp(DatabaseHandler.AUTHOR_TABLE_BIRTHDAY_COLUMN).toLocalDateTime(),
                        resultSet.getString(DatabaseHandler.AUTHOR_TABLE_PASSPORT_ID_COLUMN)
                );
            } else throw new SQLException();
        } catch (SQLException exception) {
            ServerApp.logger.error("Произошла ошибка при выполнении запроса ELECT_AUTHOR_BY_ID!");
            throw new SQLException(exception);
        } finally {
            databaseHandler.closePreparedStatement(preparedSelectAuthorByIdStatement);
        }
        return author;
    }

    public LabWork insertLabwork(LabWorkForRequest labWorkForRequest, User user) throws DatabaseHandlingException {
        LabWork labWork;
        PreparedStatement preparedInsertLabWorkStatement = null;
        PreparedStatement preparedInsertCoordinatesStatement = null;
        PreparedStatement preparedInsertAuthorStatement = null;
        try {
            databaseHandler.setCommitMode();
            databaseHandler.setSavepoint();
            LocalDateTime creationTime = LocalDateTime.now();

            preparedInsertLabWorkStatement = databaseHandler.getPreparedStatement(INSERT_LABWORK, true);
            preparedInsertCoordinatesStatement = databaseHandler.getPreparedStatement(INSERT_COORDINATES, true);
            preparedInsertAuthorStatement = databaseHandler.getPreparedStatement(INSERT_AUTHOR, true);

            preparedInsertAuthorStatement.setString(1, labWorkForRequest.getAuthor().getName());
            preparedInsertAuthorStatement.setString(2, (labWorkForRequest.getAuthor().getBirthday()).format(dateTimeFormatter));
            preparedInsertAuthorStatement.setString(3,labWorkForRequest.getAuthor().getPassportID());
            if (preparedInsertAuthorStatement.executeUpdate() == 0) throw new SQLException();
            ResultSet generatedAuthorKeys = preparedInsertAuthorStatement.getGeneratedKeys();
            long authorId;
            if (generatedAuthorKeys.next()) {
                authorId = generatedAuthorKeys.getLong(1);
            } else throw new SQLException();
            ServerApp.logger.info("Выполнен запрос INSERT_AUTHOR");

            preparedInsertLabWorkStatement.setLong(1, generateNextId());
            preparedInsertLabWorkStatement.setString(2, labWorkForRequest.getName());
            preparedInsertLabWorkStatement.setString(3, (creationTime).format(dateTimeFormatter));
            preparedInsertLabWorkStatement.setInt(4, labWorkForRequest.getMinimalPoint());
            preparedInsertLabWorkStatement.setFloat(5, labWorkForRequest.getPersonalQualitiesMinimum());
            preparedInsertLabWorkStatement.setLong(6, labWorkForRequest.getAveragePoint());
            preparedInsertLabWorkStatement.setString(7, labWorkForRequest.getDifficulty().toString());
            preparedInsertLabWorkStatement.setLong(8, authorId);
            preparedInsertLabWorkStatement.setLong(9, databaseUserManager.getUserIdByUsername(user));
            System.out.println(preparedInsertLabWorkStatement); ////!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            if (preparedInsertLabWorkStatement.executeUpdate() == 0) throw new SQLException();
            ResultSet generatedLabWorkKeys = preparedInsertLabWorkStatement.getGeneratedKeys();
            long labWorkId;
            if (generatedLabWorkKeys.next()) {
                labWorkId = generatedLabWorkKeys.getLong(1);
            } else throw new SQLException();
            ServerApp.logger.info("Выполнен запрос INSERT_LABWORK");

            preparedInsertCoordinatesStatement.setLong(1, labWorkId);
            preparedInsertCoordinatesStatement.setDouble(2, labWorkForRequest.getCoordinates().getX());
            preparedInsertCoordinatesStatement.setLong(3, labWorkForRequest.getCoordinates().getY());
            if (preparedInsertCoordinatesStatement.executeUpdate() == 0) throw new SQLException();
            ServerApp.logger.info("Выполнен запрос INSERT_COORDINATES");

            labWork = new LabWork(
                    (int) labWorkId,
                    labWorkForRequest.getName(),
                    labWorkForRequest.getCoordinates(),
                    creationTime,
                    labWorkForRequest.getMinimalPoint(),
                    labWorkForRequest.getPersonalQualitiesMinimum(),
                    labWorkForRequest.getAveragePoint(),
                    labWorkForRequest.getDifficulty(),
                    labWorkForRequest.getAuthor(),
                    user
            );

            databaseHandler.commit();
            return labWork;
        } catch (SQLException exception) {
            exception.printStackTrace();
            ServerApp.logger.error("Произошла ошибка при выполнении группы запросов на добавление нового объекта!");
            databaseHandler.rollback();
            throw new DatabaseHandlingException();
        } finally {
            databaseHandler.closePreparedStatement(preparedInsertLabWorkStatement);
            databaseHandler.closePreparedStatement(preparedInsertCoordinatesStatement);
            databaseHandler.closePreparedStatement(preparedInsertAuthorStatement);
            databaseHandler.setNormalMode();
        }
    }

    public void updateLabWorkById(long labWorkId, LabWorkForRequest labWorkForRequest) throws DatabaseHandlingException {
        PreparedStatement preparedUpdateLabWorkNameByIdStatement = null;
        PreparedStatement preparedUpdateLabWorkMinimalPointByIdStatement = null;
        PreparedStatement preparedUpdateLabWorkPersonalQualitiesMinimumByIdStatement = null;
        PreparedStatement preparedUpdateLabWorkAveragePointByIdStatement = null;
        PreparedStatement preparedUpdateLabWorkDifficultyByIdStatement = null;
        PreparedStatement preparedUpdateCoordinatesByLabWorkIdStatement = null;
        PreparedStatement preparedUpdateAuthorByIdStatement = null;
        try {
            databaseHandler.setCommitMode();
            databaseHandler.setSavepoint();

            preparedUpdateLabWorkNameByIdStatement = databaseHandler.getPreparedStatement(UPDATE_LABWORK_NAME_BY_ID, false);
            preparedUpdateLabWorkMinimalPointByIdStatement = databaseHandler.getPreparedStatement(UPDATE_LABWORK_MINIMAL_POINT_BY_ID, false);
            preparedUpdateLabWorkPersonalQualitiesMinimumByIdStatement = databaseHandler.getPreparedStatement(UPDATE_LABWORK_PERSONAL_QUALITIES_MINIMUM_BY_ID, false);
            preparedUpdateLabWorkAveragePointByIdStatement = databaseHandler.getPreparedStatement(UPDATE_LABWORK_AVERAGE_POINT_BY_ID, false);
            preparedUpdateLabWorkDifficultyByIdStatement = databaseHandler.getPreparedStatement(UPDATE_LABWORK_DIFFICULTY_BY_ID, false);
            preparedUpdateCoordinatesByLabWorkIdStatement = databaseHandler.getPreparedStatement(UPDATE_COORDINATES_BY_LABWORK_ID, false);
            preparedUpdateAuthorByIdStatement = databaseHandler.getPreparedStatement(UPDATE_AUTHOR_BY_ID, false);

            if (labWorkForRequest.getName() != null) {
                preparedUpdateLabWorkNameByIdStatement.setString(1, labWorkForRequest.getName());
                preparedUpdateLabWorkNameByIdStatement.setLong(2, labWorkId);
                if (preparedUpdateLabWorkNameByIdStatement.executeUpdate() == 0) throw new SQLException();
                ServerApp.logger.info("Выполнен запрос UPDATE_LABWORK_NAME_BY_ID.");
            }
            if (labWorkForRequest.getCoordinates() != null) {
                preparedUpdateCoordinatesByLabWorkIdStatement.setDouble(1, labWorkForRequest.getCoordinates().getX());
                preparedUpdateCoordinatesByLabWorkIdStatement.setLong(2, labWorkForRequest.getCoordinates().getY());
                preparedUpdateCoordinatesByLabWorkIdStatement.setLong(3, labWorkId);
                if (preparedUpdateCoordinatesByLabWorkIdStatement.executeUpdate() == 0) throw new SQLException();
                ServerApp.logger.info("Выполнен запрос UPDATE_COORDINATES_BY_LABWORK_ID.");
            }
            if (labWorkForRequest.getMinimalPoint() != -1) {
                preparedUpdateLabWorkMinimalPointByIdStatement.setInt(1, labWorkForRequest.getMinimalPoint());
                preparedUpdateLabWorkMinimalPointByIdStatement.setLong(2, labWorkId);
                if (preparedUpdateLabWorkMinimalPointByIdStatement.executeUpdate() == 0) throw new SQLException();
                ServerApp.logger.info("Выполнен запрос UPDATE_LABWORK_MINIMAL_POINT_BY_ID.");
            }
            if (labWorkForRequest.getPersonalQualitiesMinimum() != -1) {
                preparedUpdateLabWorkPersonalQualitiesMinimumByIdStatement.setFloat(1, labWorkForRequest.getPersonalQualitiesMinimum());
                preparedUpdateLabWorkPersonalQualitiesMinimumByIdStatement.setLong(2, labWorkId);
                if (preparedUpdateLabWorkPersonalQualitiesMinimumByIdStatement.executeUpdate() == 0) throw new SQLException();
                ServerApp.logger.info("Выполнен запрос UPDATE_LABWORK_PERSONAL_QUALITIES_MINIMUM_BY_ID.");
            }
            if (labWorkForRequest.getAveragePoint() != -1) {
                preparedUpdateLabWorkAveragePointByIdStatement.setLong(1, labWorkForRequest.getAveragePoint());
                preparedUpdateLabWorkAveragePointByIdStatement.setLong(2, labWorkId);
                if (preparedUpdateLabWorkAveragePointByIdStatement.executeUpdate() == 0) throw new SQLException();
                ServerApp.logger.info("Выполнен запрос UPDATE_LABWORK_MINIMAL_POINT_BY_ID.");
            }
            if (labWorkForRequest.getDifficulty() != null) {
                preparedUpdateLabWorkDifficultyByIdStatement.setString(1, labWorkForRequest.getDifficulty().toString());
                preparedUpdateLabWorkDifficultyByIdStatement.setLong(2, labWorkId);
                if (preparedUpdateLabWorkDifficultyByIdStatement.executeUpdate() == 0) throw new SQLException();
                ServerApp.logger.info("Выполнен запрос UPDATE_LABWORK_NAME_BY_ID.");
            }
            if (labWorkForRequest.getAuthor() != null) {
                preparedUpdateAuthorByIdStatement.setString(1, labWorkForRequest.getAuthor().getName());
                preparedUpdateAuthorByIdStatement.setString(2, (labWorkForRequest.getAuthor().getBirthday()).format(dateTimeFormatter));
                preparedUpdateAuthorByIdStatement.setString(3, labWorkForRequest.getAuthor().getPassportID());
                preparedUpdateAuthorByIdStatement.setLong(4, getAuthorIdByLabWorkId(labWorkId));
                if (preparedUpdateAuthorByIdStatement.executeUpdate() == 0) throw new SQLException();
                ServerApp.logger.info("Выполнен запрос UPDATE_AUTHOR_BY_ID.");

                databaseHandler.commit();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            ServerApp.logger.error("Произошла ошибка при выполнении группы запросов на обновление объекта!");
            databaseHandler.rollback();
            throw new DatabaseHandlingException();
        } finally {
            databaseHandler.closePreparedStatement(preparedUpdateLabWorkNameByIdStatement);
            databaseHandler.closePreparedStatement(preparedUpdateLabWorkMinimalPointByIdStatement);
            databaseHandler.closePreparedStatement(preparedUpdateLabWorkPersonalQualitiesMinimumByIdStatement);
            databaseHandler.closePreparedStatement(preparedUpdateLabWorkAveragePointByIdStatement);
            databaseHandler.closePreparedStatement(preparedUpdateLabWorkDifficultyByIdStatement);
            databaseHandler.closePreparedStatement(preparedUpdateCoordinatesByLabWorkIdStatement);
            databaseHandler.closePreparedStatement(preparedUpdateAuthorByIdStatement);
            databaseHandler.setNormalMode();
        }
    }

    public void deleteAuthorByAuthorId(long authorID) throws DatabaseHandlingException {
        PreparedStatement preparedDeleteAuthorByIdStatement = null;
        try {
            preparedDeleteAuthorByIdStatement = databaseHandler.getPreparedStatement(DELETE_AUTHOR_BY_ID, false);
            preparedDeleteAuthorByIdStatement.setLong(1, authorID);
            if (preparedDeleteAuthorByIdStatement.executeUpdate() == 0) Outputer.println(3);
            ServerApp.logger.info("Выполнен запрос DELETE_AUTHOR_BY_ID.");
        } catch (SQLException exception) {
            exception.printStackTrace();
            ServerApp.logger.error("Произошла ошибка при выполнении запроса DELETE_AUTHOR_BY_ID!");
            throw new DatabaseHandlingException();
        } finally {
            databaseHandler.closePreparedStatement(preparedDeleteAuthorByIdStatement);
        }
    }

    public void deleteCoordinatesByLabWorkId(long labWorkId) throws DatabaseHandlingException {
        PreparedStatement preparedDeleteCoordinatesBylabWorkIdStatement = null;
        try {
            preparedDeleteCoordinatesBylabWorkIdStatement = databaseHandler.getPreparedStatement(DELETE_COORDINATES_BY_LABWORK_ID, false);
            preparedDeleteCoordinatesBylabWorkIdStatement.setLong(1, labWorkId);
            if (preparedDeleteCoordinatesBylabWorkIdStatement.executeUpdate() == 0) Outputer.println(3);
            ServerApp.logger.info("Выполнен запрос DELETE_COORDINATES_BY_LABWORK_ID.");
        } catch (SQLException exception) {
            ServerApp.logger.error("Произошла ошибка при выполнении запроса DELETE_COORDINATES_BY_LABWORK_ID!");
            throw new DatabaseHandlingException();
        } finally {
            databaseHandler.closePreparedStatement(preparedDeleteCoordinatesBylabWorkIdStatement);
        }
    }
    public void deleteLabworkById(long labWorkId) throws DatabaseHandlingException {
        PreparedStatement preparedDeleteLabWorkByIdStatement = null;
        try {
            long authorID = getAuthorIdByLabWorkId(labWorkId);
            deleteCoordinatesByLabWorkId(labWorkId);
            preparedDeleteLabWorkByIdStatement = databaseHandler.getPreparedStatement(DELETE_LABWORK_BY_ID, false);
            preparedDeleteLabWorkByIdStatement.setLong(1, labWorkId);
            if (preparedDeleteLabWorkByIdStatement.executeUpdate() == 0) Outputer.println(3);
            ServerApp.logger.info("Выполнен запрос DELETE_LABWORK_BY_ID.");
            deleteAuthorByAuthorId(authorID);
        } catch (SQLException exception) {
            ServerApp.logger.error("Произошла ошибка при выполнении запроса DELETE_LABWORK_BY_ID!");
            throw new DatabaseHandlingException();
        } finally {
            databaseHandler.closePreparedStatement(preparedDeleteLabWorkByIdStatement);
        }
    }

    public boolean checkLabWorkUserId(long labWorkId, User user) throws DatabaseHandlingException {
        PreparedStatement preparedSelectLabworkByIdAndUserIdStatement = null;
        try {
            preparedSelectLabworkByIdAndUserIdStatement = databaseHandler.getPreparedStatement(SELECT_LABWORK_BY_ID_AND_USER_ID, false);
            preparedSelectLabworkByIdAndUserIdStatement.setLong(1, labWorkId);
            preparedSelectLabworkByIdAndUserIdStatement.setLong(2, databaseUserManager.getUserIdByUsername(user));
            ResultSet resultSet = preparedSelectLabworkByIdAndUserIdStatement.executeQuery();
            ServerApp.logger.info("Выполнен запрос SELECT_LABWORK_BY_ID_AND_USER_ID.");
            return resultSet.next();
        } catch (SQLException exception) {
            ServerApp.logger.error("Произошла ошибка при выполнении запроса SELECT_LABWORK_BY_ID_AND_USER_ID!");
            throw new DatabaseHandlingException();
        } finally {
            databaseHandler.closePreparedStatement(preparedSelectLabworkByIdAndUserIdStatement);
        }
    }

    public int generateNextId() throws DatabaseHandlingException {
        if (getCollection().isEmpty()) {
            return 1;
        } else {
            int id = 0;
            for (LabWork labWork : getCollection()) {
                if (labWork.getId() > id) {
                    id = labWork.getId();
                }
            }
            return id + 1;
        }
    }

    public void clearCollection() throws DatabaseHandlingException {
        NavigableSet<LabWork> labWorkList = getCollection();
        for (LabWork labWork : labWorkList) {
            deleteLabworkById(labWork.getId());
        }
    }
}

