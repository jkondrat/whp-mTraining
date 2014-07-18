package org.motechproject.whp.mtraining.service;

import org.motechproject.whp.mtraining.dto.ContentIdentifierDto;
import org.motechproject.whp.mtraining.dto.CourseDto;

import java.util.List;
import java.util.UUID;

/**
 * Service Interface that exposes APIs to manage different course contents
 */
public interface CourseService {

    /**
     * Add a course if it already does not exist, update it otherwise.
     * @param courseDto
     * @return
     */
    ContentIdentifierDto addOrUpdateCourse(CourseDto courseDto);

    /**
     * Return course with given course identifier
     * @param courseId
     * @return
     */
    CourseDto getCourse(ContentIdentifierDto courseId);

    /**
     * Return all courses
     * @return
     */
    List<CourseDto> getAllCourses();

    /**
     * Given a course identifier, return the latest active version which has also been published.
     * A course that is marked published when it has been synced with other external system(s) successfully.
     * The published course is considered ready for consumption of students of mtraining.
     * @param contentId
     * @return
     */
    CourseDto getLatestPublishedCourse(UUID contentId);

    /**
     * Mark the course with given course identifier as published.
     * @param courseIdentifier
     */
    void publish(ContentIdentifierDto courseIdentifier);
}
