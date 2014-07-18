package org.motechproject.whp.mtraining.repository;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.motechproject.whp.mtraining.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for {@link Message} couch document
 */

@Repository
public class AllMessages extends AllContents<Message> {

    @Autowired
    public AllMessages(@Qualifier("mtrainingDbConnector") CouchDbConnector db) {
        super(Message.class, db);
        initStandardDesignDocument();
    }

    @View(name = "by_contentId_and_version", map = "function(doc) { if (doc.type ==='Message') { emit([doc.contentId,doc.version], doc._id); }}")
    public Message findBy(UUID contentId, Integer version) {
        return queryContentView("by_contentId_and_version", contentId, version);
    }
}

