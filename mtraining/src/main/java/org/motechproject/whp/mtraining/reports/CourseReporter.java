package org.motechproject.whp.mtraining.reports;

import org.motechproject.mtraining.domain.Course;
import org.motechproject.mtraining.service.MTrainingService;
import org.motechproject.whp.mtraining.repository.AllCourses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CourseReporter {

    private final MTrainingService courseService;
    private final AllCourses allCourses;

    @Autowired
    public CourseReporter(MTrainingService courseService, AllCourses allCourses) {
        this.courseService = courseService;
        this.allCourses = allCourses;
    }

    public void reportCourseAdded(long courseId, Integer version) {
        Course course = courseService.getCourseById(courseId);
       // allCourses.add(new Course(course));
    }

}
