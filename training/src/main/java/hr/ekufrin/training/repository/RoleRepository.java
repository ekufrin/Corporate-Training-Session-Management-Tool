package hr.ekufrin.training.repository;

import hr.ekufrin.training.exception.ConnectionToDatabaseException;
import hr.ekufrin.training.exception.NoDataFoundInDatabaseException;
import hr.ekufrin.training.generics.Repository;
import hr.ekufrin.training.model.Role;

import java.io.IOException;
import java.sql.*;
import java.util.HashSet;

import static hr.ekufrin.training.HelloApplication.log;
import static hr.ekufrin.training.repository.DatabaseRepository.ERROR_CONNECTING_TO_DATABASE;

/**
 * Repository for the Role entity
 */
public class RoleRepository extends Repository<Role> {
    /**
     * Saves the role to the database
     * @param entity - entity to save
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public void save(Role entity) throws ConnectionToDatabaseException {
        try(Connection conn = DatabaseRepository.connectToDB();
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO role(name,description) VALUES (?,?)")) {

            stmt.setString(1, entity.getName());
            stmt.setString(2, entity.getDescription());
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and saving role {}, with exception: {}", entity.getName(), e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and saving role " + entity.getName());
        }
    }

    /**
     * Deletes the role from the database
     * @param entity - role to delete
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public void delete(Role entity) throws ConnectionToDatabaseException {
        try(Connection conn = DatabaseRepository.connectToDB();
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM role WHERE id = ?")) {

            stmt.setLong(1, entity.getId());
            stmt.executeUpdate();
        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and deleting role {}, with exception: {}", entity.getName(), e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and deleting role " + entity.getName());
        }
    }

    /**
     * Finds a role by its id
     * @param id - id of role to find
     * @return - role with the given id
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     * @throws NoDataFoundInDatabaseException - If there is no role with the given id
     */
    @Override
    public Role findById(Long id) throws ConnectionToDatabaseException {
        try(Connection conn = DatabaseRepository.connectToDB();
            PreparedStatement stmt = conn.prepareStatement("SELECT id,name,description FROM role WHERE id = ?")) {

            stmt.setLong(1, id);
            ResultSet rS = stmt.executeQuery();
            if(rS.next()) {
                return new Role(rS.getLong("id"),
                        rS.getString("name"),
                        rS.getString("description"));
            }
            else{
                log.info("No role found with id {}", id);
                throw new NoDataFoundInDatabaseException("No role found with id " + id);
            }
        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and finding role with id {}, with exception: {}", id, e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and finding role with id " + id);
        }
    }

    /**
     * Finds all roles in the database
     * @return - all roles in the database
     * @throws ConnectionToDatabaseException - If there is an error while connecting to the database
     */
    @Override
    public HashSet<Role> findAll() throws ConnectionToDatabaseException {
        try(Connection conn = DatabaseRepository.connectToDB();
            Statement stmt = conn.createStatement()) {

            ResultSet rS = stmt.executeQuery("SELECT id,name,description FROM role");
            HashSet<Role> roles = new HashSet<>();
            while(rS.next()) {
                roles.add(new Role(rS.getLong("id"),
                        rS.getString("name"),
                        rS.getString("description")));
            }
            return roles;
        } catch (SQLException | IOException e) {
            log.error(ERROR_CONNECTING_TO_DATABASE + "and finding all roles, with exception: {}", e.getMessage());
            throw new ConnectionToDatabaseException(ERROR_CONNECTING_TO_DATABASE + "and finding all roles");
        }
    }
}
