package org.motechproject.whp.mtraining.repository;

import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.support.View;
import org.motechproject.commons.couchdb.dao.MotechBaseRepository;
import org.motechproject.whp.mtraining.domain.CourseConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link org.motechproject.mtraining.domain.CourseConfiguration} couch document
 */

@Repository
public class AllCourseConfigurations extends MotechBaseRepository<CourseConfiguration> {

    @Autowired
    public AllCourseConfigurations(@Qualifier("mtrainingDbConnector") CouchDbConnector db) {
        super(CourseConfiguration.class, db);
        initStandardDesignDocument();
    }

    @View(name = "by_courseId", map = "function(doc) { if (doc.type ==='CourseConfiguration') { emit(doc.courseId, doc._id); }}")
    public CourseConfiguration findCourseConfigurationFor(UUID contentId) {
        if (contentId == null) {
            return null;
        }
        ViewQuery viewQuery = createQuery("by_courseId").key(contentId).includeDocs(true);
        List<CourseConfiguration> resultSet = db.queryView(viewQuery, CourseConfiguration.class);
        return singleResult(resultSet);
    }

}
