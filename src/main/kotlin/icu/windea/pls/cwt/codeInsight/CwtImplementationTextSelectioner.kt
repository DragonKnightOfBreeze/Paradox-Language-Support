package icu.windea.pls.cwt.codeInsight

import com.intellij.codeInsight.hint.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*

/**
 * 用于为CWT语言指定快速定义显示的文本范围。
 * * 属性、单独的值：从目标元素向前直到没有空行为止的最后一个注释开始，到目标元素结束
 * * 其他：从目标元素开始，到目标元素结束
 */
class CwtImplementationTextSelectioner: ImplementationTextSelectioner {
	override fun getTextStartOffset(element: PsiElement): Int {
		return when {
			element is CwtProperty -> {
				findTextStartOffsetIncludeComment(element) { it.parent is CwtRootBlock }
			}
			element is CwtPropertyKey -> {
				return getTextStartOffset(element.parent) //使用对应的property的
			}
			element is CwtString && element.isLonely() -> {
				findTextStartOffsetIncludeComment(element) { it.parent is CwtRootBlock }
			}
			else -> {
				element.textRange.startOffset
			}
		}
	}
	
	override fun getTextEndOffset(element: PsiElement): Int {
		return element.textRange.endOffset
	}
}