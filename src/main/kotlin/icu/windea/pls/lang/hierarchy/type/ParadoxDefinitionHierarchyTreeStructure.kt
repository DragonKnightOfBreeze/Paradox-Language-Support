package icu.windea.pls.lang.hierarchy.type

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.ui.tree.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
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
        when (nodeType) {
            NodeType.Type -> {
                when (type) {
                    Type.Type -> {
                        buildDefinitionChildren(descriptor, descriptors)
                    }
                    Type.TypeAndSubtypes -> {
                        buildSubtypeConfigChildren(descriptors, descriptor)
                    }
                    Type.EventTreeInvoker, Type.EventTreeInvoked -> {
                        buildEventTreeChildren(descriptor, descriptors)
                    }
                    Type.TechTreePre, Type.TechTreePost -> {
                        buildTechTreeChildren(descriptor, descriptors)
                    }
                }
            }
            NodeType.Subtype, NodeType.NoSubtype -> {
                buildDefinitionChildren(descriptor, descriptors)
            }
            NodeType.Definition -> {
                if (type.nested) {
                    buildNestedDefinitionChildren(descriptor, descriptors)
                }
            }
            else -> {
                buildDefinitionChildren(descriptor, descriptors)
            }
        }
        if (descriptors.isEmpty()) return HierarchyNodeDescriptor.EMPTY_ARRAY
        return descriptors.toTypedArray()
    }

    private fun buildSubtypeConfigChildren(descriptors: MutableList<HierarchyNodeDescriptor>, descriptor: ParadoxDefinitionHierarchyNodeDescriptor) {
        val project = myProject
        typeConfig.subtypes.forEach { (_, subtypeConfig) ->
            val subtypeElement = subtypeConfig.pointer.element ?: return@forEach
            descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, subtypeElement, false, subtypeConfig.name, this.type, NodeType.Subtype)
        }
        val typeElement = descriptor.psiElement ?: return
        descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, typeElement, false, "", this.type, NodeType.NoSubtype)
    }

    private fun buildEventTreeChildren(descriptor: ParadoxDefinitionHierarchyNodeDescriptor, descriptors: MutableList<HierarchyNodeDescriptor>) {
        val groupingStrategy = getSettings().hierarchy.eventTreeGrouping
        doBuildEventTreeChildren(descriptor, descriptors, groupingStrategy)
    }

    private fun doBuildEventTreeChildren(
        descriptor: ParadoxDefinitionHierarchyNodeDescriptor,
        descriptors: MutableList<HierarchyNodeDescriptor>,
        groupingStrategy: ParadoxStrategies.EventTreeGrouping
    ) {
        val gameType = typeConfig.configGroup.gameType
        if (gameType == null) return

        val project = myProject
        when (groupingStrategy) {
            ParadoxStrategies.EventTreeGrouping.None -> {
                buildDefinitionChildren(descriptor, descriptors)
            }
            ParadoxStrategies.EventTreeGrouping.Type -> {
                val eventTypeConfigs = ParadoxEventManager.getAllTypeConfigs(project, gameType)
                eventTypeConfigs.forEach { config ->
                    val configElement = config.pointer.element ?: return@forEach
                    val name = config.name
                    descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, configElement, false, name, type, NodeType.EventType)
                }
            }
        }
    }

    private fun buildTechTreeChildren(descriptor: ParadoxDefinitionHierarchyNodeDescriptor, descriptors: MutableList<HierarchyNodeDescriptor>) {
        val groupingStrategy = getSettings().hierarchy.techTreeGrouping
        doBuildTechTreeChildren(descriptor, descriptors, groupingStrategy)
    }

    private fun doBuildTechTreeChildren(
        descriptor: ParadoxDefinitionHierarchyNodeDescriptor,
        descriptors: MutableList<HierarchyNodeDescriptor>,
        groupingStrategy: ParadoxStrategies.TechTreeGrouping
    ) {
        val gameType = typeConfig.configGroup.gameType
        if (gameType != ParadoxGameType.Stellaris) return

        val project = myProject
        when (groupingStrategy) {
            ParadoxStrategies.TechTreeGrouping.None -> {
                buildDefinitionChildren(descriptor, descriptors)
            }
            ParadoxStrategies.TechTreeGrouping.Tier -> {
                val element = elementPointer.element
                val tierElements = ParadoxTechnologyManager.Stellaris.getAllTiers(project, element)
                tierElements.forEach { tierElement ->
                    val name = tierElement.name //= tierElement.definitionInfo?.name
                    descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, tierElement, false, name, type, NodeType.TechTier)
                }
            }
            ParadoxStrategies.TechTreeGrouping.Area -> {
                val areaConfigs = ParadoxTechnologyManager.Stellaris.getAllResearchAreaConfigs(project)
                areaConfigs.forEach { config ->
                    val configElement = config.pointer.element ?: return@forEach
                    val name = config.value
                    descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, configElement, false, name, type, NodeType.TechArea)
                }
            }
            ParadoxStrategies.TechTreeGrouping.Category -> {
                val element = elementPointer.element
                val categoryElements = ParadoxTechnologyManager.Stellaris.getAllCategories(project, element)
                categoryElements.forEach { categoryElement ->
                    val name = categoryElement.name //= categoryElement.definitionInfo?.name
                    descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, categoryElement, false, name, type, NodeType.TechCategory)
                }
            }
            ParadoxStrategies.TechTreeGrouping.Area2Category -> {
                val parentNodeType = descriptor.parentDescriptor?.castOrNull<ParadoxDefinitionHierarchyNodeDescriptor>()?.nodeType
                when (parentNodeType) {
                    NodeType.Type -> {
                        doBuildTechTreeChildren(descriptor, descriptors, ParadoxStrategies.TechTreeGrouping.Area)
                    }
                    NodeType.TechArea -> {
                        doBuildTechTreeChildren(descriptor, descriptors, ParadoxStrategies.TechTreeGrouping.Category)
                    }
                    else -> {} //unexpected
                }
            }
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
            descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, definition, isBase, "", type, NodeType.Definition)
        }
    }

    private fun getGroupingRules(descriptor: ParadoxDefinitionHierarchyNodeDescriptor): List<Pair<NodeType, String>> {
        return buildList {
            var currentDescriptor = descriptor
            while (true) {
                currentDescriptor = currentDescriptor.parentDescriptor?.castOrNull<ParadoxDefinitionHierarchyNodeDescriptor>() ?: break
                val currentNodeType = currentDescriptor.nodeType
                if (currentNodeType.grouped) {
                    this += tupleOf(currentNodeType, currentDescriptor.name)
                }
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
        nestedDefinitions.forEach { nestedDefinition ->
            val isBase = element != null && element isSamePosition nestedDefinition
            descriptors += ParadoxDefinitionHierarchyNodeDescriptor(project, descriptor, nestedDefinition, isBase, "", type, NodeType.Definition)
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
