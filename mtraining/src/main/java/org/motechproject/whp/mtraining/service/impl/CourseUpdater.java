package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.whp.mtraining.dto.CourseDto;
import org.motechproject.whp.mtraining.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CourseUpdater extends Updater<CourseDto> {

    private CourseService courseService;
    private ModuleUpdater moduleUpdater;
    private List<CourseDto> existingCourses;

    @Autowired
    public CourseUpdater(CourseService courseService, ModuleUpdater moduleUpdater) {
        this.courseService = courseService;
        this.moduleUpdater = moduleUpdater;
        this.existingCourses = new ArrayList<>();
    }

    @Override
    protected void updateContentId(CourseDto courseDto, CourseDto existingContentByName) {
        courseDto.setContentId(existingContentByName.getContentId());
    }

    @Override
    protected void updateChildContents(CourseDto courseDto) {
        moduleUpdater.update(courseDto.getModules());
    }

    @Override
    protected List<CourseDto> getExistingContents() {
        if (existingCourses.isEmpty()) {
            existingCourses = courseService.getAllCourses();
        }
        return existingCourses;
    }

    @Override
    public void update(List<CourseDto> contentsToUpdate) {
        super.update(contentsToUpdate);
        invalidateCache();
    }

    @Override
    protected boolean isEqual(CourseDto courseDto1, CourseDto courseDto2) {
        return courseDto1.getName().equalsIgnoreCase(courseDto2.getName());
    }

    private void invalidateCache() {
        existingCourses.clear();
        moduleUpdater.invalidateCache();
    }
}
