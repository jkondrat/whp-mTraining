package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.EventRelay;
import org.motechproject.whp.mtraining.constants.MTrainingEventConstants;
import org.motechproject.whp.mtraining.domain.Content;
import org.motechproject.whp.mtraining.domain.ContentIdentifier;
import org.motechproject.whp.mtraining.domain.Node;
import org.motechproject.whp.mtraining.domain.NodeType;
import org.motechproject.whp.mtraining.dto.ContentDto;
import org.motechproject.whp.mtraining.validator.CourseStructureValidator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Abstract class responsible for validating and saving the given tree structured {@link Node}.
 * Expects an implementer to provide methods for validating and saving a node based on the {@link org.motechproject.mtraining.domain.NodeType}
 */

public abstract class NodeHandler {

    @Autowired
    private CourseStructureValidator courseStructureValidator;
    @Autowired
    private EventRelay eventRelay;

    protected abstract void validateNodeData(ContentDto nodeData);

    protected abstract Content saveAndRaiseEvent(Node node);

    protected void sendEvent(String subject, UUID contentId, Integer version) {
        HashMap<String, Object> eventParameters = new HashMap<>();
        eventParameters.put(MTrainingEventConstants.CONTENT_ID, contentId);
        eventParameters.put(MTrainingEventConstants.VERSION, version);
        MotechEvent motechEvent = new MotechEvent(subject, eventParameters);
        eventRelay.sendEventMessage(motechEvent);
    }

    protected List<ContentIdentifier> getChildContentIdentifiers(Node node) {
        List<ContentIdentifier> childContentIdentifiers = new ArrayList<>();
        for (Node childNode : node.getChildNodes()) {
            Content persistentEntity = childNode.getPersistentEntity();
            if (persistentEntity != null) {
                childContentIdentifiers.add(new ContentIdentifier(persistentEntity.getContentId(), persistentEntity.getVersion()));
            }
        }
        return childContentIdentifiers;
    }

    protected <T extends Content> List<T> getChildContentNodes(Node node, NodeType nodeType) {
        List<T> contents = new ArrayList<>();
        for (Node childNode : node.getChildNodes()) {
            T persistentEntity = (T) childNode.getPersistentEntity();
            if (persistentEntity != null && childNode.getNodeType().equals(nodeType)) {
                contents.add(persistentEntity);
            }
        }
        return contents;
    }


    protected CourseStructureValidator validator() {
        return courseStructureValidator;
    }
}
