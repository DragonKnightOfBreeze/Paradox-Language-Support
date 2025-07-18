package icu.windea.pls.cwt.codeInsight

import com.intellij.codeInsight.hint.*
import com.intellij.psi.*
import com.intellij.psi.util.*
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
        return when {
            element is CwtProperty -> {
                PlsPsiManager.findTextStartOffsetIncludeComment(element) { it.parent is CwtRootBlock }
            }
            element is CwtPropertyKey -> {
                getTextStartOffset(element.parent)
            }
            element is CwtString && element.isBlockValue() -> {
                PlsPsiManager.findTextStartOffsetIncludeComment(element) { it.parent is CwtRootBlock }
            }
            else -> {
                element.startOffset
            }
        }
    }

    override fun getTextEndOffset(element: PsiElement): Int {
        return when {
            element is CwtPropertyKey -> {
                getTextEndOffset(element.parent)
            }
            else -> element.endOffset
        }
    }
}
