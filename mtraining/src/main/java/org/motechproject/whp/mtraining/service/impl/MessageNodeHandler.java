package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.whp.mtraining.constants.MTrainingEventConstants;
import org.motechproject.whp.mtraining.domain.Message;
import org.motechproject.whp.mtraining.domain.Node;
import org.motechproject.whp.mtraining.dto.ContentDto;
import org.motechproject.whp.mtraining.dto.MessageDto;
import org.motechproject.whp.mtraining.exception.CourseStructureValidationException;
import org.motechproject.whp.mtraining.repository.AllMessages;
import org.motechproject.whp.mtraining.validator.CourseStructureValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Implementation of abstract class {@link NodeHandler}.
 * Validates, saves and raises an event for a node of type {@link org.motechproject.mtraining.domain.NodeType#MESSAGE}
 */

@Component
public class MessageNodeHandler extends NodeHandler {
    private static Logger logger = LoggerFactory.getLogger(MessageNodeHandler.class);

    @Autowired
    private AllMessages allMessages;

    @Override
    protected void validateNodeData(ContentDto nodeData) {
        MessageDto messageDto = (MessageDto) nodeData;
        CourseStructureValidationResponse validationResponse = validator().validateMessage(messageDto);
        if (!validationResponse.isValid()) {
            String message = String.format("Invalid message: %s", validationResponse.getErrorMessage());
            logger.error(message);
            throw new CourseStructureValidationException(message);
        }
    }

    @Override
    protected Message saveAndRaiseEvent(Node node) {
        MessageDto messageDto = (MessageDto) node.getNodeData();
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Saving message: %s", messageDto.getName()));
        }

        Message message = getMessage(messageDto);
        allMessages.add(message);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Raising event for saved message: %s", message.getContentId()));
        }
        sendEvent(MTrainingEventConstants.MESSAGE_CREATION_EVENT, message.getContentId(), message.getVersion());
        return message;
    }

    private Message getMessage(MessageDto messageDto) {
        UUID contentId = messageDto.getContentId();
        if (contentId == null) {
            return new Message(messageDto.getName(), UUID.fromString(messageDto.getExternalContentId()), messageDto.getVersion(), messageDto.getDescription(), messageDto.getExternalContentId(), messageDto.getCreatedBy(), null, messageDto.isActive());
        }

        Message existingMessage = allMessages.getLatestVersionByContentId(contentId);
        Message messageToSave = new Message(existingMessage.getName(), UUID.fromString(existingMessage.getExternalContentId()), existingMessage.getVersion(), existingMessage.getDescription(), existingMessage.getExternalContentId(), existingMessage.getCreatedBy(), null, existingMessage.isActive());
        messageToSave.incrementVersion();
        return messageToSave;
    }
}
