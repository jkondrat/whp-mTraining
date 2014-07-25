package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.mtraining.domain.*;
import org.motechproject.whp.mtraining.domain.CourseConfiguration;
import org.motechproject.whp.mtraining.domain.CoursePlan;
import org.motechproject.whp.mtraining.domain.Location;
import org.motechproject.mtraining.service.MTrainingService;
import org.motechproject.security.model.UserDto;
import org.motechproject.security.service.MotechUserService;
import org.motechproject.whp.mtraining.csv.domain.Content;
import org.motechproject.whp.mtraining.csv.request.CourseConfigurationRequest;
import org.motechproject.whp.mtraining.csv.request.CourseCsvRequest;
import org.motechproject.whp.mtraining.service.CourseConfigurationService;
import org.motechproject.whp.mtraining.service.CoursePlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.valueOf;
import static org.apache.commons.lang.StringUtils.isBlank;

@Service
public class CourseImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CourseImportService.class);

    @Autowired
    private CoursePlanService coursePlanService;

    @Autowired
    private CourseConfigurationService courseConfigurationService;

    @Autowired
    private MotechUserService motechUserService;

    public CoursePlan importCoursePlan(List<CourseCsvRequest> requests) {
        CoursePlan coursePlan = formCoursePlan(requests);

        return coursePlanService.updateCoursePlan(coursePlan);
    }

    public void importCourseConfig(List<CourseConfigurationRequest> requests) {
        for (CourseConfigurationRequest request : requests) {
            CourseConfiguration courseConfiguration = new CourseConfiguration(request.getCourseName(),
                   valueOf(request.getCourseDurationInDays()), new Location(request.getBlock(), request.getDistrict(), request.getState()));
            if (courseConfigurationService.getCourseConfigurationById(courseConfiguration.getId()) == null) {
                courseConfigurationService.createCourseConfiguration(courseConfiguration);
            } else {
                courseConfigurationService.updateCourseConfiguration(courseConfiguration);
            }
        }
    }

    private CoursePlan formCoursePlan(List<CourseCsvRequest> requests) {
        CourseCsvRequest courseRequest = requests.get(0);
        CoursePlan coursePlan = new CoursePlan(courseRequest.getNodeName(), CourseUnitState.valueOf(courseRequest.getStatus()), courseRequest.getFileName());

        Map<Course, CourseCsvRequest> courses = new HashMap<>();
        Map<Chapter, CourseCsvRequest> chapters = new HashMap<>();
        Map<Lesson, CourseCsvRequest> lessons = new HashMap<>();
        Map<Question, CourseCsvRequest> questions = new HashMap<>();
        for (CourseCsvRequest request : requests) {
            String type = request.getNodeType();
            if (type.contentEquals("Chapter")) {
                Chapter chapter = new Chapter(request.getNodeName(), CourseUnitState.valueOf(request.getStatus()), request.getFileName(), new ArrayList<Lesson>());
                chapters.put(chapter, request);
            } else if (type.contentEquals("Message") || type.contentEquals("Lesson")) {
                Lesson lesson = new Lesson(request.getNodeName(), CourseUnitState.valueOf(request.getStatus()), request.getFileName());
                lessons.put(lesson, request);
            } else if (type.contentEquals("Question")) {
                Question question = new Question(request.getFileName(), request.getCorrectAnswerFileName());
                questions.put(question, request);
            } else if (type.contentEquals("Module")) {
                Course course = new Course(request.getNodeName(), CourseUnitState.valueOf(request.getStatus()), request.getFileName(), new ArrayList<Chapter>());
                courses.put(course, request);
            }
        }

        for(Map.Entry<Chapter, CourseCsvRequest> chapterMap : chapters.entrySet()) {
            Chapter chapter = chapterMap.getKey();
            CourseCsvRequest chapterRow = chapterMap.getValue();
            for(Map.Entry<Lesson, CourseCsvRequest> lesson : lessons.entrySet()) {
                if (lesson.getValue().getParentNode().contentEquals(chapter.getName())) {
                    chapter.getLessons().add(lesson.getKey());
                }
            }
            String noOfQuizQuestions = chapterRow.getNoOfQuizQuestions();
            Integer numberOfQuizQuestions = isBlank(noOfQuizQuestions) ? 0 : Integer.parseInt(noOfQuizQuestions);
            if(numberOfQuizQuestions > 0) {
                Quiz quiz = new Quiz();
                quiz.setPassPercentage(Double.valueOf(chapterRow.getPassPercentage()));
                quiz.setQuestions(new ArrayList<Question>());
                for(Map.Entry<Question, CourseCsvRequest> question : questions.entrySet()) {
                    if (question.getValue().getParentNode().contentEquals(chapter.getName())) {
                        quiz.getQuestions().add(question.getKey());
                        quiz.setName(question.getValue().getNodeName());
                    }
                }
                chapter.setQuiz(quiz);
            }

            for(Map.Entry<Course, CourseCsvRequest> course : courses.entrySet()) {
                if (course.getValue().getNodeName().contentEquals(chapterRow.getParentNode())) {
                    course.getKey().getChapters().add(chapter);
                }
            }
        }
        coursePlan.setCourses(new ArrayList<Course>(courses.keySet()));

        return coursePlan;
    }

}