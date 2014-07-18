package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.whp.mtraining.constants.MTrainingEventConstants;
import org.motechproject.whp.mtraining.domain.Chapter;
import org.motechproject.whp.mtraining.domain.Module;
import org.motechproject.whp.mtraining.domain.Node;
import org.motechproject.whp.mtraining.dto.ContentDto;
import org.motechproject.whp.mtraining.dto.ModuleDto;
import org.motechproject.whp.mtraining.exception.CourseStructureValidationException;
import org.motechproject.whp.mtraining.repository.AllModules;
import org.motechproject.whp.mtraining.validator.CourseStructureValidationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static org.motechproject.whp.mtraining.domain.NodeType.CHAPTER;

/**
 * Implementation of abstract class {@link NodeHandler}.
 * Validates, saves and raises an event for a node of type {@link org.motechproject.whp.mtraining.domain.NodeType#MODULE}
 */

@Component
public class ModuleNodeHandler extends NodeHandler {

    private static Logger logger = LoggerFactory.getLogger(ModuleNodeHandler.class);

    @Autowired
    private AllModules allModules;

    @Override
    protected void validateNodeData(ContentDto nodeData) {
        ModuleDto moduleDto = (ModuleDto) nodeData;
        CourseStructureValidationResponse validationResponse = validator().validateModule(moduleDto);
        if (!validationResponse.isValid()) {
            String message = String.format("Invalid module: %s", validationResponse.getErrorMessage());
            logger.error(message);
            throw new CourseStructureValidationException(message);
        }
    }

    @Override
    protected Module saveAndRaiseEvent(Node node) {
        ModuleDto moduleDto = (ModuleDto) node.getNodeData();
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Saving module: %s", moduleDto.getName()));
        }

        Module module = getModule(moduleDto, getChapters(node));
        allModules.add(module);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Raising event for saved module: %s", module.getContentId()));
        }

        sendEvent(MTrainingEventConstants.MODULE_CREATION_EVENT, module.getContentId(), module.getVersion());
        return module;
    }

    private List<Chapter> getChapters(Node node) {
        return getChildContentNodes(node, CHAPTER);
    }

    private Module getModule(ModuleDto moduleDto, List<Chapter> chapters) {
        UUID contentId = moduleDto.getContentId();
        if (contentId == null) {
            return new Module(moduleDto);
        }

        Module existingModule = allModules.getLatestVersionByContentId(contentId);
        // TODO
        Module moduleToSave = new Module(existingModule.getName(), existingModule.getContentId(), existingModule.getVersion(), moduleDto.getDescription(),
                moduleDto.getExternalContentId(), moduleDto.getCreatedBy(), null, chapters, existingModule.isActive());
        moduleToSave.incrementVersion();
        return moduleToSave;
    }
}
