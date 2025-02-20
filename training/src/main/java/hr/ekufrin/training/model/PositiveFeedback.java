package hr.ekufrin.training.model;

import hr.ekufrin.training.interfaces.FeedbackType;

/**
 * Represents a positive feedback.
 */
public final class PositiveFeedback implements FeedbackType {
    /**
     * Returns a positive feedback.
     * @return - Positive :)
     */
    @Override
    public String getFeedback() {
        return "Positive :)";
    }
}
