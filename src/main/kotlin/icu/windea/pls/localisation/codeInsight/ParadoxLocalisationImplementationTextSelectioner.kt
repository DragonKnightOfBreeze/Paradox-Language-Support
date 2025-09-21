package icu.windea.pls.localisation.codeInsight

import com.intellij.codeInsight.hint.ImplementationTextSelectioner
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.util.psi.PlsPsiManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiUtil

/**
 * 用于为本地化语言指定快速定义显示的文本范围。
 *
 * * 属性：从目标元素向前直到没有空行为止的最后一个注释开始，到目标元素结束
 * * 其他：从目标元素开始，到目标元素结束
 */
class ParadoxLocalisationImplementationTextSelectioner : ImplementationTextSelectioner {
    override fun getTextStartOffset(element: PsiElement): Int {
        val canAttachComment = ParadoxLocalisationPsiUtil.canAttachComment(element)
        return PlsPsiManager.findTextStartOffsetInView(element, canAttachComment)
    }

    override fun getTextEndOffset(element: PsiElement): Int {
        return PlsPsiManager.findTextEndOffsetInView(element)
    }
}
