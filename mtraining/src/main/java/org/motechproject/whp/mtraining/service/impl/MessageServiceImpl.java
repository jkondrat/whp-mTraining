package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.whp.mtraining.domain.Content;
import org.motechproject.whp.mtraining.domain.Message;
import org.motechproject.whp.mtraining.domain.Node;
import org.motechproject.whp.mtraining.domain.NodeType;
import org.motechproject.whp.mtraining.dto.ContentIdentifierDto;
import org.motechproject.whp.mtraining.dto.MessageDto;
import org.motechproject.whp.mtraining.repository.AllMessages;
import org.motechproject.whp.mtraining.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Implementation class for {@link org.motechproject.mtraining.service.MessageService}.
 * Given a content DTO to add, it constructs a tree structured generic {@link org.motechproject.mtraining.domain.Node}
 * and uses {@link org.motechproject.mtraining.service.impl.MessageServiceImpl#nodeHandlerOrchestrator} to process the node
 */

@Service("messageService")
public class MessageServiceImpl implements MessageService {

    @Autowired
    private NodeHandlerOrchestrator nodeHandlerOrchestrator;

    @Autowired
    private AllMessages allMessages;

    @Override
    public ContentIdentifierDto addOrUpdateMessage(MessageDto messageDto) {
        Node messageNode = constructMessageNode(messageDto);
        nodeHandlerOrchestrator.process(messageNode);
        return getContentIdentifier(messageNode);
    }

    @Override
    public MessageDto getMessage(ContentIdentifierDto messageIdentifier) {
        Message message = allMessages.findBy(messageIdentifier.getContentId(), messageIdentifier.getVersion());
        return message != null ? mapToMessageDto(message) : null;
    }

    @Override
    public List<MessageDto> getAllMessages() {
        List<Message> messages = allMessages.getAll();
        List<MessageDto> messageDtoList = new ArrayList<>();
        for (Message message : messages) {
            messageDtoList.add(mapToMessageDto(message));
        }
        return messageDtoList;
    }

    protected MessageDto mapToMessageDto(Message message) {
        return new MessageDto(message.getContentId(), message.getVersion(), message.isActive(), message.getName(), message.getDescription(), message.getExternalContentId(), message.getCreatedBy());
    }

    protected List<Node> constructMessageNodes(List<MessageDto> messages) {
        List<Node> messageNodes = new ArrayList<>();
        for (MessageDto message : messages) {
            Node messageNode = new Node(NodeType.MESSAGE, message);
            messageNodes.add(messageNode);
        }
        return messageNodes;
    }

    private Node constructMessageNode(MessageDto messageDto) {
        return constructMessageNodes(asList(messageDto)).get(0);
    }

    private ContentIdentifierDto getContentIdentifier(Node node) {
        Content savedContent = node.getPersistentEntity();
        return savedContent != null ? new ContentIdentifierDto(savedContent.getContentId(), savedContent.getVersion()) : null;
    }
}
