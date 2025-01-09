package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.hint.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 用于为脚本语言指定快速定义显示的文本范围。
 * * 属性、键、单独的值：从目标元素向前直到没有空行为止的最后一个注释开始，到目标元素结束
 * * 其他：从目标元素开始，到目标元素结束
 */
class ParadoxScriptImplementationTextSelectioner : ImplementationTextSelectioner {
    override fun getTextStartOffset(element: PsiElement): Int {
        return when {
            element is ParadoxScriptProperty -> {
                findTextStartOffsetIncludeComment(element) { it.parent is ParadoxScriptRootBlock }
            }
            element is ParadoxScriptPropertyKey -> {
                //处理对应的property是定义的特殊情况
                val parent = element.parent
                val isDefinition = parent?.castOrNull<ParadoxScriptProperty>()?.definitionInfo != null
                if (isDefinition) return getTextStartOffset(parent)
                findTextStartOffsetIncludeComment(element) { it.parent is ParadoxScriptRootBlock }
            }
            element is ParadoxScriptString && element.isBlockMember() -> {
                findTextStartOffsetIncludeComment(element) { it.parent is ParadoxScriptRootBlock }
            }
            else -> {
                element.startOffset
            }
        }
    }

    override fun getTextEndOffset(element: PsiElement): Int {
        return when {
            element is ParadoxScriptPropertyKey -> {
                //处理对应的property是定义的特殊情况
                val parent = element.parent
                val isDefinition = parent?.castOrNull<ParadoxScriptProperty>()?.definitionInfo != null
                if (isDefinition) return getTextEndOffset(parent)
                element.endOffset
            }
            else -> {
                element.endOffset
            }
        }
    }
}
