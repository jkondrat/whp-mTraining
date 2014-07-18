package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.whp.mtraining.constants.MTrainingEventConstants;
import org.motechproject.whp.mtraining.domain.Answer;
import org.motechproject.whp.mtraining.domain.Node;
import org.motechproject.whp.mtraining.domain.Question;
import org.motechproject.whp.mtraining.dto.ContentDto;
import org.motechproject.whp.mtraining.dto.QuestionDto;
import org.motechproject.whp.mtraining.exception.CourseStructureValidationException;
import org.motechproject.whp.mtraining.repository.AllQuestions;
import org.motechproject.whp.mtraining.validator.CourseStructureValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementation of abstract class {@link org.motechproject.whp.mtraining.service.impl.NodeHandler}.
 * Validates, saves and raises an event for a node of type {@link org.motechproject.whp.mtraining.domain.NodeType#QUESTION}
 */

@Component
public class QuestionNodeHandler extends NodeHandler {
    private Logger logger = LoggerFactory.getLogger(QuestionNodeHandler.class);

    @Autowired
    private AllQuestions questions;

    @Override
    protected void validateNodeData(ContentDto nodeData) {
        QuestionDto questionDto = (QuestionDto) nodeData;
        CourseStructureValidationResponse validationResponse = validator().validateQuestion(questionDto);
        if (!validationResponse.isValid()) {
            String question = String.format("Invalid question: %s", validationResponse.getErrorMessage());
            logger.error(question);
            throw new CourseStructureValidationException(question);
        }
    }

    @Override
    protected Question saveAndRaiseEvent(Node node) {
        QuestionDto questionDto = (QuestionDto) node.getNodeData();
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Saving question: %s", questionDto.getName()));
        }

        Question question = getMessage(questionDto);
        questions.add(question);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Raising event for saved question: %s", question.getContentId()));
        }
        sendEvent(MTrainingEventConstants.QUESTION_CREATION_EVENT, question.getContentId(), question.getVersion());
        return question;
    }

    private Question getMessage(QuestionDto questionDto) {
        UUID contentId = questionDto.getContentId();
        if (contentId == null) {
            return new Question(questionDto.isActive(), questionDto.getName(), questionDto.getDescription(), questionDto.getExternalContentId(),
                    new Answer(questionDto.getAnswer().getExternalId(), questionDto.getAnswer().getCorrectOption()), questionDto.getOptions(), questionDto.getCreatedBy());
        }
        Question existingQuestion = questions.getLatestVersionByContentId(contentId);
        Question questionToSave = new Question(existingQuestion.getContentId(), existingQuestion.getVersion(), questionDto.isActive(),
                questionDto.getName(), questionDto.getDescription(), questionDto.getExternalContentId(),
                new Answer(questionDto.getAnswer().getExternalId(), questionDto.getAnswer().getCorrectOption()), questionDto.getOptions(), questionDto.getCreatedBy());
        questionToSave.incrementVersion();
        return questionToSave;
    }
}
