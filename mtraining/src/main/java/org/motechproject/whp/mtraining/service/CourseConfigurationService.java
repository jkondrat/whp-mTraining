package org.motechproject.whp.mtraining.service;

import org.motechproject.whp.mtraining.dto.CourseConfigurationDto;

/**
 * Service Interface that exposes APIs to manage configurations for different contents
 */
public interface CourseConfigurationService {
    void addOrUpdateCourseConfiguration(CourseConfigurationDto courseDto);
}
