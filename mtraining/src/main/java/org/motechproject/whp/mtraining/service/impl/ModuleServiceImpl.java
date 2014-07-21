package org.motechproject.whp.mtraining.service.impl;

import org.motechproject.whp.mtraining.domain.*;
import org.motechproject.whp.mtraining.dto.ChapterDto;
import org.motechproject.whp.mtraining.dto.ContentIdentifierDto;
import org.motechproject.whp.mtraining.dto.ModuleDto;
import org.motechproject.whp.mtraining.repository.AllModules;
import org.motechproject.whp.mtraining.service.ModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Implementation class for {@link org.motechproject.mtraining.service.ModuleService}.
 * Given a content DTO to add, it constructs a tree structured generic {@link org.motechproject.mtraining.domain.Node}
 * and uses {@link org.motechproject.mtraining.service.impl.ModuleServiceImpl#nodeHandlerOrchestrator} to process the node
 */

@Service("moduleService")
public class ModuleServiceImpl implements ModuleService {

    private NodeHandlerOrchestrator nodeHandlerOrchestrator;
    private AllModules allModules;
    private ChapterServiceImpl chapterService;

    @Autowired
    public ModuleServiceImpl(NodeHandlerOrchestrator nodeHandlerOrchestrator, AllModules allModules, ChapterServiceImpl chapterService) {
        this.nodeHandlerOrchestrator = nodeHandlerOrchestrator;
        this.allModules = allModules;
        this.chapterService = chapterService;
    }

    /**
     * Update the module if it exists,add otherwise.
     * Validation is done before update/insert and validation exception is thrown if module is invalid.
     *
     * @param moduleDto
     * @return
     */
    @Override
    public ContentIdentifierDto addOrUpdateModule(ModuleDto moduleDto) {
        Node moduleNode = constructModuleNode(moduleDto);
        nodeHandlerOrchestrator.process(moduleNode);
        return getContentIdentifier(moduleNode);
    }

    /**
     * Returns a module given moduleIdentifier, returns null if none found
     *
     * @param moduleIdentifier
     * @return
     */
    @Override
    public ModuleDto getModule(ContentIdentifierDto moduleIdentifier) {
        Module module = allModules.findBy(moduleIdentifier.getContentId(), moduleIdentifier.getVersion());
        return module != null ? mapToModuleDto(module) : null;
    }


    /**
     * Returns a list of all modules
     *
     * @return
     */
    @Override
    public List<ModuleDto> getAllModules() {
        List<Module> modules = allModules.getAll();
        List<ModuleDto> moduleDtoList = new ArrayList<>();
        for (Module module : modules) {
            moduleDtoList.add(mapToModuleDto(module));
        }
        return moduleDtoList;
    }

    protected ModuleDto mapToModuleDto(Module module) {
        List<ChapterDto> chapters = new ArrayList<>();
        for (Content chapter : module.getChapters()) {
            ChapterDto chapterDto = chapterService.mapToChapterDto((Chapter) chapter);
            chapters.add(chapterDto);
        }
        return new ModuleDto(module.getContentId(), module.getVersion(), module.isActive(), module.getName(), module.getDescription(),
                module.getExternalContentId(), module.getCreatedBy(), chapters);
    }

    protected List<Node> constructModuleNodes(List<ModuleDto> modules) {
        List<Node> moduleNodes = new ArrayList<>();
        for (ModuleDto module : modules) {
            List<Node> chapterNodes = chapterService.constructChapterNodes(module.getChapters());
            Node moduleNode = new Node(NodeType.MODULE, module, chapterNodes);
            moduleNodes.add(moduleNode);
        }
        return moduleNodes;
    }

    private Node constructModuleNode(ModuleDto moduleDto) {
        return constructModuleNodes(asList(moduleDto)).get(0);
    }

    private ContentIdentifierDto getContentIdentifier(Node node) {
        Content savedContent = node.getPersistentEntity();
        return savedContent != null ? new ContentIdentifierDto(savedContent.getContentId(), savedContent.getVersion()) : null;
    }
}
