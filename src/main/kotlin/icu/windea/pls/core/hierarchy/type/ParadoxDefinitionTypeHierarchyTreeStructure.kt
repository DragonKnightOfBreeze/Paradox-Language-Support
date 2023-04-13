package icu.windea.pls.core.hierarchy.type

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.hierarchy.type.ParadoxDefinitionHierarchyNodeDescriptor.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*

/**
 * @property withSubtypes 是否在层级树中显示定义的子类型。
 */
class ParadoxDefinitionTypeHierarchyTreeStructure(
    project: Project,
    element: PsiElement,
    typeElement: PsiElement,
    val typeConfig: CwtTypeConfig,
    val withSubtypes: Boolean,
) : HierarchyTreeStructure(project, ParadoxDefinitionHierarchyNodeDescriptor(project, null, typeElement, false, typeConfig.name, Type.Type)) {
    private val elementPointer = element.createPointer()
    
    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<out HierarchyNodeDescriptor> {
        descriptor as ParadoxDefinitionHierarchyNodeDescriptor
        return when(descriptor.type) {
            Type.Type -> {
                if(withSubtypes) {
                    val descriptors = mutableListOf<HierarchyNodeDescriptor>()
                    typeConfig.subtypes.forEach { (_, subtypeConfig) ->
                        val subtypeElement = subtypeConfig.pointer.element ?: return@forEach
                        descriptors.add(ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, subtypeElement, false, subtypeConfig.name, Type.Subtype))
                    }
                    val typeElement = descriptor.psiElement
                    if(typeElement != null) {
                        descriptors.add(ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, typeElement, false, "", Type.NoSubtype))
                    }
                    descriptors.toTypedArray()
                } else {
                    val descriptors = mutableListOf<HierarchyNodeDescriptor>()
                    val type = typeConfig.name
                    val contextElement = elementPointer.element
                    val selector = definitionSelector(myProject, contextElement).withSearchScopeType(getHierarchySettings().scopeType)
                    val definitions = ParadoxDefinitionSearch.search(type, selector).findAll()
                    val element = elementPointer.element
                    definitions.forEach { definition ->
                        val isBase = element != null && element isSamePosition definition
                        descriptors.add(ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, definition, isBase, "", Type.Definition))
                    }
                    descriptors.toTypedArray()
                }
            }
            Type.Subtype -> {
                val descriptors = mutableListOf<HierarchyNodeDescriptor>()
                val type = "${typeConfig.name}.${descriptor.name}"
                val contextElement = elementPointer.element
                val selector = definitionSelector(myProject, contextElement).withSearchScopeType(getHierarchySettings().scopeType)
                val definitions = ParadoxDefinitionSearch.search(type, selector).findAll()
                val element = elementPointer.element
                definitions.forEach { definition ->
                    val isBase = element != null && element isSamePosition definition
                    descriptors.add(ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, definition, isBase, "", Type.Definition))
                }
                return descriptors.toTypedArray()
            }
            Type.NoSubtype -> {
                val descriptors = mutableListOf<HierarchyNodeDescriptor>()
                val type = typeConfig.name
                val contextElement = elementPointer.element
                val selector = definitionSelector(myProject, contextElement).withSearchScopeType(getHierarchySettings().scopeType)
                val definitions = ParadoxDefinitionSearch.search(type, selector).findAll()
                val element = elementPointer.element
                definitions.forEach { definition ->
                    if(definition.definitionInfo?.subtypes?.isEmpty() != true) return@forEach
                    val isBase = element != null && element isSamePosition definition
                    descriptors.add(ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, definition, isBase, "", Type.Definition))
                }
                descriptors.toTypedArray()
            }
            Type.Definition -> HierarchyNodeDescriptor.EMPTY_ARRAY
        }
    }
    
    private fun getHierarchySettings() = ParadoxDefinitionHierarchyBrowserSettings.getInstance(myProject)
}