package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.hint.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.lang.util.PlsPsiManager
import icu.windea.pls.script.psi.*

/**
 * 用于为脚本语言指定快速定义显示的文本范围。
 *
 * * 属性、单独的值：从目标元素向前直到没有空行为止的最后一个注释开始，到目标元素结束
 * * 其他：从目标元素开始，到目标元素结束
 */
class ParadoxScriptImplementationTextSelectioner : ImplementationTextSelectioner {
    override fun getTextStartOffset(element: PsiElement): Int {
        return when {
            element is ParadoxScriptProperty -> {
                PlsPsiManager.findTextStartOffsetIncludeComment(element) { it.parent is ParadoxScriptRootBlock }
            }
            element is ParadoxScriptPropertyKey -> {
                getTextStartOffset(element.parent)
            }
            element is ParadoxScriptString && element.isBlockMember() -> {
                PlsPsiManager.findTextStartOffsetIncludeComment(element) { it.parent is ParadoxScriptRootBlock }
            }
            else -> {
                element.startOffset
            }
        }
    }

    override fun getTextEndOffset(element: PsiElement): Int {
        return when {
            element is ParadoxScriptPropertyKey -> {
                getTextEndOffset(element.parent)
            }
            else -> {
                element.endOffset
            }
        }
    }
}
