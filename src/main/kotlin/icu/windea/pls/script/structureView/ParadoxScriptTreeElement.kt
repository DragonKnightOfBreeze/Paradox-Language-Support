package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.psi.PsiElement
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.tagType

abstract class ParadoxScriptTreeElement<T : PsiElement>(element: T) : PsiTreeElementBase<T>(element) {
    protected fun postHandleMemberChildren(children: MutableCollection<StructureViewTreeElement>) {
        if (children.isEmpty()) return

        // 不要在结构视图中显示特殊标签
        children.removeIf { it is ParadoxScriptValueTreeElement && it.element?.tagType() != null }
    }

    protected fun getTreeElement(element: PsiElement): ParadoxScriptTreeElement<out PsiElement>? {
        return when (element) {
            is ParadoxScriptScriptedVariable -> ParadoxScriptVariableTreeElement(element)
            is ParadoxScriptProperty -> ParadoxScriptPropertyTreeElement(element)
            is ParadoxScriptValue -> ParadoxScriptValueTreeElement(element)
            is ParadoxScriptParameterCondition -> ParadoxScriptParameterConditionTreeElement(element)
            else -> null
        }
    }
}
