package hr.ekufrin.training.model;

import java.util.Objects;

/**
 * Represents a role that can be assigned to a user.
 * Role can be Trainer or Employee.
 */
public class Role extends IdentificationEntity {
    private String name;
    private String description;

    /**
     * Constructor for Role class.
     * @param id - unique identifier of the role
     * @param name - name of the role
     * @param description - description of the role
     */
    public Role(Long id,String name, String description) {
        super(id);
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Role role = (Role) o;
        return Objects.equals(name, role.name) && Objects.equals(description, role.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, description);
    }
}
