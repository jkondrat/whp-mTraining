package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.whp.mtraining.builder.CourseProgressUpdater;
import org.motechproject.whp.mtraining.constants.CourseStatus;
import org.motechproject.whp.mtraining.domain.ContentIdentifier;
import org.motechproject.whp.mtraining.domain.CourseConfiguration;
import org.motechproject.whp.mtraining.domain.EnrolleeCourseProgress;
import org.motechproject.whp.mtraining.dto.BookmarkDto;
import org.motechproject.whp.mtraining.dto.ContentIdentifierDto;
import org.motechproject.whp.mtraining.dto.EnrolleeCourseProgressDto;
import org.motechproject.whp.mtraining.repository.AllCourseConfigurations;
import org.motechproject.whp.mtraining.repository.AllEnrolleeCourseProgress;
import org.motechproject.whp.mtraining.service.CourseProgressService;
import org.motechproject.whp.mtraining.service.impl.BookmarkServiceImpl;
import org.motechproject.whp.mtraining.util.ISODateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Implementation class for {@link org.motechproject.mtraining.service.CourseProgressService}.
 * Given an external Id, it finds the current {@link org.motechproject.mtraining.domain.Bookmark} and
 * {@link org.motechproject.mtraining.domain.EnrolleeCourseProgress}
 * also constructs a {@link org.motechproject.mtraining.domain.EnrolleeCourseProgress} given the course details
 */

@Service("courseProgressService")
public class CourseProgressServiceImpl implements CourseProgressService {
    private static final Logger LOGGER = LoggerFactory.getLogger(org.motechproject.mtraining.service.impl.BookmarkServiceImpl.class);

    private org.motechproject.whp.mtraining.service.impl.BookmarkServiceImpl bookmarkService;
    private AllEnrolleeCourseProgress allEnrolleeCourseProgress;
    private CourseProgressUpdater courseProgressUpdater;
    private AllCourseConfigurations allCourseConfigs;

    @Autowired
    public CourseProgressServiceImpl(BookmarkServiceImpl bookmarkService, AllEnrolleeCourseProgress allEnrolleeCourseProgress, CourseProgressUpdater courseProgressUpdater, AllCourseConfigurations allCourseConfigs) {
        this.bookmarkService = bookmarkService;
        this.allEnrolleeCourseProgress = allEnrolleeCourseProgress;
        this.courseProgressUpdater = courseProgressUpdater;
        this.allCourseConfigs = allCourseConfigs;
    }

    /**
     * Return the course progress dto for the given courseId for the enrollee.
     * This API makes the assumption that at a given point of time an enrollee will be doing one course only.
     * The courseProgressDTO contains information about course progress as well as the current bookmark.
     * If the enrollee has course progress information for only courses that has been successfully completed by enrollee and certification released i.e the course
     * has been 'CLOSED' with respect to the enrollee,then null is returned.
     * Please have a look at @CourseStatus for more information.
     * @param externalId
     * @param courseContentId
     * @return CourseProgressDto
     */
    @Override
    public EnrolleeCourseProgressDto getCourseProgressForEnrollee(String externalId, UUID courseContentId) {
        EnrolleeCourseProgress enrolleeCourseProgress = allEnrolleeCourseProgress.findBy(externalId, courseContentId);
        if (enrolleeCourseProgress != null) {
            BookmarkDto bookmarkDto = bookmarkService.getBookmark(externalId);
            if (bookmarkDto == null) {
                return null;
            }
            EnrolleeCourseProgressDto enrolleeCourseProgressDto = new EnrolleeCourseProgressDto(externalId, enrolleeCourseProgress.getCourseStartTime(), bookmarkDto, enrolleeCourseProgress.getCourseStatus());
            if (enrolleeCourseProgress.isCourseClosed()) {
                return enrolleeCourseProgressDto;
            }
            setTimeLeftToCompleteCourse(bookmarkDto.getCourse().getContentId(), enrolleeCourseProgressDto);
            return courseProgressUpdater.update(enrolleeCourseProgressDto);
        }
        return null;
    }

