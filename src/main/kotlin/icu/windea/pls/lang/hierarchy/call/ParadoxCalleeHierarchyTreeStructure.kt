package icu.windea.pls.lang.hierarchy.call

import cn.yiiguxing.plugin.translate.util.*
import com.intellij.ide.hierarchy.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.ui.tree.*
import icu.windea.pls.ep.inline.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.scope.type.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*

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
        val hierarchySettings = getSettings().hierarchy
        val scopeType = getHierarchySettings().scopeType
        val scope = ParadoxSearchScopeTypes.get(scopeType).getGlobalSearchScope(myProject, element)
            ?: GlobalSearchScope.allScope(myProject)
        element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            var inInlineMath = false

            override fun visitElement(element: PsiElement) {
                //兼容向下内联的情况（即使内联后为自身）
                if (element is ParadoxScriptMemberElement) {
                    val inlined = ParadoxInlineSupport.inlineElement(element)
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
                    element is ParadoxLocalisationPropertyReference -> {
                        addDescriptor(element) //localisation
                    }
                }
                if (element is ParadoxScriptInlineMath) {
                    inInlineMath = true
                }
                if (!inInlineMath && element.elementType !in ParadoxScriptTokenSets.MEMBER_CONTEXT) return //optimize
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
