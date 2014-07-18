package org.motechproject.whp.mtraining.repository;

import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.motechproject.whp.mtraining.domain.ContentVersionComparator;
import org.motechproject.whp.mtraining.domain.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link Course} couch document
 */

@Repository
public class AllCourses extends AllContents<Course> {

    @Autowired
    public AllCourses(@Qualifier("mtrainingDbConnector") CouchDbConnector db) {
        super(Course.class, db);
        initStandardDesignDocument();
    }

    @View(name = "by_contentId_and_version", map = "function(doc) { if (doc.type ==='Course') { emit([doc.contentId,doc.version], doc._id); }}")
    public Course findBy(UUID contentId, Integer version) {
        return queryContentView("by_contentId_and_version", contentId, version);
    }

    @View(name = "by_contentId_and_published", map = "function(doc) { if (doc.type ==='Course') { emit([doc.contentId,doc.isPublished,doc.isActive], doc._id); }}")
    public List<Course> findAllPublishedBy(UUID contentId) {
        if (contentId == null) {
            return null;
        }
        return queryView("by_contentId_and_published", ComplexKey.of(contentId, true, true));
    }

    @View(name = "by_name", map = "function(doc) { if (doc.type ==='Course') { emit(doc.name, doc._id); }}")
    public Course findByName(String name) {
        if (name == null) {
            return null;
        }
        return singleResult(queryView("by_name", name));
    }

    /**
     * Returns latest published course (published course should be active as well)
     * @param courseId
     * @return
     */
    public Course findLatestPublishedCourse(UUID courseId) {
        List<Course> publishedCourses = findAllPublishedBy(courseId);
        if (publishedCourses.isEmpty()) {
            return null;
        }
        return Collections.max(publishedCourses, new ContentVersionComparator());
    }

}
