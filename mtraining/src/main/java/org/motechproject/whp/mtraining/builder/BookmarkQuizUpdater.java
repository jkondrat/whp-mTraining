package org.motechproject.whp.mtraining.builder;

import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.domain.Chapter;
import org.motechproject.mtraining.domain.Course;
import org.motechproject.mtraining.domain.Quiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Updater to re-validate and set the quiz in a Bookmark against the provided Course Structure for a given enrollee.
 * Switches Bookmark to next chapter if unable to set a quiz in the Bookmark for the given chapter.
 * @see org.motechproject.whp.mtraining.builder.BookmarkBuilder
 */

@Component
public class BookmarkQuizUpdater {

    private BookmarkBuilder courseBookmarkBuilder;

    @Autowired
    public BookmarkQuizUpdater(BookmarkBuilder bookmarkBuilder) {
        this.courseBookmarkBuilder = bookmarkBuilder;
    }

    /**
     * Given bookmark the API ensures that the current bookmark is valid and if not then updates the bookmark to a valid point
     * 1) If bookmark quiz does not exist, then build bookmark from the chapter
     * 2) If quiz is not active then set bookmark to next active chapter
     * 5) If no next active chapter is left in the module then build bookmark from next active module
     * 5) If no next active module is left in the course then build course completion bookmark
     * @param bookmark
     * @param course
     * @param chapter
     * @return
     */
    public Bookmark update(Bookmark bookmark, Course course, Chapter chapter) {
        Quiz quiz = chapter.getQuiz();
        String externalId = bookmark.getExternalId();

        if (quiz == null) {
            return courseBookmarkBuilder.buildBookmarkFromFirstActiveContent(externalId, course, chapter);
        }

        //if (!quiz.isActive()) {
        if (quiz != null) {
            //Chapter nextActiveChapter = course.getNextActiveChapterAfter(chapter.getId());
            Chapter nextActiveChapter = course.getChapters().get((int)chapter.getId());
            if (nextActiveChapter != null) {
                return courseBookmarkBuilder.buildBookmarkFromFirstActiveContent(externalId, course, nextActiveChapter);
            }
            return courseBookmarkBuilder.buildCourseCompletionBookmark(externalId, course);
        }
        return courseBookmarkBuilder.buildBookmarkFrom(externalId, course, chapter, quiz, bookmark.getModificationDate());
    }

}
