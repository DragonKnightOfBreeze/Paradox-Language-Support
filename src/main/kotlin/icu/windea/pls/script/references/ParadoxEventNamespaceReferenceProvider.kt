package icu.windea.pls.script.references

import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 解析事件ID中的事件命名空间引用。
 */
class ParadoxEventNamespaceReferenceProvider : PsiReferenceProvider() {
	override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        ProgressManager.checkCanceled()
        
        if(element !is ParadoxScriptString) return PsiReference.EMPTY_ARRAY
        if(element.text.isParameterized()) return PsiReference.EMPTY_ARRAY //不应当带有参数
        val rangeInElement = getRangeInElement(element) ?: return PsiReference.EMPTY_ARRAY
        val event = element.findParentByPath("id", definitionType = "event") //不处理内联的情况
        if(event !is ParadoxScriptProperty) return PsiReference.EMPTY_ARRAY
        val reference = ParadoxEventNamespacePsiReference(element, rangeInElement, event.createPointer())
        return arrayOf(reference)
    }
	
	private fun getRangeInElement(element: ParadoxScriptString): TextRange? {
		val text = element.text
		val dotIndex = text.indexOf('.')
		if(dotIndex == -1) return null
		val range = TextRange.create(if(text.isLeftQuoted()) 1 else 0, dotIndex)
		if(range.isEmpty) return null
		return range
	}
}
