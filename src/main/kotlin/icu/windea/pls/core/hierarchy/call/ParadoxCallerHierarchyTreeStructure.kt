package icu.windea.pls.core.hierarchy.call

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.scope.type.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

//com.intellij.ide.hierarchy.call.CallerMethodsTreeStructure

class ParadoxCallerHierarchyTreeStructure(
    project: Project,
    element: PsiElement,
    val rootDefinitionInfo: ParadoxDefinitionInfo?
) : HierarchyTreeStructure(project, ParadoxCallHierarchyNodeDescriptor(project, null, element, true, false)) {
    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<out HierarchyNodeDescriptor> {
        descriptor as ParadoxCallHierarchyNodeDescriptor
        val element = descriptor.psiElement ?: return HierarchyNodeDescriptor.EMPTY_ARRAY
        val descriptors = mutableMapOf<String, ParadoxCallHierarchyNodeDescriptor>()
        when {
            element is ParadoxScriptScriptedVariable -> {
                searchElement(element, descriptor, descriptors)
            }
            element is ParadoxScriptDefinitionElement -> {
                searchElement(element, descriptor, descriptors)
            }
            element is ParadoxLocalisationProperty -> {
                searchElement(element, descriptor, descriptors)
            }
        }
        return descriptors.values.toTypedArray()
    }
    
    private fun searchElement(element: PsiElement, descriptor: HierarchyNodeDescriptor, descriptors: MutableMap<String, ParadoxCallHierarchyNodeDescriptor>) {
        val scopeType = getHierarchySettings().scopeType
        val scope = ParadoxSearchScopeTypes.get(scopeType).getGlobalSearchScope(myProject, element)
            ?: GlobalSearchScope.allScope(myProject)
        ReferencesSearch.search(element, scope).processQuery { reference ->
            ProgressManager.checkCanceled()
            val referenceElement = reference.element
            if(referenceElement.language == ParadoxScriptLanguage) {
                processScriptReferenceElement(reference, referenceElement, descriptor, descriptors)
            } else if(referenceElement.language == ParadoxLocalisationLanguage) {
                processLocalisationReferenceElement(reference, referenceElement, descriptor, descriptors)
            }
            true
        }
    }
    
    private fun processScriptReferenceElement(reference: PsiReference, referenceElement: PsiElement, descriptor: HierarchyNodeDescriptor, descriptors: MutableMap<String, ParadoxCallHierarchyNodeDescriptor>) {
        if(!getSettings().hierarchy.showDefinitionsInCallHierarchy) return //不显示
        //兼容向上内联的情况
        val definition = referenceElement.findParentDefinition(link = true)
        val definitionInfo = definition?.definitionInfo
        if(definition != null && definitionInfo != null) {
            ProgressManager.checkCanceled()
            if(!getSettings().hierarchy.showDefinitionsInCallHierarchy(rootDefinitionInfo, definitionInfo)) return //不显示
            val key = "d:${definitionInfo.name}: ${definitionInfo.type}"
            synchronized(descriptors) {
                val d = descriptors.getOrPut(key) { ParadoxCallHierarchyNodeDescriptor(myProject, descriptor, definition, false, true) }
                if(d.references.isNotEmpty() && !d.references.contains(reference)) {
                    d.usageCount++
                }
                d.references.add(reference)
            }
        }
    }
    
    private fun processLocalisationReferenceElement(reference: PsiReference, referenceElement: PsiElement, descriptor: HierarchyNodeDescriptor, descriptors: MutableMap<String, ParadoxCallHierarchyNodeDescriptor>) {
        if(!getSettings().hierarchy.showLocalisationsInCallHierarchy) return //不显示
        //兼容向上内联的情况
        val localisation = referenceElement.parentOfType<ParadoxLocalisationProperty>()
        val localisationInfo = localisation?.localisationInfo
        if(localisation != null && localisationInfo != null) {
            ProgressManager.checkCanceled()
            val key = "l:${localisationInfo.name}"
            //synchronized is used in CallerMethodsTreeStructure for descriptor map
            //to optimize performance, no synchronized here and it should be ok
            val d = descriptors.getOrPut(key) { ParadoxCallHierarchyNodeDescriptor(myProject, descriptor, localisation, false, true) }
            if(d.references.isNotEmpty() && !d.references.contains(reference)) {
                d.usageCount++
            }
            d.references.add(reference)
        }
    }
    
    private fun getHierarchySettings() = ParadoxCallHierarchyBrowserSettings.getInstance(myProject)
}
