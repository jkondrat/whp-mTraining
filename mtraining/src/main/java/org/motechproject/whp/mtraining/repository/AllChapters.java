package org.motechproject.whp.mtraining.repository;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.motechproject.whp.mtraining.domain.Chapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for {@link org.motechproject.mtraining.domain.Chapter} couch document
 */

@Repository
public class AllChapters extends AllContents<Chapter> {

    @Autowired
    public AllChapters(@Qualifier("mtrainingDbConnector") CouchDbConnector db) {
        super(Chapter.class, db);
        initStandardDesignDocument();
    }

    @View(name = "by_contentId_and_version", map = "function(doc) { if (doc.type ==='Chapter') { emit([doc.contentId,doc.version], doc._id); }}")
    public Chapter findBy(UUID contentId, Integer version) {
        return queryContentView("by_contentId_and_version", contentId, version);
    }
}
