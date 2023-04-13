package icu.windea.pls.core.hierarchy.call

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.hierarchy.*
import icu.windea.pls.core.search.scope.type.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

class ParadoxCallerHierarchyTreeStructure(
    project: Project,
    psiElement: PsiElement
) : HierarchyTreeStructure(project, ParadoxCallHierarchyNodeDescriptor(project, null, psiElement, true, false)) {
    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<out HierarchyNodeDescriptor> {
        descriptor as ParadoxCallHierarchyNodeDescriptor
        val element = descriptor.psiElement ?: return HierarchyNodeDescriptor.EMPTY_ARRAY
        val scopeType = getScopeType()
        val scope = ParadoxSearchScopeTypes.get(scopeType).getGlobalSearchScope(myProject, element) ?: GlobalSearchScope.allScope(myProject)
        val descriptors = mutableMapOf<String, ParadoxCallHierarchyNodeDescriptor>()
        when {
            element is ParadoxScriptScriptedVariable || element is ParadoxScriptDefinitionElement -> {
                ReferencesSearch.search(element, scope).processQuery { reference ->
                    val referenceElement = reference.element
                    if(referenceElement.language == ParadoxScriptLanguage) {
                        //兼容向上内联的情况
                        val definition = referenceElement.findParentDefinition(link = true)
                        val definitionInfo = definition?.definitionInfo
                        if(definition != null && definitionInfo != null) {
                            val key = definitionInfo.name + ": " + definitionInfo.type
                            synchronized(descriptors) {
                                val d = descriptors.getOrPut(key) { ParadoxCallHierarchyNodeDescriptor(myProject, descriptor, definition, false, true) }
                                if(d.references.isNotEmpty() && !d.references.contains(reference)) {
                                    d.usageCount++
                                }
                                d.references.add(reference)
                            }
                        }
                    }
                    true
                }
            }
        }
        return descriptors.values.toTypedArray()
    }
    
    private fun getScopeType(): String {
        return ParadoxHierarchyBrowserSettings.getInstance(myProject).scopeTypes.get(ParadoxHierarchyBrowserSettings.CALL) ?: "all"
    }
}
