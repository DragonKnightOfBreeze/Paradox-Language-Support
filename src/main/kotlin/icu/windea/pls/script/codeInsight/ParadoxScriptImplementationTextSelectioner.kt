package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.hint.ImplementationTextSelectioner
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.util.PlsPsiManager
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptPsiUtil

/**
 * 用于为脚本语言指定快速定义显示的文本范围。
 *
 * * 属性、单独的值：从目标元素向前直到没有空行为止的最后一个注释开始，到目标元素结束
 * * 其他：从目标元素开始，到目标元素结束
 */
class ParadoxScriptImplementationTextSelectioner : ImplementationTextSelectioner {
    override fun getTextStartOffset(element: PsiElement): Int {
        if (element is ParadoxScriptPropertyKey) return getTextStartOffset(element.parent)
        val canAttachComment = ParadoxScriptPsiUtil.canAttachComment(element)
        return PlsPsiManager.findTextStartOffsetInView(element, canAttachComment)
    }

    override fun getTextEndOffset(element: PsiElement): Int {
        if (element is ParadoxScriptPropertyKey) return getTextStartOffset(element.parent)
        return PlsPsiManager.findTextEndOffsetInView(element)
    }
}
