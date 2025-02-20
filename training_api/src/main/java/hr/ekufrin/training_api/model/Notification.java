package hr.ekufrin.training_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String message;
    @Column(nullable = false, name = "sent_date")
    private LocalDateTime sentdate;
    @ManyToMany
    @JoinTable(
            name = "users_notifications",
            joinColumns = @JoinColumn(name = "notification_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnore
    private List<User> recipients;

    public Notification(Long id,String message, LocalDateTime sentdate) {
        this.id = id;
        this.message = message;
        this.sentdate = sentdate;
    }
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(message, that.message) && Objects.equals(sentdate, that.sentdate) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, sentdate, id);
    }


}
