package icu.windea.pls.core.hierarchy.call

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.hierarchy.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.scope.type.*
import icu.windea.pls.lang.inline.*
import icu.windea.pls.script.psi.*
import java.util.*

class ParadoxCalleeHierarchyTreeStructure(
    project: Project,
    psiElement: PsiElement
) : HierarchyTreeStructure(project, ParadoxCallHierarchyNodeDescriptor(project, null, psiElement, true, false)) {
    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<out HierarchyNodeDescriptor> {
        descriptor as ParadoxCallHierarchyNodeDescriptor
        val element = descriptor.psiElement ?: return HierarchyNodeDescriptor.EMPTY_ARRAY
        val descriptors = mutableMapOf<String, ParadoxCallHierarchyNodeDescriptor>()
        when {
            element is ParadoxScriptDefinitionElement -> {
                processElement(element, descriptor, descriptors)
            }
        }
        return descriptors.values.toTypedArray()
    }
    
    private fun processElement(element: PsiElement, descriptor: HierarchyNodeDescriptor, descriptors: MutableMap<String, ParadoxCallHierarchyNodeDescriptor>) {
        val scopeType = getScopeType()
        val scope = ParadoxSearchScopeTypes.get(scopeType).getGlobalSearchScope(myProject, element)
        element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                //兼容向下内联的情况
                if(element is ParadoxScriptMemberElement) {
                    val inlined = ParadoxScriptMemberElementInlineSupport.inlineElement(element, LinkedList())
                    if(inlined != null) {
                        processElement(inlined, descriptor, descriptors)
                        return
                    }
                }
                if(element is ParadoxScriptedVariableReference) {
                    addDescriptor(element)
                }
                if(element is ParadoxScriptExpressionElement && element.isExpression()) {
                    addDescriptor(element)
                }
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
            
            private fun addDescriptor(element: ParadoxScriptedVariableReference) {
                val resolved = element.reference.resolve()
                if(resolved == null) return
                val key = resolved.name
                if(descriptors.containsKey(key)) return //去重
                val resolvedFile = selectFile(resolved)
                if(resolvedFile != null && scope != null && !scope.contains(resolvedFile)) return
                descriptors.put(key, ParadoxCallHierarchyNodeDescriptor(myProject, descriptor, resolved, false, false))
            }
            
            private fun addDescriptor(element: ParadoxScriptExpressionElement) {
                val resolved = element.reference?.resolve() as? ParadoxScriptDefinitionElement
                if(resolved == null) return
                val definitionInfo = resolved.definitionInfo ?: return
                val key = definitionInfo.name + ": " + definitionInfo.type
                if(descriptors.containsKey(key)) return //去重
                val resolvedFile = selectFile(resolved)
                if(resolvedFile != null && scope != null && !scope.contains(resolvedFile)) return
                descriptors.put(key, ParadoxCallHierarchyNodeDescriptor(myProject, descriptor, resolved, false, false))
            }
        })
    }
    
    private fun getScopeType(): String {
        return ParadoxHierarchyBrowserSettings.getInstance(myProject).scopeTypes.get(ParadoxHierarchyBrowserSettings.CALL) ?: "all"
    }
}