package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.whp.mtraining.domain.*;
import org.motechproject.whp.mtraining.domain.Node;
import org.motechproject.whp.mtraining.dto.ContentIdentifierDto;
import org.motechproject.whp.mtraining.dto.CourseDto;
import org.motechproject.whp.mtraining.dto.ModuleDto;
import org.motechproject.whp.mtraining.exception.CourseNotFoundException;
import org.motechproject.whp.mtraining.exception.CoursePublicationException;
import org.motechproject.whp.mtraining.repository.AllCourses;
import org.motechproject.whp.mtraining.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation class for {@link CourseService}.
 * Given a content DTO to add, it constructs a tree structured generic {@link Node}
 * and uses {@link CourseServiceImpl#nodeHandlerOrchestrator} to process the node
 */

@Service("courseService")
public class CourseServiceImpl implements CourseService {

    private NodeHandlerOrchestrator nodeHandlerOrchestrator;
    private ModuleServiceImpl moduleService;
    private AllCourses allCourses;

    @Autowired
    public CourseServiceImpl(NodeHandlerOrchestrator nodeHandlerOrchestrator, ModuleServiceImpl moduleService, AllCourses allCourses) {
        this.nodeHandlerOrchestrator = nodeHandlerOrchestrator;
        this.moduleService = moduleService;
        this.allCourses = allCourses;
    }

    @Override
    public ContentIdentifierDto addOrUpdateCourse(CourseDto courseDto) {
        Node courseNode = constructCourseNode(courseDto);
        nodeHandlerOrchestrator.process(courseNode);
        return getContentIdentifier(courseNode);
    }

    @Override
    public CourseDto getCourse(ContentIdentifierDto courseIdentifier) {
        Course course = allCourses.findBy(courseIdentifier.getContentId(), courseIdentifier.getVersion());
        return course != null ? mapToCourseDto(course) : null;
    }

    @Override
    public List<CourseDto> getAllCourses() {
        List<Course> courses = allCourses.getAll();
        List<CourseDto> courseDtoList = new ArrayList<>();
        for (Course course : courses) {
            courseDtoList.add(mapToCourseDto(course));
        }
        return courseDtoList;
    }

    /**
     * Get latest by version course which is published and active
     * @param contentId
     * @return
     */
    @Override
    public CourseDto getLatestPublishedCourse(UUID contentId) {
        Course course = allCourses.findLatestPublishedCourse(contentId);
        if (course == null) {
            return null;
        }
        return mapToCourseDto(course);
    }

    /**
     * Mark course with given course identifier as published.
     * If course is not found then throws CourseNotFoundException
     * If course is not active then throws CoursePublicationException
     * @param courseIdentifier
     */
    @Override
    public void publish(ContentIdentifierDto courseIdentifier) {
        Course course = allCourses.findBy(courseIdentifier.getContentId(), courseIdentifier.getVersion());
        if (course == null) {
            throw new CourseNotFoundException(String.format("Course with contentId %s and version %s not found",
                    courseIdentifier.getContentId(), courseIdentifier.getVersion()));
        }
        if (!course.isActive()) {
            throw new CoursePublicationException(String.format("Course with id %s and version %s could not be published as it is inactive",
                    courseIdentifier.getContentId(), courseIdentifier.getVersion()));
        }
        course.publish();
        allCourses.update(course);
    }

    private Node constructCourseNode(CourseDto courseDto) {
        List<Node> moduleNodes = moduleService.constructModuleNodes(courseDto.getModules());
        return new Node(NodeType.COURSE, courseDto, moduleNodes);
    }

    private ContentIdentifierDto getContentIdentifier(Node node) {
        Content savedContent = node.getPersistentEntity();
        return savedContent != null ? new ContentIdentifierDto(savedContent.getContentId(), savedContent.getVersion()) : null;
    }

    private CourseDto mapToCourseDto(Course course) {
        ArrayList<ModuleDto> modules = new ArrayList<>();
        for (Content module : course.getModules()) {
            ModuleDto moduleDto = moduleService.mapToModuleDto((Module) module);
            modules.add(moduleDto);
        }
        return new CourseDto(course.getContentId(), course.getVersion(), course.isActive(), course.getName(), course.getDescription(),
                course.getExternalContentId(), course.getCreatedBy(), modules);
    }
}
