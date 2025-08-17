package icu.windea.pls.cwt.codeInsight

import com.intellij.codeInsight.hint.*
import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.util.*

/**
 * 用于为CWT语言指定快速定义显示的文本范围。
 *
 * * 属性、单独的值：从目标元素向前直到没有空行为止的最后一个注释开始，到目标元素结束
 * * 其他：从目标元素开始，到目标元素结束
 */
class CwtImplementationTextSelectioner : ImplementationTextSelectioner {
    override fun getTextStartOffset(element: PsiElement): Int {
        if (element is CwtPropertyKey) return getTextStartOffset(element.parent)
        val canAttachComment = CwtPsiUtil.canAttachComment(element)
        return PlsPsiManager.findTextStartOffsetInView(element, canAttachComment)
    }

    override fun getTextEndOffset(element: PsiElement): Int {
        if (element is CwtPropertyKey) return getTextStartOffset(element.parent)
        return PlsPsiManager.findTextEndOffsetInView(element)
    }
}
