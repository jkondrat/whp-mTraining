package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.whp.mtraining.domain.NodeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory which keeps tracks of implementations of {@link NodeHandler} for different {@link NodeType}s.
 */

@Component
public class NodeHandlerFactory {

    private Map<NodeType, NodeHandler> nodeHandlerMap;

    @Autowired
    public NodeHandlerFactory(CourseNodeHandler courseNodeHandler, ModuleNodeHandler moduleNodeHandler,
                              ChapterNodeHandler chapterNodeHandler, MessageNodeHandler messageNodeHandler, QuizNodeHandler quizNodeHandler, QuestionNodeHandler questionNodeHandler) {
        nodeHandlerMap = new HashMap<>();
        nodeHandlerMap.put(NodeType.COURSE, courseNodeHandler);
        nodeHandlerMap.put(NodeType.MODULE, moduleNodeHandler);
        nodeHandlerMap.put(NodeType.CHAPTER, chapterNodeHandler);
        nodeHandlerMap.put(NodeType.MESSAGE, messageNodeHandler);
        nodeHandlerMap.put(NodeType.QUIZ, quizNodeHandler);
        nodeHandlerMap.put(NodeType.QUESTION, questionNodeHandler);
    }

    public NodeHandler getHandler(NodeType nodeType) {
        return nodeHandlerMap.get(nodeType);
    }
}
