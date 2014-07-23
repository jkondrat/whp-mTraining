package org.motechproject.whp.mtraining.domain;

import org.joda.time.DateTime;
import org.motechproject.whp.mtraining.dto.CourseDto;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@PersistenceCapable(table = "course", identityType = IdentityType.APPLICATION, detachable = "true")
public class Course extends CourseContent {

    @Element(column = "course_id")
    @Order(column = "module_order")
    @Persistent
    private String description;
    @Persistent(column = "audio_file_name")
    private String externalId;


    public Course(String name, UUID courseId, Integer version, String description, String externalId, String createdBy, DateTime createdOn,boolean isActive) {
        super(name, courseId, version, createdBy, createdOn, isActive);
        this.description = description;
        this.externalId = externalId;
    }

    public Course(CourseDto courseDto) {
        this(courseDto.getName(), courseDto.getContentId(), courseDto.getVersion(), courseDto.getDescription(), courseDto.getExternalContentId(), courseDto.getCreatedBy(), courseDto.getCreatedOn(),
                courseDto.isActive());
    }

    public String getDescription() {
        return description;
    }

    public String getExternalId() {
        return externalId;
    }

}
