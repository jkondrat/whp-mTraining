package org.motechproject.whp.mtraining.exception;

/**
 * Exception thrown when platform was unable to publish a course because it was inactive.
 */

public class CoursePublicationException extends RuntimeException {

    public CoursePublicationException(String message) {
        super(message);
    }
}
