package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.psi.PsiElement
import icu.windea.pls.script.psi.tagType

abstract class ParadoxScriptTreeElement<T : PsiElement>(element: T) : PsiTreeElementBase<T>(element) {
    protected fun postHandleMemberChildren(children: MutableCollection<StructureViewTreeElement>) {
        if (children.isEmpty()) return

        //不要在结构视图中显示特殊标签
        children.removeIf { it is ParadoxScriptValueTreeElement && it.element?.tagType() != null }
    }
}
