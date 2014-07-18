package org.motechproject.whp.mtraining.service;

import org.motechproject.whp.mtraining.dto.BookmarkDto;
import org.motechproject.whp.mtraining.dto.ContentIdentifierDto;

/**
 * Service Interface that exposes APIs to manage bookmarks of enrollee
 */
public interface BookmarkService {

    /**
     * The bookmark returned will have the latest version for the given courseId
     * The updates updates the bookmark in memory and does not write any changes to database.
     * Assumption here is that the post bookmark will post the correct bookmark.
     *
     * @param externalId
     */
    BookmarkDto getBookmark(String externalId);

    /**
     * Returns the first bookmark for a given course for a enrollee (specified by the externalId)
     * Internally calls the bookmark builder which has the logic and know how to create the first bookmark for a given course
     *
     * @param externalId
     * @param courseIdentifier
     */
    BookmarkDto getInitialBookmark(String externalId, ContentIdentifierDto courseIdentifier);

    /**
     * Given a bookmarkDto, adds a bookmark if bookmark for the enrollee does not exist,otherwise updates the bookmark
     *
     * @param bookmark
     */
    Boolean addOrUpdate(BookmarkDto bookmark);

    /**
     * Returns bookmark to a quiz, given the chapterId.All the parents are also required as bookmark needs to point to the complete path to quiz within the given course.
     *
     * @param externalId
     * @param courseIdentifierDto
     * @param moduleIdentifierDto
     * @param chapterIdentifierDto
     * @return
     */
    BookmarkDto getBookmarkForQuizOfAChapter(String externalId, ContentIdentifierDto courseIdentifierDto, ContentIdentifierDto moduleIdentifierDto, ContentIdentifierDto chapterIdentifierDto);

    /**
     * Sets an enrollee's bookmark to first active content of a given chapter
     *
     * @param externalId
     * @param courseIdentifierDto
     * @param moduleIdentifierDto
     * @param chapterIdentifierDto
     */
    void setBookmarkToFirstActiveContentOfAChapter(String externalId, ContentIdentifierDto courseIdentifierDto, ContentIdentifierDto moduleIdentifierDto, ContentIdentifierDto chapterIdentifierDto);

    /**
     * Given the path pointing to chapter contentId for a given enrollee,returns the bookmark with the first active content available starting from the given chapter
     *
     * @param externalId
     * @param courseIdentifierDto
     * @param moduleIdentifierDto
     * @param chapterIdentifierDto
     */
    BookmarkDto getNextBookmark(String externalId, ContentIdentifierDto courseIdentifierDto, ContentIdentifierDto moduleIdentifierDto, ContentIdentifierDto chapterIdentifierDto);

    /**
     *
     * Sets the bookmark to last active content of a given course
     *
     * @param externalId
     * @param courseIdentifier
     */
    void setBookmarkToLastActiveContentOfACourse(String externalId, ContentIdentifierDto courseIdentifier);
}
