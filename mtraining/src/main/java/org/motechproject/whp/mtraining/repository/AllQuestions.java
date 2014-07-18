package org.motechproject.whp.mtraining.repository;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.motechproject.whp.mtraining.domain.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for {@link org.motechproject.mtraining.domain.Question} couch document
 */

@Repository
public class AllQuestions extends AllContents<Question> {

    @Autowired
    public AllQuestions(@Qualifier("mtrainingDbConnector") CouchDbConnector db) {
        super(Question.class, db);
        initStandardDesignDocument();
    }

    @View(name = "by_contentId_and_version", map = "function(doc) { if (doc.type ==='Question') { emit([doc.contentId,doc.version], doc._id); }}")
    public Question findBy(UUID contentId, Integer version) {
        return queryContentView("by_contentId_and_version", contentId, version);
    }
}
