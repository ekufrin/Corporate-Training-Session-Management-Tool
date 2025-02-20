package hr.ekufrin.training.repository;

import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.exception.NoDataFoundInDatabaseException;
import hr.ekufrin.training.generics.Repository;
import hr.ekufrin.training.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Optional;

import static hr.ekufrin.training.HelloApplication.log;
import static hr.ekufrin.training.repository.DatabaseRepository.ERROR_CONNECTING_TO_DATABASE;
import static hr.ekufrin.training.repository.DatabaseRepository.HASHED_PASSWORD;

/**
 * Repository for the User entity
 */
public class UserRepository extends Repository<User> {

    private final HashSet<User> users = new HashSet<>();

    /**
     * Saves the user to the database
     * @param entity - entity to save
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public void save(User entity) throws ConnectionToDatabaseException {
        String hashedPassword = BCrypt.hashpw(entity.getPassword(), BCrypt.gensalt(12));
        try (Connection conn = DatabaseRepository.connectToDB();
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO public.user(first_name,last_name,email,password_hash,role_id) VALUES (?,?,?,?,?)")) {
            stmt.setString(1, entity.getFirstName());
            stmt.setString(2, entity.getLastName());
            stmt.setString(3, entity.getEmail());
            stmt.setString(4, hashedPassword);
            stmt.setLong(5, entity.getRole().getId());
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and saving user {}, with exception: {}",entity.toString(), e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and saving user " + entity.toString());
        }
    }

    /**
     * Deletes the user from the database
     * @param entity - user to delete
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public void delete(User entity) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM public.user WHERE id = ?")){
            stmt.setLong(1, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and deleting user {}, with exception: {}",entity.toString(), e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and deleting user " + entity.toString());
        }
    }

    /**
     * Finds a user by its id
     * @param id - id of user to find
     * @return - user with the given id
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     * @throws NoDataFoundInDatabaseException - If there is no user with the given id
     */
    @Override
    public User findById(Long id) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("SELECT id,first_name,last_name,email,password_hash,role_id FROM public.user WHERE id = ?")) {
            stmt.setLong(1, id);
            ResultSet rS = stmt.executeQuery();
            if (rS.next()) {
                RoleRepository roleRepo = new RoleRepository();
                return new User.Builder()
                        .setId(rS.getLong("id"))
                        .setFirstName(rS.getString("first_name"))
                        .setLastName(rS.getString("last_name"))
                        .setEmail(rS.getString("email"))
                        .setPassword(rS.getString(HASHED_PASSWORD))
                        .setRole(roleRepo.findById(rS.getLong("role_id")))
                        .build();
            } else {
                log.info("No user found with id: {}", id);
                throw new NoDataFoundInDatabaseException("No user found with id " + id);
            }

        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and finding user with ID: {}, with exception: {}",id, e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and finding user with ID:" + id);
        }
    }

    /**
     * Finds all users in the database
     * @return - hashset of all users in the database
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public HashSet<User> findAll() throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             Statement stmt = conn.createStatement()) {
            ResultSet rS = stmt.executeQuery("SELECT id,first_name,last_name,email,password_hash,role_id FROM public.user");
            RoleRepository roleRepo = new RoleRepository();
            while (rS.next()) {
                User user = new User.Builder()
                        .setId(rS.getLong("id"))
                        .setFirstName(rS.getString("first_name"))
                        .setLastName(rS.getString("last_name"))
                        .setEmail(rS.getString("email"))
                        .setPassword(rS.getString(HASHED_PASSWORD))
                        .setRole(roleRepo.findById(rS.getLong("role_id")))
                        .build();
                users.add(user);
            }

        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and finding all users, with exception: {}", e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and fetching all users");
        }
        return users;
    }

    /**
     * Checks if the user with the given email exists in the database
     * If the user exists, checks if the password hash matches the one in the database
     * @param email - email of the user
     * @param password - password of the user
     * @return - id of the user if the login is successful, empty optional otherwise
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    public Optional<Long> login(String email, String password) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, password_hash FROM public.user WHERE email = ?")) {

            stmt.setString(1, email.toLowerCase());
            ResultSet rS = stmt.executeQuery();
            if (rS.next()) {
                if (BCrypt.checkpw(password, rS.getString(HASHED_PASSWORD))) {
                    Long id = rS.getLong("id");
                    return Optional.of(id);
                } else {
                    return Optional.empty();
                }
            } else {
                log.error("Login failed! No user found with email: {}", email);
                return Optional.empty();
            }

        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and logging in user with email: {}, with exception: {}",email, e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and logging in user with email: " + email);
        }
    }

    /**
     * Updates the user in the database
     * @param user - user to update
     * @return - true if the user was updated, false otherwise
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    public Boolean update(User user) throws ConnectionToDatabaseException {
        try (Connection conn = DatabaseRepository.connectToDB()) {
            String currentHashedPassword = null;
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT password_hash FROM public.user WHERE id = ?")) {
                checkStmt.setLong(1, user.getId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    currentHashedPassword = rs.getString("password_hash");
                }
            }

            String newHashedPassword = currentHashedPassword;
            if (!user.getPassword().equals(currentHashedPassword)) {
                newHashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE public.user SET first_name = ?, last_name = ?, email = ?, password_hash = ?, role_id = ? WHERE id = ?")) {
                stmt.setString(1, user.getFirstName());
                stmt.setString(2, user.getLastName());
                stmt.setString(3, user.getEmail());
                stmt.setString(4, newHashedPassword);
                stmt.setLong(5, user.getRole().getId());
                stmt.setLong(6, user.getId());
                return stmt.executeUpdate() > 0;
            }

        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and updating user {}, with exception: {}", user.toString(), e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and updating user " + user.toString());
        }
    }

}
