package icu.windea.pls.lang.hierarchy.call

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.ide.hierarchy.HierarchyTreeStructure
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.PsiReference
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.tree.LeafState
import icu.windea.pls.PlsFacade
import icu.windea.pls.ep.resolve.ParadoxInlineSupport
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.localisationInfo
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.search.scope.type.ParadoxSearchScopeTypes
import icu.windea.pls.lang.selectFile
import icu.windea.pls.lang.settings.PlsSettingsState
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.isComplexExpression
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptMemberElement
import icu.windea.pls.script.psi.ParadoxScriptPsiUtil
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.isExpression

//com.intellij.ide.hierarchy.call.CallerMethodsTreeStructure

class ParadoxCalleeHierarchyTreeStructure(
    project: Project,
    baseDescriptor: ParadoxCallHierarchyNodeDescriptor,
    val baseDefinitionInfo: ParadoxDefinitionInfo?
) : HierarchyTreeStructure(project, baseDescriptor) {
    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<out HierarchyNodeDescriptor> {
        descriptor as ParadoxCallHierarchyNodeDescriptor
        val element = descriptor.psiElement ?: return HierarchyNodeDescriptor.EMPTY_ARRAY
        val descriptors = mutableMapOf<String, ParadoxCallHierarchyNodeDescriptor>()
        when {
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
        element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            var inInlineMath = false

            override fun visitElement(element: PsiElement) {
                //兼容向下内联的情况（即使内联后为自身）
                if (element is ParadoxScriptMemberElement) {
                    val inlined = ParadoxInlineSupport.getInlinedElement(element)
                    if (inlined != null) {
                        searchElement(inlined, descriptor, descriptors)
                        return
                    }
                }
                when {
                    element is ParadoxScriptedVariableReference -> {
                        addDescriptor(element) //scripted_variable
                    }
                    element is ParadoxScriptExpressionElement && element.isExpression() -> {
                        addDescriptor(element) //definition | localisation
                    }
                    element is ParadoxLocalisationExpressionElement && element.isComplexExpression() -> {
                        addDescriptor(element) //definition
                    }
                    element is ParadoxLocalisationParameter -> {
                        addDescriptor(element) //localisation
                    }
                }
                if (element is ParadoxScriptInlineMath) {
                    inInlineMath = true
                }
                if (!inInlineMath && !ParadoxScriptPsiUtil.isMemberContextElement(element)) return //optimize
                super.visitElement(element)
            }

            override fun elementFinished(element: PsiElement?) {
                if (element is ParadoxScriptInlineMath) {
                    inInlineMath = false
                }
            }

            private fun addDescriptor(element: PsiElement) {
                ProgressManager.checkCanceled()
                val references = element.references
                if (references.isEmpty()) return
                for (reference in references) {
                    var canResolve = false
                    canResolve = canResolve || (ParadoxResolveConstraint.ScriptedVariable.canResolve(reference) && hierarchySettings.showScriptedVariablesInCallHierarchy)
                    canResolve = canResolve || (ParadoxResolveConstraint.Definition.canResolve(reference) && hierarchySettings.showDefinitionsInCallHierarchy)
                    canResolve = canResolve || (ParadoxResolveConstraint.Localisation.canResolve(reference) && hierarchySettings.showLocalisationsInCallHierarchy)
                    if (!canResolve) continue

                    processReference(reference, scope, descriptor, descriptors, hierarchySettings)
                }
            }
        })
    }

    private fun processReference(
        reference: PsiReference,
        scope: GlobalSearchScope,
        descriptor: HierarchyNodeDescriptor,
        descriptors: MutableMap<String, ParadoxCallHierarchyNodeDescriptor>,
        settings: PlsSettingsState.HierarchyState
    ) {
        val resolved = reference.resolve()
        when (resolved) {
            is ParadoxScriptScriptedVariable -> {
                if (!settings.showScriptedVariablesInCallHierarchy) return //不显示
                val key = "v:${resolved.name}"
                if (descriptors.containsKey(key)) return //去重
                val resolvedFile = selectFile(resolved)
                if (resolvedFile != null && !scope.contains(resolvedFile)) return
                synchronized(descriptors) {
                    descriptors.put(key, ParadoxCallHierarchyNodeDescriptor(myProject, descriptor, resolved, false, false))
                }
            }
            is ParadoxScriptDefinitionElement -> {
                if (!settings.showDefinitionsInCallHierarchy) return //不显示
                val definitionInfo = resolved.definitionInfo ?: return
                if (!settings.showDefinitionsInCallHierarchyByBindings(baseDefinitionInfo, definitionInfo)) return //不显示
                val key = "d:${definitionInfo.name}: ${definitionInfo.type}"
                if (descriptors.containsKey(key)) return //去重
                val resolvedFile = selectFile(resolved)
                if (resolvedFile != null && !scope.contains(resolvedFile)) return
                synchronized(descriptors) {
                    descriptors.put(key, ParadoxCallHierarchyNodeDescriptor(myProject, descriptor, resolved, false, false))
                }
            }
            is ParadoxLocalisationProperty -> {
                if (!settings.showLocalisationsInCallHierarchy) return //不显示
                val localisationInfo = resolved.localisationInfo ?: return
                val key = "l:${localisationInfo.name}"
                if (descriptors.containsKey(key)) return //去重
                val resolvedFile = selectFile(resolved)
                if (resolvedFile != null && !scope.contains(resolvedFile)) return
                synchronized(descriptors) {
                    descriptors.put(key, ParadoxCallHierarchyNodeDescriptor(myProject, descriptor, resolved, false, false))
                }
            }
        }
    }

    override fun getLeafState(element: Any) = LeafState.ASYNC

    private fun getHierarchySettings() = ParadoxCallHierarchyBrowserSettings.getInstance(myProject)
}
