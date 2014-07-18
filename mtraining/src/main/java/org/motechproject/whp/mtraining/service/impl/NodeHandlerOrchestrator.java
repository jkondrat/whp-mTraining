package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.whp.mtraining.domain.Content;
import org.motechproject.whp.mtraining.domain.Node;
import org.motechproject.whp.mtraining.domain.NodeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Traverses the given tree structured node {@link Node} for validating and saving it, by invoking
 * different implementations of {@link NodeHandler} based on the {@link NodeType}
 * by orchestrating with {@link NodeHandlerFactory}
 */

@Component
public class NodeHandlerOrchestrator {
    private NodeHandlerFactory nodeHandlerFactory;

    @Autowired
    public NodeHandlerOrchestrator(NodeHandlerFactory nodeHandlerFactory) {
        this.nodeHandlerFactory = nodeHandlerFactory;
    }

    public void process(Node node) {
        validate(node);
        add(node);
    }

    /**
     * Validates given tree structured node and all its descendant nodes by doing a LEVEL ORDER TRAVERSAL,
     * since the validation of the parent node should be done before child nodes level by level.
     */

    private void validate(Node rootNode) {
        Queue<Node> nodeQueue = new ArrayDeque<>();
        nodeQueue.add(rootNode);
        while (!nodeQueue.isEmpty()) {
            Node currentNode = nodeQueue.poll();
            handler(currentNode.getNodeType()).validateNodeData(currentNode.getNodeData());
            nodeQueue.addAll(currentNode.getChildNodes());
        }
    }

    /**
     * Saves the given tree structured node and all its descendant nodes by doing a POST ORDER TRAVERSAL,
     * as the child nodes have to saved before parent.
     */
    private void add(Node node) {
        for (Node childNode : node.getChildNodes()) {
            add(childNode);
        }
        Content savedEntity = handler(node.getNodeType()).saveAndRaiseEvent(node);
        node.setPersistentEntity(savedEntity);
    }

    private NodeHandler handler(NodeType nodeType) {
        return nodeHandlerFactory.getHandler(nodeType);
    }
}
