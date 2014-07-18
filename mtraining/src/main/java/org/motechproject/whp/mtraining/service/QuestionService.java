package org.motechproject.whp.mtraining.service;

import org.motechproject.whp.mtraining.dto.ContentIdentifierDto;
import org.motechproject.whp.mtraining.dto.QuestionDto;

import java.util.List;

/**
 * Service Interface that exposes APIs to questions
 */
public interface QuestionService {

    /**
     * Add a question if it already does not exist, update it otherwise.
     *
     * @param questionDto
     * @return
     */
    ContentIdentifierDto addOrUpdateQuestion(QuestionDto questionDto);

    /**
     * Return question with given question identifier
     *
     * @param questionIdentifier
     * @return
     */
    QuestionDto getQuestion(ContentIdentifierDto questionIdentifier);

    /**
     * Return all questions
     *
     * @return
     */
    List<QuestionDto> getAllQuestions();
}

