package org.motechproject.whp.mtraining.repository;

import org.ektorp.CouchDbConnector;
import org.ektorp.support.View;
import org.motechproject.whp.mtraining.domain.Module;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for {@link Module} couch document
 */

@Repository
public class AllModules extends AllContents<Module> {

    @Autowired
    public AllModules(@Qualifier("mtrainingDbConnector") CouchDbConnector db) {
        super(Module.class, db);
        initStandardDesignDocument();
    }

    @View(name = "by_contentId_and_version", map = "function(doc) { if (doc.type ==='Module') { emit([doc.contentId,doc.version], doc._id); }}")
    public Module findBy(UUID contentId, Integer version) {
        return queryContentView("by_contentId_and_version", contentId, version);
    }

}
