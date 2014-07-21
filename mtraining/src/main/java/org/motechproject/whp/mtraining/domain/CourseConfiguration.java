package org.motechproject.whp.mtraining.domain;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.TypeDiscriminator;
import org.motechproject.commons.couchdb.model.MotechBaseDataObject;

import java.util.UUID;

/**
 * Couch document object representing a Course configuration which is region specific
 * + courseId : content id of a course
 * + durationInDays : no of days in which the course has to be completed
 * + Location : the region for which the course is configured
 */

@TypeDiscriminator("doc.type === 'CourseConfiguration'")
public class CourseConfiguration extends MotechBaseDataObject {

    @JsonProperty
    private UUID courseId;

    @JsonProperty
    private Integer durationInDays;

    @JsonProperty
    private Location location;

    CourseConfiguration() {
    }

    public CourseConfiguration(UUID courseId, Integer durationInDays, Location location) {
        this.courseId = courseId;
        this.durationInDays = durationInDays;
        this.location = location;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public Integer getDurationInDays() {
        return durationInDays;
    }

    public Location getLocation() {
        return location;
    }

    public void update(Integer courseDuration, Location location) {
        this.durationInDays = courseDuration;
        this.location = location;
    }
}
