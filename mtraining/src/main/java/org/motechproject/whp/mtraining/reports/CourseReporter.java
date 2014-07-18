package org.motechproject.whp.mtraining.reports;

import org.motechproject.whp.mtraining.dto.ContentIdentifierDto;
import org.motechproject.whp.mtraining.dto.CourseDto;
import org.motechproject.whp.mtraining.service.CourseService;
import org.motechproject.whp.mtraining.domain.Course;
import org.motechproject.whp.mtraining.repository.AllCourses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CourseReporter {

    private final CourseService courseService;
    private final AllCourses allCourses;

    @Autowired
    public CourseReporter(CourseService courseService, AllCourses allCourses) {
        this.courseService = courseService;
        this.allCourses = allCourses;
    }

    public void reportCourseAdded(UUID courseId, Integer version) {
        CourseDto course = courseService.getCourse(new ContentIdentifierDto(courseId, version));
        allCourses.add(new Course(course));
    }

}
