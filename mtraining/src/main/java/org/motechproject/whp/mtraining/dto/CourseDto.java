package org.motechproject.whp.mtraining.dto;

import java.util.List;
import java.util.UUID;

/**
 * Contract Class
 * CourseDto is the root level node in the course content hierarchy.
 * Course
 *   + modules : List of Modules
 *   + name    : course name (eg. CS 001)
 *   + description : Course description (eg. Computer Science Fundamentals for 1st year students)
 *   + contentId   : UUID that for a course (different from the _id generated by couch)
 *   + version     : course version (a course can have multiple versions, different versions of the course will have same contentId)
 *   + externalContentId  : Id that points to an external file or resource that is associated with the course.For eg. an audio file that is played to the enrollee when the course is started
 *   + createdBy    : Author of the course
 *   + createdOn    : Date on which course was created
 */
public class CourseDto extends ContentDto {
    private String name;
    private String description;

    public CourseDto() {
    }

    public CourseDto(boolean isActive, String name, String description, String externalId, String createdBy) {
        super(isActive, externalId, createdBy);
        this.name = name;
        this.description = description;
    }

    public CourseDto(UUID contentId, Integer version, boolean isActive, String name, String description, String externalId, String createdBy) {
        super(contentId, version, isActive, externalId, createdBy);
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
