package icu.windea.pls.cwt.codeInsight

import com.intellij.codeInsight.hint.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

/**
 * 用于为CWT语言指定快速定义显示的文本范围。
 * * 属性、单独的值：从目标元素向前直到没有空行为止的最后一个注释开始，到目标元素结束
 * * 其他：从目标元素开始，到目标元素结束
 */
class CwtImplementationTextSelectioner : ImplementationTextSelectioner {
    override fun getTextStartOffset(element: PsiElement): Int {
        return when {
            element is CwtProperty -> {
                findTextStartOffsetIncludeComment(element) { it.parent is CwtRootBlock }
            }
            element is CwtPropertyKey -> {
                return getTextStartOffset(element.parent) //使用对应的property的
            }
            element is CwtString && element.isBlockValue() -> {
                findTextStartOffsetIncludeComment(element) { it.parent is CwtRootBlock }
            }
            else -> {
                element.startOffset
            }
        }
    }

    override fun getTextEndOffset(element: PsiElement): Int {
        return element.endOffset
    }
}
