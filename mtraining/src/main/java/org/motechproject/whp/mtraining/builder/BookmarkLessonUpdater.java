package org.motechproject.whp.mtraining.builder;

import org.motechproject.mtraining.domain.Bookmark;
import org.motechproject.mtraining.domain.Chapter;
import org.motechproject.mtraining.domain.Course;
import org.motechproject.mtraining.domain.Lesson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Updater to re-validate and set the message in a Bookmark against the provided Course Structure for a given enrollee.
 * In cases where a valid message cannot be set, build the Bookmark with a valid Quiz.
 * @see org.motechproject.whp.mtraining.builder.BookmarkBuilder
 */

@Component
public class BookmarkLessonUpdater {

    private BookmarkBuilder courseBookmarkBuilder;

    @Autowired
    public BookmarkLessonUpdater(BookmarkBuilder bookmarkBuilder) {
        this.courseBookmarkBuilder = bookmarkBuilder;
    }


    /**
     * Given bookmark the API ensures that the current bookmark is valid and if not then updates the bookmark to a valid point
     * 1) If bookmark message does not exist, then build bookmark from the chapter
     * 2) If message is not active then set bookmark to next active message
     * 3) If no active message is left in the chapter then build bookmark from chapter quiz
     * 4) If no active quiz is left in the chapter then build bookmark from next active chapter
     * 5) If no next active chapter is left in the module then build bookmark from next active module
     * 5) If no next active module is left in the course then build course completion bookmark
     * @param bookmark
     * @param course
     * @param chapter
     * @return
     */
    public Bookmark update(Bookmark bookmark, Course course, Chapter chapter) {
        Lesson lesson = chapter.getLessons().get(Integer.parseInt(bookmark.getLessonIdentifier()));
        String externalId = bookmark.getExternalId();
        if (lesson == null) {
            return courseBookmarkBuilder.buildBookmarkFromFirstActiveContent(externalId, course, chapter);
        }
        //if (!lesson.isActive()) {
        if (lesson != null) {
            //Lesson nextActiveLesson = chapter.getNextActiveLessonAfter((int)lesson.getId());
            Lesson nextActiveLesson = chapter.getLessons().get(0);
            if (nextActiveLesson != null) {
                return courseBookmarkBuilder.buildBookmarkFrom(externalId, course, chapter, nextActiveLesson);
            }

            //if (chapter.hasActiveQuiz()) {
            //    return courseBookmarkBuilder.buildBookmarkFrom(externalId, course, chapter, chapter.getQuiz());
            //}

            //Chapter nextActiveChapterDto = course.getNextActiveChapterAfter((int)chapter.getId());
            Chapter nextActiveChapterDto = course.getChapters().get(0);
            if (nextActiveChapterDto != null) {
                return courseBookmarkBuilder.buildBookmarkFromFirstActiveContent(externalId, course, nextActiveChapterDto);
            }

            return courseBookmarkBuilder.buildCourseCompletionBookmark(externalId, course);
        }
        return courseBookmarkBuilder.buildBookmarkFrom(externalId, course, chapter, lesson, bookmark.getModificationDate());
    }

}
