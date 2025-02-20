package hr.ekufrin.training.model;

import hr.ekufrin.training.interfaces.FeedbackType;

/**
 * Represents negative feedback.
 */
public final class  NegativeFeedback implements FeedbackType {
    /**
     * Returns negative feedback.
     * @return - Negative :(
     */
    @Override
    public String getFeedback() {
        return "Negative :(";
    }
}
