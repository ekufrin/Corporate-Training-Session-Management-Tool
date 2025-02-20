package hr.ekufrin.training.model;

import hr.ekufrin.training.interfaces.FeedbackType;

import java.time.LocalDateTime;

/**
 * Represents feedback given by employee after training session
 * @param id - unique identifier
 * @param trainingSession - training session that feedback is related to
 * @param employee - employee that gave feedback
 * @param rating - rating of training session
 * @param comment - comment about training session
 * @param timestamp - time when feedback was given
 * @param feedbackType - type of feedback (positive or negative)
 */
public record Feedback(Long id, TrainingSession trainingSession, User employee, Integer rating, String comment, LocalDateTime timestamp, FeedbackType feedbackType) {
    public static class Builder{
        private Long id;
        private TrainingSession trainingSession;
        private User user;
        private Integer rating;
        private String comment;
        private LocalDateTime timestamp;
        private FeedbackType feedbackType;

        public Builder id(Long id){
            this.id = id;
            return this;
        }

        public Builder trainingSession(TrainingSession trainingSession){
            this.trainingSession = trainingSession;
            return this;
        }

        public Builder user(User user){
            this.user = user;
            return this;
        }

        public Builder rating(Integer rating){
            this.rating = rating;
            this.feedbackType = getFeedbackType(rating);
            return this;
        }

        public Builder comment(String comment){
            this.comment = comment;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp){
            this.timestamp = timestamp;
            return this;
        }

        private static FeedbackType getFeedbackType(Integer rating){
            if(rating > 3){
                return new PositiveFeedback();
            }else{
                return new NegativeFeedback();
            }
        }

        public Feedback build(){
            return new Feedback(id, trainingSession, user, rating, comment, timestamp, feedbackType);
        }
    }


}
