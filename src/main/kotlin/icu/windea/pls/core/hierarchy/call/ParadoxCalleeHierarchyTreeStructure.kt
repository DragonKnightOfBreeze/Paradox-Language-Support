package icu.windea.pls.core.hierarchy.call

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.inline.*
import icu.windea.pls.script.psi.*
import java.util.*

class ParadoxCalleeHierarchyTreeStructure(
    project: Project,
    psiElement: PsiElement
) : HierarchyTreeStructure(project, ParadoxCallHierarchyNodeDescriptor(project, null, psiElement, true)) {
    override fun buildChildren(descriptor: HierarchyNodeDescriptor): Array<out HierarchyNodeDescriptor> {
        descriptor as ParadoxCallHierarchyNodeDescriptor
        val element = descriptor.psiElement ?: return HierarchyNodeDescriptor.EMPTY_ARRAY
        when {
            element is ParadoxScriptDefinitionElement -> {
                val descriptors = mutableListOf<ParadoxCallHierarchyNodeDescriptor>()
                processElement(element, descriptor, descriptors)
                return descriptors.toTypedArray()
            }
        }
        return HierarchyNodeDescriptor.EMPTY_ARRAY
    }
    
    private fun processElement(element: PsiElement, descriptor: HierarchyNodeDescriptor, descriptors: MutableList<ParadoxCallHierarchyNodeDescriptor>) {
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
                if(element is ParadoxScriptExpressionElement && element.isExpression()) {
                    addDescriptor(element)
                }
                if(element is ParadoxScriptedVariableReference) {
                    addDescriptor(element)
                }
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
            
            private fun addDescriptor(element: ParadoxScriptExpressionElement) {
                val resolved = element.reference?.resolve() as? ParadoxScriptDefinitionElement
                if(resolved == null) return
                if(resolved.definitionInfo == null) return
                descriptors.add(ParadoxCallHierarchyNodeDescriptor(myProject, descriptor, resolved, false))
            }
            
            private fun addDescriptor(element: ParadoxScriptedVariableReference) {
                val resolved = element.reference.resolve()
                if(resolved == null) return
                descriptors.add(ParadoxCallHierarchyNodeDescriptor(myProject, descriptor, resolved, false))
            }
        })
    }
}