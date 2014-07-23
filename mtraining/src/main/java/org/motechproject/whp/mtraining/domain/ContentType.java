package org.motechproject.whp.mtraining.domain;

import org.apache.commons.lang.StringUtils;
import org.motechproject.whp.mtraining.dto.ChapterDto;
import org.motechproject.whp.mtraining.dto.CourseDto;
import org.motechproject.whp.mtraining.dto.MessageDto;
import org.motechproject.whp.mtraining.dto.ModuleDto;
import org.motechproject.whp.mtraining.dto.QuestionDto;
import org.motechproject.whp.mtraining.dto.QuizDto;

import java.util.ArrayList;
import java.util.List;

public enum ContentType {
    COURSE {
        @Override
        public CourseDto toDto(String nodeName, String description, String fileName, boolean isActive, Integer numberOfQuizQuestions,
                               List<String> options, String correctAnswer, String correctAnswerFileName, Double passPercentage, List<Object> childDtos, String contentAuthor) {
            return new CourseDto(isActive, nodeName, description, fileName, contentAuthor, (List<ModuleDto>) (Object) childDtos);
        }
    },
    MODULE {
        @Override
        public ModuleDto toDto(String nodeName, String description, String fileName, boolean isActive, Integer numberOfQuizQuestions,
                               List<String> options, String correctAnswer, String correctAnswerFileName, Double passPercentage, List<Object> childDtos, String contentAuthor) {
            return new ModuleDto(isActive, nodeName, description, fileName, contentAuthor, (List<ChapterDto>) (Object) childDtos);
        }
    },
    CHAPTER {
        @Override
        public ChapterDto toDto(String nodeName, String description, String fileName, boolean isActive, Integer numberOfQuizQuestions,
                                List<String> options, String correctAnswer, String correctAnswerFileName, Double passPercentage, List<Object> childDtos, String contentAuthor) {
            List<QuestionDto> questions = filterChildNodesOfType(childDtos, QuestionDto.class);
            List<MessageDto> messages = filterChildNodesOfType(childDtos, MessageDto.class);
            QuizDto quizDto = new QuizDto(true, nodeName, null, questions, numberOfQuizQuestions, passPercentage, contentAuthor);
            return numberOfQuizQuestions > 0 ? new ChapterDto(isActive, nodeName, description, fileName, contentAuthor, messages, quizDto) :
                    new ChapterDto(isActive, nodeName, description, fileName, contentAuthor, messages, null);
        }

        private <T> List<T> filterChildNodesOfType(List<Object> childDtos, Class<T> classType) {
            List<T> childrenOfGivenType = new ArrayList<>();
            for (Object childDto : childDtos) {
                if (childDto.getClass().equals(classType)) {
                    childrenOfGivenType.add((T) childDto);
                }
            }
            return childrenOfGivenType;
        }
    },
    MESSAGE {
        @Override
        public MessageDto toDto(String nodeName, String description, String fileName, boolean isActive, Integer numberOfQuizQuestions,
                                List<String> options, String correctAnswer, String correctAnswerFileName, Double passPercentage, List<Object> childDtos, String contentAuthor) {
            return new MessageDto(isActive, nodeName, description, fileName, contentAuthor);
        }
    },
    QUESTION {
        @Override
        public QuestionDto toDto(String nodeName, String description, String fileName, boolean isActive, Integer numberOfQuizQuestions,
                                 List<String> options, String correctAnswer, String correctAnswerFileName, Double passPercentage, List<Object> childDtos, String contentAuthor) {
            return new QuestionDto(isActive, nodeName, description, fileName, options, contentAuthor);
        }
    };

    public static ContentType from(String nodeType) {
        return ContentType.valueOf(StringUtils.trimToEmpty(nodeType).toUpperCase());
    }

    public abstract Object toDto(String nodeName, String description, String fileName, boolean isActive, Integer numberOfQuizQuestions,
                                 List<String> options, String correctAnswer, String correctAnswerFileName, Double passPercentage, List<Object> childDtos, String contentAuthor);
}
