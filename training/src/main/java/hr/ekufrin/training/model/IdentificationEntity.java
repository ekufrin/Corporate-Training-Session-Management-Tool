package hr.ekufrin.training.model;

import java.util.Objects;

/**
 * Abstract class that represents an id of an entity.
 */
public abstract class IdentificationEntity {
    private Long id;

    protected IdentificationEntity() {
    }

    protected IdentificationEntity(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IdentificationEntity that = (IdentificationEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
