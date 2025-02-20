package hr.ekufrin.training.repository;

import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.exception.NoDataFoundInDatabaseException;
import hr.ekufrin.training.generics.Repository;
import hr.ekufrin.training.model.Log;

import java.io.*;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static hr.ekufrin.training.HelloApplication.log;
import static hr.ekufrin.training.repository.DatabaseRepository.ERROR_CONNECTING_TO_DATABASE;

/**
 * Repository for the Log entity
 */
public class LogRepository extends Repository<Log> {
    private static final String BINARY_FILE_PATH = "dat/logs.dat";
    private static final ReentrantLock lock = new ReentrantLock();

    /**
     * Saves the log to the database and to the binary file
     * Uses a lock to prevent multiple threads from writing to the binary file at the same time
     * @param entity - entity to save
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public void save(Log entity) throws ConnectionToDatabaseException {
        lock.lock();
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO log(user_id, changed_field, old_value, new_value, operation,timestamp) VALUES (?,?,?,?,?,?)")) {

            stmt.setLong(1, entity.getUserId());
            stmt.setString(2, entity.getChangedField());
            stmt.setString(3, entity.getOldValue());
            stmt.setString(4, entity.getNewValue());
            stmt.setString(5, entity.getOperation());
            stmt.setTimestamp(6, Timestamp.valueOf(entity.getTimestamp()));
            stmt.executeUpdate();

            writeToBinaryFile(entity);
        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and saving log with field {} changed from {} to {}, exception: {}",
                    entity.getChangedField(), entity.getOldValue(), entity.getNewValue(), e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and saving log with field " + entity.getChangedField() +
                    " changed from " + entity.getOldValue() + " to " + entity.getNewValue());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void delete(Log entity) {
        log.info("Deleting logs is not allowed.");
        throw new UnsupportedOperationException("Deleting logs is not allowed.");
    }

    /**
     * Finds a log by its id
     * @param id - id of log to find
     * @return - Log with the given id
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     * @throws NoDataFoundInDatabaseException - If there is no log with the given id
     */
    @Override
    public Log findById(Long id) throws ConnectionToDatabaseException {
        try(Connection conn = DatabaseRepository.connectToDB();
            PreparedStatement stmt = conn.prepareStatement("SELECT user_id, changed_field, old_value, new_value, operation,timestamp FROM log WHERE id = ?")) {
            stmt.setLong(1, id);
            ResultSet rS = stmt.executeQuery();
            if(rS.next()){
                return new Log(id, rS.getTimestamp("timestamp").toLocalDateTime(),
                        new UserRepository().findById(rS.getLong("user_id")).getId(),
                        rS.getString("changed_field"),
                        rS.getString("old_value"),
                        rS.getString("new_value"),
                        rS.getString("operation"));
            }
            else{
                log.info("No log found with id: {}", id);
                throw new NoDataFoundInDatabaseException("No log found with id: " + id);
            }
        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and finding log with id {}, with exception: {}", id, e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and finding log with id " + id);
        }
    }

    /**
     * Finds all logs in the database
     * Uses a lock to prevent multiple threads from reading the binary file at the same time
     * @return - hashset of all logs in the database
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public HashSet<Log> findAll() throws ConnectionToDatabaseException {
        lock.lock();
        try(Connection conn = DatabaseRepository.connectToDB();
            Statement stmt = conn.createStatement()) {
            ResultSet rS = stmt.executeQuery("SELECT id, user_id, changed_field, old_value, new_value, operation,timestamp FROM log");
            HashSet<Log> logs = new HashSet<>();
            while(rS.next()){
                logs.add(new Log(rS.getLong("id"), rS.getTimestamp("timestamp").toLocalDateTime(),
                        new UserRepository().findById(rS.getLong("user_id")).getId(),
                        rS.getString("changed_field"),
                        rS.getString("old_value"),
                        rS.getString("new_value"),
                        rS.getString("operation")));
            }
            return logs;
        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and finding all logs, with exception: {}", e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and finding all logs");
        }finally {
            lock.unlock();
        }
    }

    /**
     * Writes a log to the binary file
     * @param log - log to write
     * @throws IOException - If there is an error while writing to the binary file
     */
    private void writeToBinaryFile(Log log) throws IOException{
        HashSet<Log> logs = (HashSet<Log>) readFromBinaryFile();
        logs.add(log);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BINARY_FILE_PATH))) {
            oos.writeObject(logs);
        }
    }

    /**
     * Reads logs from the binary file
     * Uses lock to prevent multiple threads from reading the binary file at the same time
     * @return - set of logs read from the binary file
     */
    public Set<Log> readFromBinaryFile() {
        lock.lock();
        File file = new File(BINARY_FILE_PATH);
        if (!file.exists() || file.length() == 0) return new HashSet<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(BINARY_FILE_PATH))) {
            return (HashSet<Log>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error reading logs from binary file log: {}", e.getMessage());
            return new HashSet<>();
        } finally {
            lock.unlock();
        }
    }

}
