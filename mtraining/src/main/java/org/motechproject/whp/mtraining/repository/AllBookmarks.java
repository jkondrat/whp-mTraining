package org.motechproject.whp.mtraining.repository;

import org.ektorp.CouchDbConnector;
import org.ektorp.ViewQuery;
import org.ektorp.support.View;
import org.motechproject.commons.couchdb.dao.MotechBaseRepository;
import org.motechproject.whp.mtraining.domain.Bookmark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link org.motechproject.mtraining.domain.Bookmark} couch document
 */

@Repository
public class AllBookmarks extends MotechBaseRepository<Bookmark> {

    @Autowired
    public AllBookmarks(@Qualifier("mtrainingDbConnector") CouchDbConnector db) {
        super(Bookmark.class, db);
        initStandardDesignDocument();
    }

    @View(name = "by_externalId", map = "function(doc) { if (doc.type ==='Bookmark') { emit(doc.externalId, doc._id); }}")
    public Bookmark findBy(String externalId) {
        if (externalId == null) {
            return null;
        }
        ViewQuery viewQuery = createQuery("by_externalId").key(externalId).includeDocs(true);
        List<Bookmark> resultSet = db.queryView(viewQuery, Bookmark.class);
        return singleResult(resultSet);
    }

    public void deleteBookmarkFor(String externalId) {
        Bookmark bookmark = findBy(externalId);
        remove(bookmark);
    }
}
