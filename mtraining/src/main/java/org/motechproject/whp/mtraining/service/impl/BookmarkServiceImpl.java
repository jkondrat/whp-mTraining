package org.motechproject.whp.mtraining.service.impl;

import org.joda.time.DateTime;
import org.motechproject.whp.mtraining.builder.BookmarkBuilder;
import org.motechproject.whp.mtraining.domain.Bookmark;
import org.motechproject.whp.mtraining.domain.ContentIdentifier;
import org.motechproject.whp.mtraining.dto.*;
import org.motechproject.whp.mtraining.exception.CourseNotFoundException;
import org.motechproject.whp.mtraining.exception.InvalidBookmarkException;
import org.motechproject.whp.mtraining.repository.AllBookmarks;
import org.motechproject.whp.mtraining.service.BookmarkService;
import org.motechproject.whp.mtraining.service.CourseService;
import org.motechproject.whp.mtraining.util.ISODateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation class for {@link org.motechproject.mtraining.service.BookmarkService}.
 * Given an external Id, it finds the current {@link org.motechproject.mtraining.domain.Bookmark} and
 * also constructs a {@link org.motechproject.mtraining.domain.Bookmark} given the course details
 */

@Service("bookmarkService")
public class BookmarkServiceImpl implements BookmarkService {

    private static final Logger LOGGER = LoggerFactory.getLogger(org.motechproject.mtraining.service.impl.BookmarkServiceImpl.class);

    private AllBookmarks allBookmarks;
    private CourseService courseService;
    private BookmarkBuilder bookmarkBuilder;

    @Autowired
    public BookmarkServiceImpl(AllBookmarks allBookmarks, CourseService courseService, BookmarkBuilder bookmarkBuilder) {
        this.allBookmarks = allBookmarks;
        this.courseService = courseService;
        this.bookmarkBuilder = bookmarkBuilder;
    }

    /**
     * Return bookmark given externalId ( enolleeId or student rollNumber etc)
     * Return null if none found
     * If found,then get the latest course (published and latest version) referenced by the bookmark courseId and create a BookmarkDTO out of it.
     * Return null if course referenced by bookmark not found.
     * @param externalId
     */
    @Override
    public BookmarkDto getBookmark(String externalId) {
        Bookmark bookmark = allBookmarks.findBy(externalId);
        if (bookmark == null) {
            return null;
        }
        CourseDto latestPublishedCourse = courseService.getLatestPublishedCourse(bookmark.getCourse().getContentId());
        if (latestPublishedCourse == null) {
            return null;
        }
        return new BookmarkDto(bookmark.getExternalId(), createContentIdentifierDto(bookmark.getCourse()),
                createContentIdentifierDto(bookmark.getModule()), createContentIdentifierDto(bookmark.getChapter()),
                createContentIdentifierDto(bookmark.getMessage()), createContentIdentifierDto(bookmark.getQuiz()), bookmark.getDateModified());
    }

    /**
     * Given a course identifier,return the first bookmark from first active content of the course
     * If course not found then throw CourseNotFoundException
     *
     * @param externalId
     * @param courseIdentifier
     * @return
     */
    @Override
    public BookmarkDto getInitialBookmark(String externalId, ContentIdentifierDto courseIdentifier) {
        LOGGER.info(String.format("Request for adding bookmark for externalId %s and courseId %s ", externalId, courseIdentifier.getContentId()));
        CourseDto course = courseService.getLatestPublishedCourse(courseIdentifier.getContentId());
        if (course == null) {
            throw new CourseNotFoundException();
        }
        BookmarkDto bookmarkDto = bookmarkBuilder.buildBookmarkFromFirstActiveContent(externalId, course);
        LOGGER.debug(String.format("created initial bookmark for externalId %s %s ", externalId, bookmarkDto));
        return bookmarkDto;
    }

