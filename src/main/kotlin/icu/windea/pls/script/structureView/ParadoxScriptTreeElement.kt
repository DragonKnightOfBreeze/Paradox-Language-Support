package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.structureView.impl.common.*
import com.intellij.psi.*

abstract class ParadoxScriptTreeElement<T : PsiElement>(element: T) : PsiTreeElementBase<T>(element) {
    protected fun postHandleMemberChildren(children: MutableCollection<StructureViewTreeElement>) {
        if (children.size <= 1) return

        val hasProperty = children.any { it is ParadoxScriptPropertyTreeElement }
        val hasValue = children.any { it is ParadoxScriptValueTreeElement }
        if (hasProperty && hasValue) {
            //不要在结构视图中显示特殊标签
            children.removeIf { it is ParadoxScriptValueTreeElement && it.isTag() }
        }
    }
}
