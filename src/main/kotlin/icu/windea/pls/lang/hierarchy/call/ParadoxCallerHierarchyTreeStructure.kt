package icu.windea.pls.lang.hierarchy.call

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.parentOfType
import com.intellij.ui.tree.LeafState
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.processQueryAsync
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.localisationInfo
import icu.windea.pls.lang.search.scope.type.ParadoxSearchScopeTypes
import icu.windea.pls.lang.settings.PlsSettingsState
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.findParentDefinition

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
