package icu.windea.pls.lang.hierarchy.call

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.psi.util.*
import com.intellij.ui.tree.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.lang.search.scope.type.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

//com.intellij.ide.hierarchy.call.CallerMethodsTreeStructure

class ParadoxCallerHierarchyTreeStructure(
    project: Project,
    baseDescriptor: ParadoxCallHierarchyNodeDescriptor,
    val baseDefinitionInfo: ParadoxDefinitionInfo?
) : HierarchyTreeStructure(project, baseDescriptor) {
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
        if (descriptors.values.isEmpty()) return HierarchyNodeDescriptor.EMPTY_ARRAY
        return descriptors.values.toTypedArray()
    }

    private fun searchElement(element: PsiElement, descriptor: HierarchyNodeDescriptor, descriptors: MutableMap<String, ParadoxCallHierarchyNodeDescriptor>) {
        val hierarchySettings = PlsFacade.getSettings().hierarchy
        val scopeType = getHierarchySettings().scopeType
        val scope = ParadoxSearchScopeTypes.get(scopeType).getGlobalSearchScope(myProject, element)
            ?: GlobalSearchScope.allScope(myProject)
        ReferencesSearch.search(element, scope).processQueryAsync { reference ->
            ProgressManager.checkCanceled()
            processReference(reference, descriptor, descriptors, hierarchySettings)
            true
        }
    }

    private fun processReference(
        reference: PsiReference,
        descriptor: HierarchyNodeDescriptor,
        descriptors: MutableMap<String, ParadoxCallHierarchyNodeDescriptor>,
        settings: PlsSettingsState.HierarchyState
    ) {
        val referenceElement = reference.element
        when (referenceElement.language) {
            is ParadoxScriptLanguage -> {
                if (!settings.showDefinitionsInCallHierarchy) return //不显示
                val definition = referenceElement.findParentDefinition()
                val definitionInfo = definition?.definitionInfo
                if (definition == null || definitionInfo == null) return
                ProgressManager.checkCanceled()
                if (!settings.showDefinitionsInCallHierarchyByBindings(baseDefinitionInfo, definitionInfo)) return //不显示
                val key = "d:${definitionInfo.name}: ${definitionInfo.type}"
                val d = synchronized(descriptors) {
                    descriptors.getOrPut(key) { ParadoxCallHierarchyNodeDescriptor(myProject, descriptor, definition, false, true) }
                }
                if (d.references.isNotEmpty() && !d.references.contains(reference)) {
                    d.usageCount++
                }
                d.references.add(reference)
            }
            is ParadoxLocalisationLanguage -> {
                if (!settings.showLocalisationsInCallHierarchy) return  //不显示
                //兼容向上内联的情况
                val localisation = referenceElement.parentOfType<ParadoxLocalisationProperty>()
                val localisationInfo = localisation?.localisationInfo
                if (localisation == null || localisationInfo == null) return
                ProgressManager.checkCanceled()
                val key = "l:${localisationInfo.name}"
                val d = synchronized(descriptors) {
                    descriptors.getOrPut(key) { ParadoxCallHierarchyNodeDescriptor(myProject, descriptor, localisation, false, true) }
                }
                if (d.references.isNotEmpty() && !d.references.contains(reference)) {
                    d.usageCount++
                }
                d.references.add(reference)
            }
        }
    }

    override fun getLeafState(element: Any) = LeafState.ASYNC

    private fun getHierarchySettings() = ParadoxCallHierarchyBrowserSettings.getInstance(myProject)
}
