package icu.windea.pls.lang.hierarchy.type

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.ui.tree.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.lang.hierarchy.type.ParadoxDefinitionHierarchyNodeType as NodeType
import icu.windea.pls.lang.hierarchy.type.ParadoxDefinitionHierarchyType as Type

class ParadoxDefinitionHierarchyTreeStructure(
    project: Project,
    baseDescriptor: ParadoxDefinitionHierarchyNodeDescriptor,
    element: PsiElement,
    val typeConfig: CwtTypeConfig,
    val type: Type
) : HierarchyTreeStructure(project, baseDescriptor) {
    private val elementPointer = element.createPointer()

    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<out HierarchyNodeDescriptor> {
        descriptor as ParadoxDefinitionHierarchyNodeDescriptor
        val nodeType = descriptor.nodeType
        val descriptors = mutableListOf<HierarchyNodeDescriptor>()
        when {
            type == Type.Type -> {
                buildDefinitionChildren(descriptor, descriptors)
            }
            type == Type.TypeAndSubtypes -> {
                when {
                    nodeType == NodeType.Type -> {
                        buildSubtypeConfigChildren(descriptors, descriptor)
                    }
                    else -> {
                        buildDefinitionChildren(descriptor, descriptors)
                    }
                }
            }
            type.nested -> {
                when {
                    nodeType == NodeType.Definition -> {
                        buildNestedDefinitionChildren(descriptor, descriptors)
                    }
                    type == Type.EventTreeInvoker || type == Type.EventTreeInvoked -> {
                        buildEventTreeChildren(descriptor, descriptors)
                    }
                    type == Type.TechTreePre || type == Type.TechTreePost -> {
                        buildTechTreeChildren(descriptor, descriptors)
                    }
                }
            }
        }
        if (descriptors.isEmpty()) return HierarchyNodeDescriptor.EMPTY_ARRAY
        return descriptors.toTypedArray()
    }

    private fun buildSubtypeConfigChildren(descriptors: MutableList<HierarchyNodeDescriptor>, descriptor: ParadoxDefinitionHierarchyNodeDescriptor) {
        val project = myProject
        typeConfig.subtypes.forEach { (_, subtypeConfig) ->
            val subtypeElement = subtypeConfig.pointer.element ?: return@forEach
            descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, subtypeElement, false, subtypeConfig.name, type, NodeType.Subtype)
        }
        val typeElement = descriptor.psiElement ?: return
        val name = PlsBundle.message("hierarchy.definition.descriptor.noSubtype")
        descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, typeElement, false, name, type, NodeType.NoSubtype)
    }

    private fun buildEventTreeChildren(descriptor: ParadoxDefinitionHierarchyNodeDescriptor, descriptors: MutableList<HierarchyNodeDescriptor>) {
        val groupingStrategy = getSettings().hierarchy.eventTreeGrouping
        when {
            groupingStrategy == ParadoxStrategies.EventTreeGrouping.Type && descriptor.nodeType == NodeType.Type -> {
                doBuildEventTreeChildren(descriptor, descriptors, NodeType.EventType)
            }
            else -> {
                buildDefinitionChildren(descriptor, descriptors)
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun doBuildEventTreeChildren(descriptor: ParadoxDefinitionHierarchyNodeDescriptor, descriptors: MutableList<HierarchyNodeDescriptor>, nextNodeType: NodeType) {
        val gameType = typeConfig.configGroup.gameType
        if (gameType == null) return

        val project = myProject
        when (nextNodeType) {
            NodeType.EventType -> {
                val eventTypeConfigs = ParadoxEventManager.getAllTypeConfigs(project, gameType)
                eventTypeConfigs.forEach { config ->
                    val configElement = config.pointer.element ?: return@forEach
                    val name = config.name
                    descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, configElement, false, name, type, NodeType.EventType)
                }
            }
            else -> {}
        }
    }

    private fun buildTechTreeChildren(descriptor: ParadoxDefinitionHierarchyNodeDescriptor, descriptors: MutableList<HierarchyNodeDescriptor>) {
        val groupingStrategy = getSettings().hierarchy.techTreeGrouping
        when {
            groupingStrategy == ParadoxStrategies.TechTreeGrouping.Tier && descriptor.nodeType == NodeType.Type -> {
                doBuildTechTreeChildren(descriptor, descriptors, NodeType.TechTier)
            }
            groupingStrategy == ParadoxStrategies.TechTreeGrouping.Area && descriptor.nodeType == NodeType.Type -> {
                doBuildTechTreeChildren(descriptor, descriptors, NodeType.TechArea)
            }
            groupingStrategy == ParadoxStrategies.TechTreeGrouping.Category && descriptor.nodeType == NodeType.Type -> {
                doBuildTechTreeChildren(descriptor, descriptors, NodeType.TechCategory)
            }
            groupingStrategy == ParadoxStrategies.TechTreeGrouping.Area2Category -> {
                when (descriptor.nodeType) {
                    NodeType.Type -> {
                        doBuildTechTreeChildren(descriptor, descriptors, NodeType.TechArea)
                    }
                    NodeType.TechArea -> {
                        doBuildTechTreeChildren(descriptor, descriptors, NodeType.TechCategory)
                    }
                    else -> {
                        buildDefinitionChildren(descriptor, descriptors)
                    }
                }
            }
            else -> {
                buildDefinitionChildren(descriptor, descriptors)
            }
        }
    }

    private fun doBuildTechTreeChildren(descriptor: ParadoxDefinitionHierarchyNodeDescriptor, descriptors: MutableList<HierarchyNodeDescriptor>, nextNodeType: NodeType) {
        val gameType = typeConfig.configGroup.gameType
        if (gameType == null) return

        val project = myProject
        when (nextNodeType) {
            NodeType.TechTier -> {
                val element = elementPointer.element
                val tierElements = ParadoxTechnologyManager.Stellaris.getAllTiers(project, element)
                tierElements.forEach { tierElement ->
                    val name = tierElement.name //= tierElement.definitionInfo?.name
                    descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, tierElement, false, name, type, NodeType.TechTier)
                }
            }
            NodeType.TechArea -> {
                val areaConfigs = ParadoxTechnologyManager.Stellaris.getAllResearchAreaConfigs(project)
                areaConfigs.forEach { config ->
                    val configElement = config.pointer.element ?: return@forEach
                    val name = config.value
                    descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, configElement, false, name, type, NodeType.TechArea)
                }
            }
            NodeType.TechCategory -> {
                val element = elementPointer.element
                val categoryElements = ParadoxTechnologyManager.Stellaris.getAllCategories(project, element)
                categoryElements.forEach { categoryElement ->
                    val name = categoryElement.name //= categoryElement.definitionInfo?.name
                    descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, categoryElement, false, name, type, NodeType.TechCategory)
                }
            }
            else -> {}
        }
    }

    private fun buildDefinitionChildren(descriptor: ParadoxDefinitionHierarchyNodeDescriptor, descriptors: MutableList<HierarchyNodeDescriptor>) {
        val project = myProject
        val typeName = buildString {
            append(typeConfig.name)
            if (descriptor.nodeType == NodeType.Subtype) append(".").append(descriptor.name)
        }
        val element = elementPointer.element
        val selector = selector(project, element).definition().withSearchScopeType(getHierarchySettings().scopeType)
        val definitions = ParadoxDefinitionSearch.search(typeName, selector).findAll()
        if (definitions.isEmpty()) return
        val groupingRules = getGroupingRules(descriptor)
        definitions.forEach f@{ definition ->
            if (!filterDefinitionChild(descriptor, definition, groupingRules)) return@f
            val isBase = element != null && element isSamePosition definition
            val name = definition.definitionInfo?.name ?: return@f
            descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, definition, isBase, name, type, NodeType.Definition)
        }
    }

    private fun getGroupingRules(descriptor: ParadoxDefinitionHierarchyNodeDescriptor): List<Pair<NodeType, String>> {
        return buildList {
            var currentDescriptor = descriptor
            while (true) {
                val currentNodeType = currentDescriptor.nodeType
                if (currentNodeType.grouped) {
                    this += tupleOf(currentNodeType, currentDescriptor.name)
                }
                currentDescriptor = currentDescriptor.parentDescriptor?.castOrNull<ParadoxDefinitionHierarchyNodeDescriptor>() ?: break
            }
        }.optimized()
    }

    private fun filterDefinitionChild(descriptor: ParadoxDefinitionHierarchyNodeDescriptor, definition: ParadoxScriptDefinitionElement, groupingRules: List<Tuple2<NodeType, String>>): Boolean {
        val definitionInfo = definition.definitionInfo ?: return false
        if (descriptor.nodeType == NodeType.NoSubtype && !definitionInfo.subtypes.isEmpty()) return false
        for ((nodeType, name) in groupingRules) {
            when {
                nodeType == NodeType.EventType -> {
                    if (definitionInfo.type != "event") return false
                    if (ParadoxEventManager.getType(definitionInfo) != name) return false
                }
                nodeType == NodeType.TechTier -> {
                    if (definitionInfo.type != "technology") return false
                    if (ParadoxTechnologyManager.Stellaris.getTier(definition) != name) return false
                }
                nodeType == NodeType.TechArea -> {
                    if (definitionInfo.type != "technology") return false
                    if (ParadoxTechnologyManager.Stellaris.getArea(definition) != name) return false
                }
                nodeType == NodeType.TechCategory -> {
                    if (definitionInfo.type != "technology") return false
                    if (!(ParadoxTechnologyManager.Stellaris.getCategories(definition).contains(name))) return false
                }
                else -> {}
            }
        }
        return true
    }

    private fun buildNestedDefinitionChildren(descriptor: ParadoxDefinitionHierarchyNodeDescriptor, descriptors: MutableList<HierarchyNodeDescriptor>) {
        val project = myProject
        val definition = descriptor.psiElement?.castOrNull<ParadoxScriptDefinitionElement>() ?: return
        val definitionInfo = definition.definitionInfo ?: return
        if (!type.predicate(definitionInfo)) return
        val selector = selector(project, definition).definition().withSearchScopeType(getHierarchySettings().scopeType)
        val nestedDefinitions = when (type) {
            Type.EventTreeInvoker -> ParadoxEventManager.getInvokerEvents(definition, selector)
            Type.EventTreeInvoked -> ParadoxEventManager.getInvokedEvents(definition, selector)
            Type.TechTreePre -> ParadoxTechnologyManager.Stellaris.getPreTechnologies(definition, selector)
            Type.TechTreePost -> ParadoxTechnologyManager.Stellaris.getPostTechnologies(definition, selector)
            else -> emptyList()
        }
        if (nestedDefinitions.isEmpty()) return
        val element = elementPointer.element
        nestedDefinitions.forEach f@{ nestedDefinition ->
            val isBase = element != null && element isSamePosition nestedDefinition
            val name = nestedDefinition.definitionInfo?.name ?: return@f
            descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, nestedDefinition, isBase, name, type, NodeType.Definition)
        }
    }

    override fun getLeafState(element: Any): LeafState {
        if (element is ParadoxScriptDefinitionElement && (type == Type.Type || type == Type.TypeAndSubtypes)) {
            return LeafState.NEVER
        }
        return LeafState.ASYNC
    }

    private fun getHierarchySettings() = ParadoxDefinitionHierarchyBrowserSettings.getInstance(myProject)
}
