package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.whp.mtraining.constants.MTrainingEventConstants;
import org.motechproject.whp.mtraining.domain.Course;
import org.motechproject.whp.mtraining.domain.Module;
import org.motechproject.whp.mtraining.domain.Node;
import org.motechproject.whp.mtraining.dto.ContentDto;
import org.motechproject.whp.mtraining.dto.CourseDto;
import org.motechproject.whp.mtraining.exception.CourseStructureValidationException;
import org.motechproject.whp.mtraining.repository.AllCourses;
import org.motechproject.whp.mtraining.validator.CourseStructureValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static org.motechproject.whp.mtraining.domain.NodeType.MODULE;

/**
 * Implementation of abstract class {@link NodeHandler}.
 * Validates, saves and raises an event for a node of type {@link org.motechproject.whp.mtraining.domain.NodeType#COURSE}
 */

@Component
public class CourseNodeHandler extends NodeHandler {

    private static Logger logger = LoggerFactory.getLogger(CourseNodeHandler.class);

    @Autowired
    private AllCourses allCourses;


    @Override
    protected void validateNodeData(ContentDto nodeData) {
        CourseDto courseDto = (CourseDto) nodeData;
        CourseStructureValidationResponse validationResponse = validator().validateCourse(courseDto);
        if (!validationResponse.isValid()) {
            String message = String.format("Invalid course: %s", validationResponse.getErrorMessage());
            logger.error(message);
            throw new CourseStructureValidationException(message);
        }
    }

    @Override
    protected Course saveAndRaiseEvent(Node node) {
        CourseDto courseDto = (CourseDto) node.getNodeData();

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Saving course: %s", courseDto.getName()));
        }

        Course course = getCourse(courseDto, getModules(node));
        allCourses.add(course);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Raising event for saved course: %s", course.getContentId()));
        }

        sendEvent(MTrainingEventConstants.COURSE_CREATION_EVENT, course.getContentId(), course.getVersion());
        return course;
    }

    private List<Module> getModules(Node node) {
        return getChildContentNodes(node, MODULE);
    }

    private Course getCourse(CourseDto courseDto, List<Module> modules) {
        UUID contentId = courseDto.getContentId();
        if (contentId == null) {
            return new Course(courseDto);
        }

        Course existingCourse = allCourses.getLatestVersionByContentId(contentId);
        Course courseToSave = new Course(existingCourse.getName(), existingCourse.getContentId(), existingCourse.getVersion(),
                courseDto.getDescription(), courseDto.getExternalContentId(), courseDto.getCreatedBy(), null, modules, courseDto.isActive());
        courseToSave.incrementVersion();
        return courseToSave;
    }

}
