package icu.windea.pls.core.hierarchy.call

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import icu.windea.pls.core.*
import icu.windea.pls.core.hierarchy.type.*
import icu.windea.pls.core.search.scope.type.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

class ParadoxCallerHierarchyTreeStructure(
    project: Project,
    psiElement: PsiElement
) : HierarchyTreeStructure(project, ParadoxCallHierarchyNodeDescriptor(project, null, psiElement, true)) {
    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<out HierarchyNodeDescriptor> {
        //兼容向上内联的情况
        descriptor as ParadoxCallHierarchyNodeDescriptor
        val element = descriptor.psiElement ?: return HierarchyNodeDescriptor.EMPTY_ARRAY
        val scopeType = getScopeType()
        val scope = ParadoxSearchScopeTypes.get(scopeType).getGlobalSearchScope(myProject, element) ?: GlobalSearchScope.allScope(myProject)
        when {
            element is ParadoxScriptScriptedVariable -> {
                val descriptors = mutableListOf<ParadoxCallHierarchyNodeDescriptor>()
                ReferencesSearch.search(element, scope).processQuery {
                    val referenceElement = it.element
                    if(referenceElement.language == ParadoxScriptLanguage) {
                        val definition = referenceElement.findParentDefinition(link = true)
                        if(definition != null) {
                            descriptors.add(ParadoxCallHierarchyNodeDescriptor(myProject, descriptor, definition, false))
                        }
                    }
                    true
                }
                return descriptors.toTypedArray()
            }
            element is ParadoxScriptDefinitionElement -> {
                val descriptors = mutableListOf<ParadoxCallHierarchyNodeDescriptor>()
                ReferencesSearch.search(element, scope).processQuery {
                    val referenceElement = it.element
                    if(referenceElement.language == ParadoxScriptLanguage) {
                        val definition = referenceElement.findParentDefinition(link = true)
                        if(definition != null) {
                            descriptors.add(ParadoxCallHierarchyNodeDescriptor(myProject, descriptor, definition, false))
                        }
                    }
                    true
                }
                return descriptors.toTypedArray()
            }
        }
        return HierarchyNodeDescriptor.EMPTY_ARRAY
    }
    
    private fun getScopeType(): String {
        return ParadoxHierarchyBrowserSettings.getInstance(myProject).scopeTypes.get(ParadoxHierarchyBrowserSettings.CALL) ?: "all"
    }
}
