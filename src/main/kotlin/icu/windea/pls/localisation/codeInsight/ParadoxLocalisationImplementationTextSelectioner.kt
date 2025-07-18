package icu.windea.pls.localisation.codeInsight

import com.intellij.codeInsight.hint.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * 用于为本地化语言指定快速定义显示的文本范围。
 *
 * * 属性：从目标元素向前直到没有空行为止的最后一个注释开始，到目标元素结束
 * * 其他：从目标元素开始，到目标元素结束
 */
class ParadoxLocalisationImplementationTextSelectioner : ImplementationTextSelectioner {
    override fun getTextStartOffset(element: PsiElement): Int {
        return when {
            element is ParadoxLocalisationProperty -> {
                PlsPsiManager.findTextStartOffsetIncludeComment(element) { it.parent is ParadoxScriptRootBlock }
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
