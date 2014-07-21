package org.motechproject.whp.mtraining.domain;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.TypeDiscriminator;
import org.joda.time.DateTime;
import org.motechproject.commons.couchdb.model.MotechBaseDataObject;
import org.motechproject.whp.mtraining.constants.CourseStatus;

import java.util.UUID;

/**
 * Couch document representing a Course Progress Object.
 * A course progress object stores the status of the a given course for a given enrollee.
 * There will one courseProgress for every course the enrollee enrols for
 * + courseStartTime : it is the time when the enrollee has started the course
 * + courseContentId : course content id for which the enrollee has enrolled
 * + courseStatus : status of the a bookmark in a course.
 */

@TypeDiscriminator("doc.type === 'EnrolleeCourseProgress'")
public class EnrolleeCourseProgress extends MotechBaseDataObject {
    private final UUID courseContentId;
    private String externalId;
    private DateTime courseStartTime;
    private CourseStatus courseStatus;

    @JsonCreator
    public EnrolleeCourseProgress(@JsonProperty("externalId") String externalId,
                                  @JsonProperty("courseStartTime") DateTime courseStartTime,
                                  @JsonProperty("courseStatus") CourseStatus courseStatus,
                                  @JsonProperty("courseContentId") UUID courseContentId) {
        this.externalId = externalId;
        this.courseStartTime = courseStartTime;
        this.courseStatus = courseStatus;
        this.courseContentId = courseContentId;
    }

    public String getExternalId() {
        return externalId;
    }

    public DateTime getCourseStartTime() {
        return courseStartTime;
    }

    public CourseStatus getCourseStatus() {
        return courseStatus;
    }


    public void update(String externalId, DateTime courseStartTime, CourseStatus courseStatus) {
        this.externalId = externalId;
        this.courseStartTime = courseStartTime;
        this.courseStatus = courseStatus;
    }

    public void markComplete() {
        this.courseStatus = CourseStatus.COMPLETED;
    }

    @JsonIgnore
    public boolean isFor(UUID givenCourseIdentifier) {
        if (givenCourseIdentifier == null) {
            return false;
        }

        return givenCourseIdentifier.equals(courseContentId);
    }

    @JsonIgnore
    public boolean notClosed() {
        return !isCourseClosed();
    }

    @JsonIgnore
    public boolean isCourseClosed() {
        return courseStatus != null && courseStatus.isClosed();
    }

    public UUID getCourseContentId() {
        return courseContentId;
    }
}
