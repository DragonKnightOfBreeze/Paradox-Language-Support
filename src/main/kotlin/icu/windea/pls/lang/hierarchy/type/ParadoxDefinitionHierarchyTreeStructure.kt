package icu.windea.pls.lang.hierarchy.type

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.hierarchy.type.ParadoxDefinitionHierarchyNodeType as NodeType

class ParadoxDefinitionHierarchyTreeStructure(
    project: Project,
    element: PsiElement,
    typeElement: PsiElement,
    val typeConfig: CwtTypeConfig,
    val type: ParadoxDefinitionHierarchyType,
    nodeType: NodeType
) : HierarchyTreeStructure(project, ParadoxDefinitionHierarchyNodeDescriptor(project, null, typeElement, false, typeConfig.name, type, nodeType)) {
    private val elementPointer = element.createPointer()

    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<out HierarchyNodeDescriptor> {
        descriptor as ParadoxDefinitionHierarchyNodeDescriptor
        return when (descriptor.nodeType) {
            NodeType.Type -> {
                when (type) {
                    ParadoxDefinitionHierarchyType.TypeAndSubtypes -> {
                        val descriptors = mutableListOf<HierarchyNodeDescriptor>()
                        typeConfig.subtypes.forEach { (_, subtypeConfig) ->
                            val subtypeElement = subtypeConfig.pointer.element ?: return@forEach
                            descriptors.add(ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, subtypeElement, false, subtypeConfig.name, type, NodeType.Subtype))
                        }
                        val typeElement = descriptor.psiElement
                        if (typeElement != null) {
                            descriptors.add(ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, typeElement, false, "", type, NodeType.NoSubtype))
                        }
                        descriptors.toTypedArray()
                    }
                    else -> {
                        val descriptors = mutableListOf<HierarchyNodeDescriptor>()
                        val typeName = typeConfig.name
                        val contextElement = elementPointer.element
                        val selector = selector(myProject, contextElement).definition().withSearchScopeType(getHierarchySettings().scopeType)
                        val definitions = ParadoxDefinitionSearch.search(typeName, selector).findAll()
                        val element = elementPointer.element
                        definitions.forEach { definition ->
                            val isBase = element != null && element isSamePosition definition
                            descriptors.add(ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, definition, isBase, "", type, NodeType.Definition))
                        }
                        descriptors.toTypedArray()
                    }
                }
            }
            NodeType.Subtype -> {
                val descriptors = mutableListOf<HierarchyNodeDescriptor>()
                val typeName = "${typeConfig.name}.${descriptor.name}"
                val contextElement = elementPointer.element
                val selector = selector(myProject, contextElement).definition().withSearchScopeType(getHierarchySettings().scopeType)
                val definitions = ParadoxDefinitionSearch.search(typeName, selector).findAll()
                val element = elementPointer.element
                definitions.forEach { definition ->
                    val isBase = element != null && element isSamePosition definition
                    descriptors.add(ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, definition, isBase, "", type, NodeType.Definition))
                }
                return descriptors.toTypedArray()
            }
            NodeType.NoSubtype -> {
                val descriptors = mutableListOf<HierarchyNodeDescriptor>()
                val typeName = typeConfig.name
                val contextElement = elementPointer.element
                val selector = selector(myProject, contextElement).definition().withSearchScopeType(getHierarchySettings().scopeType)
                val definitions = ParadoxDefinitionSearch.search(typeName, selector).findAll()
                val element = elementPointer.element
                definitions.forEach { definition ->
                    if (definition.definitionInfo?.subtypes?.isEmpty() != true) return@forEach
                    val isBase = element != null && element isSamePosition definition
                    descriptors.add(ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, definition, isBase, "", type, NodeType.Definition))
                }
                descriptors.toTypedArray()
            }
            NodeType.Definition -> HierarchyNodeDescriptor.EMPTY_ARRAY
        }
    }

    private fun getHierarchySettings() = ParadoxDefinitionHierarchyBrowserSettings.getInstance(myProject)
}
