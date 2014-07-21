package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.whp.mtraining.domain.*;
import org.motechproject.whp.mtraining.dto.ChapterDto;
import org.motechproject.whp.mtraining.dto.ContentIdentifierDto;
import org.motechproject.whp.mtraining.dto.MessageDto;
import org.motechproject.whp.mtraining.dto.QuizDto;
import org.motechproject.whp.mtraining.repository.AllChapters;
import org.motechproject.whp.mtraining.service.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Implementation class for {@link org.motechproject.mtraining.service.ChapterService}.
 * Given a content DTO to add, it constructs a tree structured generic {@link org.motechproject.mtraining.domain.Node}
 * and uses {@link org.motechproject.mtraining.service.impl.ChapterServiceImpl#nodeHandlerOrchestrator} to process the node
 */

@Service("chapterService")
public class ChapterServiceImpl implements ChapterService {

    private NodeHandlerOrchestrator nodeHandlerOrchestrator;
    private AllChapters allChapters;
    private MessageServiceImpl messageService;
    private QuizServiceImpl quizService;

    @Autowired
    public ChapterServiceImpl(NodeHandlerOrchestrator nodeHandlerOrchestrator, AllChapters allChapters, MessageServiceImpl messageService, QuizServiceImpl quizService) {
        this.nodeHandlerOrchestrator = nodeHandlerOrchestrator;
        this.allChapters = allChapters;
        this.messageService = messageService;
        this.quizService = quizService;
    }

    /**
     * Update the chapter if it exists
     *
     * @param chapterDto
     * @return
     */
    @Override
    public ContentIdentifierDto addOrUpdateChapter(ChapterDto chapterDto) {
        Node chapterNode = constructChapterNode(chapterDto);
        nodeHandlerOrchestrator.process(chapterNode);
        return getContentIdentifier(chapterNode);
    }

    @Override
    public ChapterDto getChapter(ContentIdentifierDto chapterIdentifier) {
        Chapter chapter = allChapters.findBy(chapterIdentifier.getContentId(), chapterIdentifier.getVersion());
        return chapter != null ? mapToChapterDto(chapter) : null;
    }

    @Override
    public List<ChapterDto> getAllChapters() {
        ArrayList<ChapterDto> chapterDtoList = new ArrayList<>();
        List<Chapter> chapters = allChapters.getAll();
        for (Chapter chapter : chapters) {
            chapterDtoList.add(mapToChapterDto(chapter));
        }
        return chapterDtoList;
    }

    private Node constructChapterNode(ChapterDto chapterDto) {
        return constructChapterNodes(asList(chapterDto)).get(0);
    }

    protected List<Node> constructChapterNodes(List<ChapterDto> chapters) {
        List<Node> chapterNodes = new ArrayList<>();
        for (ChapterDto chapter : chapters) {
            List<Node> messageNodes = messageService.constructMessageNodes(chapter.getMessages());
            if (chapter.getQuiz() != null) {
                messageNodes.add(quizService.constructQuizNode(chapter.getQuiz()));
            }
            Node chapterNode = new Node(NodeType.CHAPTER, chapter, messageNodes);
            chapterNodes.add(chapterNode);
        }
        return chapterNodes;
    }

    protected ChapterDto mapToChapterDto(Chapter chapter) {
        ArrayList<MessageDto> messages = new ArrayList<>();
        for (Content message : chapter.getMessages()) {
            MessageDto messageDto = messageService.mapToMessageDto((Message) message);
            messages.add(messageDto);
        }
        QuizDto quizDto = quizService.mapToQuizDto(chapter.getQuiz());
        return new ChapterDto(chapter.getContentId(), chapter.getVersion(), chapter.isActive(), chapter.getName(), chapter.getDescription(),
                chapter.getExternalContentId(), chapter.getCreatedBy(), messages, quizDto);
    }

    private ContentIdentifierDto getContentIdentifier(Node node) {
        Content savedContent = node.getPersistentEntity();
        return savedContent != null ? new ContentIdentifierDto(savedContent.getContentId(), savedContent.getVersion()) : null;
    }
}
