package icu.windea.pls.lang.references.script

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.findParentByPath

/**
 * 解析事件ID中的事件命名空间引用。
 */
class ParadoxEventNamespacePsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        ProgressManager.checkCanceled()

        if (element !is ParadoxScriptString) return PsiReference.EMPTY_ARRAY
        if (element.text.isParameterized()) return PsiReference.EMPTY_ARRAY //不应当带有参数
        val rangeInElement = getRangeInElement(element) ?: return PsiReference.EMPTY_ARRAY
        val event = element.findParentByPath("id", definitionType = ParadoxDefinitionTypes.Event) //不处理内联的情况
        if (event !is ParadoxScriptProperty) return PsiReference.EMPTY_ARRAY
        val reference = ParadoxEventNamespacePsiReference(element, rangeInElement, event.createPointer())
        return arrayOf(reference)
    }

    private fun getRangeInElement(element: ParadoxScriptString): TextRange? {
        val text = element.text
        val dotIndex = text.indexOf('.')
        if (dotIndex == -1) return null
        val range = TextRange.create(if (text.isLeftQuoted()) 1 else 0, dotIndex)
        if (range.isEmpty) return null
        return range
    }
}