    /**
     * Add or update the bookmark
     * The BookmarkDTO is the payload.
     * Create/update a bookmark from the dto
     *
     * @param bookmarkDto
     * @return
     */
    @Override
    public Boolean addOrUpdate(BookmarkDto bookmarkDto) {
        if (!bookmarkDto.isValid()) {
            throw new InvalidBookmarkException("Invalid Bookmark");
        }
        Bookmark bookmarkFromDb = allBookmarks.findBy(bookmarkDto.getExternalId());
        if (bookmarkFromDb == null) {
            LOGGER.info(String.format("Request for bookmark update failed for externalId %s as no bookmark exists for this id.Hence adding this bookmark", bookmarkDto.getExternalId()));
            allBookmarks.add(toBookmark(bookmarkDto));
            return true;
        }
        if (bookmarkFromDb.wasModifiedAfter(ISODateTimeUtil.parseWithTimeZoneUTC(bookmarkDto.getDateModified()))) {
            LOGGER.info(String.format("Request for bookmark update ignored for externalId %s as more recent bookmark exists for this id", bookmarkDto.getExternalId()));
            return false;
        }
        bookmarkFromDb.update(createContentIdentifier(bookmarkDto.getCourse()),
                createContentIdentifier(bookmarkDto.getModule()),
                createContentIdentifier(bookmarkDto.getChapter()),
                createContentIdentifier(bookmarkDto.getMessage()),
                createContentIdentifier(bookmarkDto.getQuiz()),
                ISODateTimeUtil.parseWithTimeZoneUTC(bookmarkDto.getDateModified()));
        allBookmarks.update(bookmarkFromDb);
        LOGGER.debug(String.format("Bookmark updates for externalId %s with bookmark %s ", bookmarkFromDb.getExternalId(), bookmarkFromDb));
        return true;
    }

    /**
     * Given path up to chapter of a course,return a bookmark pointing to the quiz.
     * Look into @BookmarkBuilder for more details which does the actual work.
     *
     * @param externalId
     * @param courseIdentifierDto
     * @param moduleIdentifierDto
     * @param chapterIdentifierDto
     * @return
     */
    @Override
    public BookmarkDto getBookmarkForQuizOfAChapter(String externalId, ContentIdentifierDto courseIdentifierDto, ContentIdentifierDto moduleIdentifierDto, ContentIdentifierDto chapterIdentifierDto) {
        CourseDto courseDto = courseService.getCourse(courseIdentifierDto);
        ModuleDto moduleDto = courseDto.getModule(moduleIdentifierDto.getContentId());
        ChapterDto chapterDto = moduleDto.getChapter(chapterIdentifierDto.getContentId());
        QuizDto quizDto = chapterDto.getQuiz();
        BookmarkDto bookmarkDto = bookmarkBuilder.buildBookmarkFrom(externalId, courseDto, moduleDto,
                chapterDto, quizDto);
        return bookmarkDto;
    }

    /**
     * Set bookmark to first active content (this can be either a message or quiz) of given course,module,chapter
     * Look into @BookmarkBuilder for more details which does the actual work.
     *
     * @param externalId
     * @param courseIdentifierDto
     * @param moduleIdentifierDto
     * @param chapterIdentifierDto
     */
    @Override
    public void setBookmarkToFirstActiveContentOfAChapter(String externalId, ContentIdentifierDto courseIdentifierDto, ContentIdentifierDto moduleIdentifierDto, ContentIdentifierDto chapterIdentifierDto) {
        CourseDto courseDto = courseService.getCourse(courseIdentifierDto);
        ModuleDto moduleDto = courseDto.getModule(moduleIdentifierDto.getContentId());
        ChapterDto chapterDto = moduleDto.getChapter(chapterIdentifierDto.getContentId());

        BookmarkDto bookmarkDto = bookmarkBuilder.buildBookmarkFromFirstActiveContent(externalId, courseDto, moduleDto,
                chapterDto);
        addOrUpdate(bookmarkDto);
    }