    /**
     * Returns the initial course progress DTO for a given course.
     * The DTO also contains the current bookmark
     *
     * @param externalId
     * @param courseIdentifier
     * @return CourseProgressDto
     */
    @Override
    public EnrolleeCourseProgressDto getInitialCourseProgressForEnrollee(String externalId, ContentIdentifierDto courseIdentifier) {
        BookmarkDto bookmarkDto = bookmarkService.getInitialBookmark(externalId, courseIdentifier);
        EnrolleeCourseProgressDto enrolleeCourseProgressDto = new EnrolleeCourseProgressDto(externalId, null, bookmarkDto, CourseStatus.STARTED);
        setTimeLeftToCompleteCourse(bookmarkDto.getCourse().getContentId(), enrolleeCourseProgressDto);
        return enrolleeCourseProgressDto;
    }

    /**
     * Add or update course progress DTO.
     * This API abstracts two operations, one of updating the current bookmark and the other of updating the course progress
     * @param enrolleeCourseProgressDto
     */
    @Override
    public void addOrUpdateCourseProgress(EnrolleeCourseProgressDto enrolleeCourseProgressDto) {
        ContentIdentifierDto course = enrolleeCourseProgressDto.getCourse();
        EnrolleeCourseProgress enrolleeCourseProgressFromDb = allEnrolleeCourseProgress.findBy(enrolleeCourseProgressDto.getExternalId(), course.getContentId());
        Boolean bookmarkUpdated = bookmarkService.addOrUpdate(enrolleeCourseProgressDto.getBookmarkDto());
        if (!bookmarkUpdated) {
            return;
        }
        if (enrolleeCourseProgressFromDb == null) {
            LOGGER.info(String.format("Request for bookmark update failed for externalId %s as no bookmark exists for this id.Hence adding this bookmark", enrolleeCourseProgressDto.getExternalId()));
            EnrolleeCourseProgress enrolleeCourseProgress = new EnrolleeCourseProgress(enrolleeCourseProgressDto.getExternalId(), ISODateTimeUtil.parseWithTimeZoneUTC(enrolleeCourseProgressDto.getCourseStartTime()),
                    enrolleeCourseProgressDto.getCourseStatus(), course.getContentId());
            allEnrolleeCourseProgress.add(enrolleeCourseProgress);
            return;
        }
        enrolleeCourseProgressFromDb.update(enrolleeCourseProgressDto.getExternalId(), ISODateTimeUtil.parseWithTimeZoneUTC(enrolleeCourseProgressDto.getCourseStartTime()), enrolleeCourseProgressDto.getCourseStatus());
        allEnrolleeCourseProgress.update(enrolleeCourseProgressFromDb);
    }


    /**
     * Mark a given course as complete.This implies marking the course status as 'COMPLETE'
     * Also update the bookmark to point to the last active content of the course
     * @param externalId
     * @param startTime
     * @param courseIdentifier
     */
    @Override
    public void markCourseAsComplete(String externalId, String startTime, ContentIdentifierDto courseIdentifier) {
        ContentIdentifier course = toContentIdentifier(courseIdentifier);
        EnrolleeCourseProgress enrolleeCourseProgressFromDb = allEnrolleeCourseProgress.findBy(externalId, course.getContentId());
        if (enrolleeCourseProgressFromDb != null) {
            bookmarkService.setBookmarkToLastActiveContentOfACourse(externalId, courseIdentifier);
            enrolleeCourseProgressFromDb.markComplete();
            allEnrolleeCourseProgress.update(enrolleeCourseProgressFromDb);
            return;
        }

        EnrolleeCourseProgress enrolleeCourseProgress = new EnrolleeCourseProgress(externalId, ISODateTimeUtil.parseWithTimeZoneUTC(startTime), CourseStatus.COMPLETED, course.getContentId());
        allEnrolleeCourseProgress.add(enrolleeCourseProgress);
    }

    private void setTimeLeftToCompleteCourse(UUID contentId, EnrolleeCourseProgressDto enrolleeCourseProgressDto) {
        CourseConfiguration courseConfig = allCourseConfigs.findCourseConfigurationFor(contentId);
        if (courseConfig == null) {
            enrolleeCourseProgressDto.setTimeLeftToCompleteCourse();
            return;
        }
        enrolleeCourseProgressDto.setTimeLeftToCompleteCourseInHrs(courseConfig.getDurationInDays());
    }

    private ContentIdentifier toContentIdentifier(ContentIdentifierDto contentIdentifierDto) {
        return new ContentIdentifier(contentIdentifierDto.getContentId(), contentIdentifierDto.getVersion());
    }
}

