package org.motechproject.whp.mtraining.repository;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.motechproject.whp.mtraining.domain.Quiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for {@link org.motechproject.mtraining.domain.Quiz} couch document
 */

@Repository
public class AllQuizes extends AllContents<Quiz> {

    @Autowired
    public AllQuizes(@Qualifier("mtrainingDbConnector") CouchDbConnector db) {
        super(Quiz.class, db);
        initStandardDesignDocument();
    }

    @View(name = "by_contentId_and_version", map = "function(doc) { if (doc.type ==='Quiz') { emit([doc.contentId,doc.version], doc._id); }}")
    public Quiz findBy(UUID contentId, Integer version) {
        return queryContentView("by_contentId_and_version", contentId, version);
    }
}