    /**
     * Given complete path to chapter of a given module of a given course,create bookmark to point to first active content(message or quiz) after the chapter.
     * For example, lets say the argument is ch001 and the next chapter is ch002 (which is inactive) and then ch003(which is active),
     * then this API will return a bookmark that points to the next active message or quiz in ch003
     * In case the current module does not have any active chapter left,try the next module
     * In case no active module left return a course completion bookmark.
     * @param externalId
     * @param courseIdentifierDto
     * @param moduleIdentifierDto
     * @param chapterIdentifierDto
     * @return
     */
    @Override
    public BookmarkDto getNextBookmark(String externalId, ContentIdentifierDto courseIdentifierDto, ContentIdentifierDto moduleIdentifierDto, ContentIdentifierDto chapterIdentifierDto) {
        CourseDto courseDto = courseService.getCourse(courseIdentifierDto);
        ModuleDto moduleDto = courseDto.getModule(moduleIdentifierDto.getContentId());
        ChapterDto chapterDto = moduleDto.getChapter(chapterIdentifierDto.getContentId());

        BookmarkDto bookmarkDto = null;

        ChapterDto nextActiveChapterAfterGivenChapter = moduleDto.getNextActiveChapterAfter(chapterDto.getContentId());

        if (nextActiveChapterAfterGivenChapter != null) {
            bookmarkDto = bookmarkBuilder.buildBookmarkFromFirstActiveContent(externalId, courseDto, moduleDto, nextActiveChapterAfterGivenChapter);
        }

        if (bookmarkDto == null) {
            ModuleDto nextActiveModuleAfterGivenModule = courseDto.getNextActiveModuleAfter(moduleDto.getContentId());
            bookmarkDto = bookmarkBuilder.buildBookmarkFromFirstActiveContent(externalId, courseDto, nextActiveModuleAfterGivenModule);
        }

        if (bookmarkDto == null) {
            return bookmarkBuilder.buildCourseCompletionBookmark(externalId, courseDto);
        }
        return bookmarkDto;
    }

    /**
     * Set bookmark to last active content of course
     * @param externalId
     * @param courseIdentifier
     */
    @Override
    public void setBookmarkToLastActiveContentOfACourse(String externalId, ContentIdentifierDto courseIdentifier) {
        CourseDto courseDto = courseService.getCourse(courseIdentifier);
        BookmarkDto bookmarkDto = bookmarkBuilder.buildBookmarkFromLastActiveContent(externalId, courseDto);
        addOrUpdate(bookmarkDto);
    }

    private Bookmark toBookmark(BookmarkDto bookmarkDto) {
        String externalId = bookmarkDto.getExternalId();
        ContentIdentifierDto course = bookmarkDto.getCourse();
        ContentIdentifierDto module = bookmarkDto.getModule();
        ContentIdentifierDto chapter = bookmarkDto.getChapter();
        ContentIdentifierDto message = bookmarkDto.getMessage();
        ContentIdentifierDto quiz = bookmarkDto.getQuiz();
        DateTime dateModified = ISODateTimeUtil.parseWithTimeZoneUTC(bookmarkDto.getDateModified());
        return new Bookmark(externalId, createContentIdentifier(course), createContentIdentifier(module), createContentIdentifier(chapter), createContentIdentifier(message),
                createContentIdentifier(quiz), dateModified);
    }

    private ContentIdentifier createContentIdentifier(ContentIdentifierDto contentIdentifierDto) {
        if (contentIdentifierDto == null) {
            return null;
        }
        return new ContentIdentifier(contentIdentifierDto.getContentId(), contentIdentifierDto.getVersion());
    }

    private ContentIdentifierDto createContentIdentifierDto(ContentIdentifier contentIdentifier) {
        if (contentIdentifier == null) {
            return null;
        }
        return new ContentIdentifierDto(contentIdentifier.getContentId(), contentIdentifier.getVersion());
    }

}
