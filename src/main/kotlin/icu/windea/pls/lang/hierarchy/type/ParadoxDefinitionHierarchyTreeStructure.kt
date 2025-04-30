package icu.windea.pls.lang.hierarchy.type

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.hierarchy.type.ParadoxDefinitionHierarchyNodeType
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
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
        when (nodeType) {
            NodeType.Type -> {
                if (type == Type.TypeAndSubtypes) {
                    buildConfigChildren(descriptors, descriptor)
                } else {
                    buildDefinitionChildren(descriptor, descriptors)
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
            ParadoxDefinitionHierarchyNodeType.EventAttribute -> TODO()
            ParadoxDefinitionHierarchyNodeType.EventType -> TODO()
            ParadoxDefinitionHierarchyNodeType.TechAttribute -> TODO()
            ParadoxDefinitionHierarchyNodeType.TechSearchArea -> TODO()
            ParadoxDefinitionHierarchyNodeType.TechTier -> TODO()
        }
        if (descriptors.isEmpty()) return HierarchyNodeDescriptor.EMPTY_ARRAY
        return descriptors.toTypedArray()
    }

    private fun buildConfigChildren(descriptors: MutableList<HierarchyNodeDescriptor>, descriptor: ParadoxDefinitionHierarchyNodeDescriptor) {
        typeConfig.subtypes.forEach { (_, subtypeConfig) ->
            val subtypeElement = subtypeConfig.pointer.element ?: return@forEach
            descriptors += ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, subtypeElement, false, subtypeConfig.name, this.type, NodeType.Subtype)
        }
        val typeElement = descriptor.psiElement ?: return
        descriptors += ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, typeElement, false, "", this.type, NodeType.NoSubtype)
    }

    private fun buildDefinitionChildren(descriptor: ParadoxDefinitionHierarchyNodeDescriptor, descriptors: MutableList<HierarchyNodeDescriptor>) {
        val typeName = buildString {
            append(typeConfig.name)
            if (descriptor.nodeType == NodeType.Subtype) append(".").append(descriptor.name)
        }
        val contextElement = elementPointer.element
        val selector = selector(myProject, contextElement).definition().withSearchScopeType(getHierarchySettings().scopeType)
        val definitions = ParadoxDefinitionSearch.search(typeName, selector).findAll()
        if (definitions.isEmpty()) return
        val element = elementPointer.element
        definitions.forEach { definition ->
            if (descriptor.nodeType == NodeType.NoSubtype) {
                if (definition.definitionInfo?.subtypes?.isEmpty() != true) return@forEach
            }
            val isBase = element != null && element isSamePosition definition
            descriptors += ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, definition, isBase, "", type, NodeType.Definition)
        }
    }

    private fun buildNestedDefinitionChildren(descriptor: ParadoxDefinitionHierarchyNodeDescriptor, descriptors: MutableList<HierarchyNodeDescriptor>) {
        val definition = descriptor.psiElement?.castOrNull<ParadoxScriptDefinitionElement>() ?: return
        val definitionInfo = definition.definitionInfo ?: return
        if (!type.predicate(definitionInfo)) return
        val selector = selector(myProject, definition).definition().withSearchScopeType(getHierarchySettings().scopeType)
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
            descriptors += ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, nestedDefinition, isBase, "", type, NodeType.Definition)
        }
    }

    private fun getHierarchySettings() = ParadoxDefinitionHierarchyBrowserSettings.getInstance(myProject)
}
