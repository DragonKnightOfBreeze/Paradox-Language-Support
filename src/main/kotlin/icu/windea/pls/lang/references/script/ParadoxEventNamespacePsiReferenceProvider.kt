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
import icu.windea.pls.lang.manipulation.ParadoxEventManipulationService
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.script.psi.ParadoxScriptString

/**
 * 解析（位于事件声明中的）事件ID中的事件命名空间引用。
 *
 * NOTE 2.1.10 仅支持作为字符串（[ParadoxScriptString]）的事件ID。
 */
class ParadoxEventNamespacePsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        ProgressManager.checkCanceled()

        if (element !is ParadoxScriptString) return PsiReference.EMPTY_ARRAY
        if (element.text.isParameterized()) return PsiReference.EMPTY_ARRAY // 不应当带有参数
        val rangeInElement = ParadoxEventManipulationService.getNamespaceRangeInFromEventId(element) ?: return PsiReference.EMPTY_ARRAY
        val event = ParadoxEventManipulationService.getEventDeclarationElementFromEventId(element) ?: return PsiReference.EMPTY_ARRAY
        val reference = ParadoxEventNamespacePsiReference(element, rangeInElement, event.createPointer())
        return arrayOf(reference)
    }
}
