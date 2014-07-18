package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.whp.mtraining.domain.*;
import org.motechproject.whp.mtraining.dto.*;
import org.motechproject.whp.mtraining.exception.InactiveContentException;
import org.motechproject.whp.mtraining.exception.InvalidQuestionException;
import org.motechproject.whp.mtraining.exception.InvalidQuizException;
import org.motechproject.whp.mtraining.repository.AllQuizes;
import org.motechproject.whp.mtraining.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Implementation class for {@link org.motechproject.mtraining.service.QuizService}.
 * Given a content DTO to add, it constructs a tree structured generic {@link org.motechproject.mtraining.domain.Node}
 * and uses {@link org.motechproject.mtraining.service.impl.QuizServiceImpl#nodeHandlerOrchestrator} to process the node
 */

@Service("quizService")
public class QuizServiceImpl implements QuizService {

    private NodeHandlerOrchestrator nodeHandlerOrchestrator;
    private AllQuizes allQuizes;
    private QuestionServiceImpl questionService;
    private static final int HUNDRED_PERCENTS = 100;

    @Autowired
    public QuizServiceImpl(NodeHandlerOrchestrator nodeHandlerOrchestrator, AllQuizes allQuizes, QuestionServiceImpl questionService) {
        this.nodeHandlerOrchestrator = nodeHandlerOrchestrator;
        this.allQuizes = allQuizes;
        this.questionService = questionService;
    }

    /**
     * Add or update Quiz
     *
     * @param quizDto
     * @return
     */
    @Override
    public ContentIdentifierDto addOrUpdateQuiz(QuizDto quizDto) {
        Node quizNode = constructQuizNode(quizDto);
        nodeHandlerOrchestrator.process(quizNode);
        return getContentIdentifier(quizNode);
    }

    /**
     * Get Quiz given quiz identifier
     *
     * @param quizIdentifier
     * @return
     */
    @Override
    public QuizDto getQuiz(ContentIdentifierDto quizIdentifier) {
        Quiz quiz = allQuizes.findBy(quizIdentifier.getContentId(), quizIdentifier.getVersion());
        return quiz != null ? mapToQuizDto(quiz) : null;
    }


    /**
     * Get all quizzes.
     *
     * @return
     */
    @Override
    public List<QuizDto> getAllQuizes() {
        List<Quiz> quizes = allQuizes.getAll();
        List<QuizDto> quizDtoList = new ArrayList<>();
        for (Quiz quiz : quizes) {
            quizDtoList.add(mapToQuizDto(quiz));
        }
        return quizDtoList;
    }


    /**
     * Returns a list of question identifiers given the quiz id
     * A quiz is configured to have a  number of questions which are to be returned according to the strategy.
     * As of now a Quiz will have a default strategy of "RANDOM".
     * Assume a quiz is created with a strategy RANDOM and numberOfQuestions as 5.
     * If now 10 questions are added to the quiz (there is no limitation on this),then this API will randomly select 5 questions and return these
     * Will throw IllegalStateException if the number of questions is less,for eg. in the above scenario if the Quiz has only 4 questions,then exception will be thrown.
     * Will throw InvalidQuiz exception if quiz is not found
     * Will throw InactiveContentException if quiz is inactive
     * @param quizIdentifier
     * @return
     */
    @Override
    public List<ContentIdentifierDto> getQuestionsForQuiz(ContentIdentifierDto quizIdentifier) {
        UUID quizId = quizIdentifier.getContentId();
        Integer quizVersion = quizIdentifier.getVersion();
        Quiz quiz = allQuizes.findBy(quizId, quizVersion);
        if (quiz == null) {
            throw new InvalidQuizException(quizIdentifier.getContentId());
        }
        if (!quiz.isActive()) {
            throw new InactiveContentException(quizIdentifier.getContentId());
        }
        List<Question> questions = filterActiveQuestions(quiz.getQuestions());
        Integer numberOfQuizQuestionsToBePlayed = quiz.getNumberOfQuizQuestionsToBePlayed();
        if (questions.size() < numberOfQuizQuestionsToBePlayed) {
            throw new IllegalStateException("The quiz with id: " + quizId + " and version: " + quizVersion + " does not contain required no of active questions");
        }
        return getQuestionIdentifiers(questions, numberOfQuizQuestionsToBePlayed, quiz.getStrategy());
    }


    /**
     * Return a quiz result when answers are provided
     * If the quizId does not exist then InvalidQuizException is thrown.
     * If a questionId does not exist then InvalidQuestionException is thrown.
     *
     * @param quizAnswerSheetDto
     * @return
     */
    @Override
    public QuizResultSheetDto gradeQuiz(QuizAnswerSheetDto quizAnswerSheetDto) {
        UUID quizId = quizAnswerSheetDto.getQuizDto().getContentId();
        Quiz quiz = allQuizes.findBy(quizId, quizAnswerSheetDto.getQuizDto().getVersion());
        if (quiz == null) {
            throw new InvalidQuizException(quizId);
        }
        List<QuestionResultDto> questionResultDtos = new ArrayList<>();
        Integer score = 0;
        for (AnswerSheetDto answerSheetDto : quizAnswerSheetDto.getAnswerSheetDtos()) {
            Question question = quiz.findQuestion(answerSheetDto.getQuestionId());
            if (question == null) {
                throw new InvalidQuestionException(quizId, answerSheetDto.getQuestionId());
            }
            Boolean wasQuestionAnsweredCorrectly = question.isCorrectAnswer(answerSheetDto.getSelectedOption());
            QuestionResultDto questionResultDto = new QuestionResultDto(answerSheetDto.getQuestionId(), question.getVersion(), answerSheetDto.getSelectedOption(), wasQuestionAnsweredCorrectly);
            questionResultDtos.add(questionResultDto);
            if (questionResultDto.isCorrect()) {
                score++;
            }
        }
        Double percentageScored = calculatePercentage(score, quiz.getNumberOfQuizQuestionsToBePlayed());
        Boolean quizPassed = quiz.isPassed(percentageScored);
        return new QuizResultSheetDto(quizAnswerSheetDto.getExternalId(), quizAnswerSheetDto.getQuizDto(), questionResultDtos, percentageScored, quizPassed);
    }

    private Double calculatePercentage(Integer score, int noOfQuestions) {
        Double scoredPercentage = (score.doubleValue() / noOfQuestions) * HUNDRED_PERCENTS;
        return Double.parseDouble(new DecimalFormat("##.##").format(scoredPercentage));
    }

    protected QuizDto mapToQuizDto(Quiz quiz) {
        if (quiz == null) {
            return null;
        }
        ArrayList<QuestionDto> questions = new ArrayList<>();
        for (Question question : quiz.getQuestions()) {
            QuestionDto questionDto = questionService.mapToQuestionDto(question);
            questions.add(questionDto);
        }
        return new QuizDto(quiz.getContentId(), quiz.getVersion(), quiz.isActive(), quiz.getName(), quiz.getExternalContentId(), questions, quiz.getNumberOfQuizQuestionsToBePlayed(), quiz.getPassPercentage(), quiz.getCreatedBy());
    }

    protected Node constructQuizNode(QuizDto quizDto) {
        List<Node> questionNodes = questionService.constructQuestionNodes(quizDto.getQuestions());
        return new Node(NodeType.QUIZ, quizDto, questionNodes);
    }

    private List<ContentIdentifierDto> getQuestionIdentifiers(List<Question> questions, Integer numberOfQuestionsToBePlayed, String quizStrategy) {
        if ("RANDOM".equalsIgnoreCase(quizStrategy)) {
            Collections.shuffle(questions);
        }

        List<ContentIdentifierDto> questionsToBeAsked = new ArrayList<>();

        for (Question question : questions.subList(0, numberOfQuestionsToBePlayed)) {
            ContentIdentifierDto questionsIdentifier = new ContentIdentifierDto(question.getContentId(), question.getVersion());
            questionsToBeAsked.add(questionsIdentifier);
        }

        return questionsToBeAsked;
    }

    private List<Question> filterActiveQuestions(List<Question> questions) {
        List<Question> activeQuestionDtos = new ArrayList<>();
        for (Question question : questions) {
            if (question.isActive()) {
                activeQuestionDtos.add(question);
            }
        }
        return activeQuestionDtos;
    }

    private ContentIdentifierDto getContentIdentifier(Node node) {
        Content savedContent = node.getPersistentEntity();
        return savedContent != null ? new ContentIdentifierDto(savedContent.getContentId(), savedContent.getVersion()) : null;
    }
}
