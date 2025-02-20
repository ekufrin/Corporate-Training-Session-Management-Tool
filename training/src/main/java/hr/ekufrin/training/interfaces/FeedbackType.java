package hr.ekufrin.training.interfaces;

import hr.ekufrin.training.model.NegativeFeedback;
import hr.ekufrin.training.model.PositiveFeedback;

/**
 * FeedbackType interface is used to define the feedback type.
 */
public sealed interface FeedbackType permits PositiveFeedback, NegativeFeedback {
    String getFeedback();
}
