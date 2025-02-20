package hr.ekufrin.training_api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import hr.ekufrin.training_api.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "training_session")
public class TrainingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "trainer_id",referencedColumnName = "id",nullable = false)
    @JsonIgnore
    private User trainer;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Integer duration;
    @Column(nullable = false)
    @JsonFormat(pattern = "dd.MM.yyyy. HH:mm")
    private LocalDateTime date;
    @Column(nullable = false)
    private Integer maxParticipants;
    @Column(nullable = false)
    private String location;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;
    @ManyToMany
    @JoinTable(
            name = "users_sessions",
            joinColumns = @JoinColumn(name = "session_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> participants;


}
