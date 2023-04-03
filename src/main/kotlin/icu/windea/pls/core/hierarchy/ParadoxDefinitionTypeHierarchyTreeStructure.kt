package icu.windea.pls.core.hierarchy

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.chained.*

/**
 * @property withSubtypes 是否在层级树中显示定义的子类型。
 */
class ParadoxDefinitionTypeHierarchyTreeStructure(
    project: Project,
    element: PsiElement,
    typeElement: PsiElement,
    val typeConfig: CwtTypeConfig,
    val withSubtypes: Boolean,
) : HierarchyTreeStructure(project, ParadoxDefinitionHierarchyNodeDescriptor(project, null, typeElement, true)) {
    private val elementPointer = element.createPointer()
    
    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<out Any> {
        descriptor as ParadoxDefinitionHierarchyNodeDescriptor
        val descriptors = mutableListOf<HierarchyNodeDescriptor>()
        val type = typeConfig.name
        val contextElement = elementPointer.element
        val selector = definitionSelector(myProject, contextElement)
        val definitions = ParadoxDefinitionSearch.search(type, selector).findAll()
        if(withSubtypes) {
            val definitionsWithInfo = definitions.mapNotNull { definition ->
                val definitionInfo = definition.definitionInfo ?: return@mapNotNull null
                definition to definitionInfo
            }
            typeConfig.subtypes.forEach { (_, subtypeConfig) ->
                val subtypeElement = subtypeConfig.pointer.element ?: return@forEach
                val subtypeDescriptor = ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, subtypeElement, false)
                descriptors.add(subtypeDescriptor)
                definitionsWithInfo.forEach { (definition, definitionInfo) ->
                    if(definitionInfo.subtypes.contains(subtypeConfig.name)) {
                        descriptors.add(ParadoxDefinitionHierarchyNodeDescriptor(myProject, subtypeDescriptor, definition, false))
                    }
                }
            }
            definitionsWithInfo.forEach { (definition, definitionInfo) ->
                if(definitionInfo.subtypes.isEmpty()) {
                    descriptors.add(ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, definition, false))
                }
            }
        } else {
            definitions.forEach { definition ->
                descriptors.add(ParadoxDefinitionHierarchyNodeDescriptor(myProject, descriptor, definition, false))
            }
        }
        return descriptors.toTypedArray()
    }
}