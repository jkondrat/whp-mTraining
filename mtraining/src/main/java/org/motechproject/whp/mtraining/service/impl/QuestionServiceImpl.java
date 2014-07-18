package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.whp.mtraining.domain.*;
import org.motechproject.whp.mtraining.dto.AnswerDto;
import org.motechproject.whp.mtraining.dto.ContentIdentifierDto;
import org.motechproject.whp.mtraining.dto.QuestionDto;
import org.motechproject.whp.mtraining.repository.AllQuestions;
import org.motechproject.whp.mtraining.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Implementation class for {@link org.motechproject.mtraining.service.QuestionService}.
 * Given a content DTO to add, it constructs a tree structured generic {@link org.motechproject.mtraining.domain.Node}
 * and uses {@link org.motechproject.mtraining.service.impl.QuestionServiceImpl#nodeHandlerOrchestrator} to process the node
 */

@Service("questionService")
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private NodeHandlerOrchestrator nodeHandlerOrchestrator;
    @Autowired
    private AllQuestions allQuestions;

    @Override
    public ContentIdentifierDto addOrUpdateQuestion(QuestionDto questionDto) {
        Node quizNode = constructQuestionNodes(asList(questionDto)).get(0);
        nodeHandlerOrchestrator.process(quizNode);
        return getContentIdentifier(quizNode);
    }

    @Override
    public QuestionDto getQuestion(ContentIdentifierDto questionIdentifier) {
        Question question = allQuestions.findBy(questionIdentifier.getContentId(), questionIdentifier.getVersion());
        return question != null ? mapToQuestionDto(question) : null;
    }

    @Override
    public List<QuestionDto> getAllQuestions() {
        List<Question> questions = allQuestions.getAll();
        List<QuestionDto> questionDtoList = new ArrayList<>();
        for (Question question : questions) {
            questionDtoList.add(mapToQuestionDto(question));
        }
        return questionDtoList;
    }

    protected List<Node> constructQuestionNodes(List<QuestionDto> questions) {
        List<Node> questionNodes = new ArrayList<>();
        for (QuestionDto question : questions) {
            Node questionNode = new Node(NodeType.QUESTION, question);
            questionNodes.add(questionNode);
        }
        return questionNodes;
    }

    protected QuestionDto mapToQuestionDto(Question question) {
        AnswerDto answerDto = mapToAnswerDto(question.getAnswer());
        return new QuestionDto(question.getContentId(), question.getVersion(), question.isActive(),
                question.getName(), question.getDescription(), question.getExternalContentId(), answerDto, question.getOptions(), question.getCreatedBy());
    }

    private ContentIdentifierDto getContentIdentifier(Node node) {
        Content savedContent = node.getPersistentEntity();
        return savedContent != null ? new ContentIdentifierDto(savedContent.getContentId(), savedContent.getVersion()) : null;
    }

    private AnswerDto mapToAnswerDto(Answer answer) {
        return new AnswerDto(answer.getCorrectOption(), answer.getExternalContentId());
    }
}
