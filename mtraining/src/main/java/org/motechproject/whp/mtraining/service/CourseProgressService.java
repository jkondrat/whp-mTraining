package org.motechproject.whp.mtraining.service;

import org.motechproject.whp.mtraining.dto.ContentIdentifierDto;
import org.motechproject.whp.mtraining.dto.EnrolleeCourseProgressDto;

import java.util.UUID;

/**
 *  Service that exposes APIs to manage Course Progress for a given Enrollee.
 *  Course Progress of an enrollee is captured in the @EnrolleeCourseProgress object which is stored in couch db.
 *  The contract object returned is @EnrolleeCourseProgressDto
 *  The returned @EnrolleeCourseProgressDto is composed of the enrollee course progress data as well the current bookmark for the student.
 *  It is assumed that at any given point of time, enrollee is enrolled into a single course only.
 *  Also please have a look into @CourseStatus
 */

public interface CourseProgressService {

    /**
     * Given a externalId (i.e the enrollee id) return a course progress dto for the currently enrolled course
     * @param externalId
     * @param courseContentId
     * @return
     */
    EnrolleeCourseProgressDto getCourseProgressForEnrollee(String externalId, Long courseContentId);

    /**
     * Given a externalId (i.e the enrollee id) and courseId return a course progress dto with the first bookmark from the given course.
     * The course status will be 'STARTED'
     * @param externalId
     * @return
     */
    EnrolleeCourseProgressDto getInitialCourseProgressForEnrollee(String externalId, ContentIdentifierDto courseIdentifier);

    /**
     * Add or update the course progress.
     * As the @EnrolleeCourseProgressDto also contains the updated bookmark,this API also updates the current bookmark.
     * @param enrolleeCourseProgressDto
     */
    void addOrUpdateCourseProgress(EnrolleeCourseProgressDto enrolleeCourseProgressDto);

    /**
     * Mark the course as specified by the courseIdentifier as complete for the given course.
     * The course status will become 'COMPLETED'
     * @param externalId
     * @param startTime
     * @param courseIdentifier
     */
    void markCourseAsComplete(String externalId, String startTime, ContentIdentifierDto courseIdentifier);

}
