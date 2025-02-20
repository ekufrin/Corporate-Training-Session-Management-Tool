package hr.ekufrin.trainingapplication.model;


public class TrainingSession {
    private Long id;
    private String name;
    private Integer duration;
    private String date;
    private Integer maxParticipants;
    private String location;
    private String status;

    public TrainingSession(Long id, String name, Integer duration, String date, Integer maxParticipants, String location, String status) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.date = date;
        this.maxParticipants = maxParticipants;
        this.location = location;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getDuration() {
        return duration;
    }

    public String getDate() {
        return date;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public String getLocation() {
        return location;
    }

    public String getStatus() {
        return status;
    }
}