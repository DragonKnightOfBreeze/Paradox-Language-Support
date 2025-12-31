package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import com.intellij.testFramework.LightVirtualFile
import icu.windea.pls.core.getCurrentStackTrace
import icu.windea.pls.lang.psi.ParadoxFile

@Suppress("UnstableApiUsage")
object ParadoxHintsPreviewUtil {
    fun detectPreview(file: ParadoxFile, index: Int): Boolean {
        // 首先判断是否是内存文件，然后再根据堆栈来判断
        val vFile = file.viewProvider.virtualFile
        if (vFile !is LightVirtualFile) return false
        val currentStackTrace = getCurrentStackTrace()
        val toDetect = currentStackTrace.getOrNull(index) ?: return false
        if (toDetect.className != "com.intellij.codeInsight.hints.settings.language.NewInlayProviderSettingsModel") return false
        if (toDetect.methodName != "collectData") return false
        return true
    }

    context(provider: ParadoxHintsProvider, context: ParadoxHintsContext)
    fun fillData(element: PsiElement, sink: InlayHintsSink) {
        val providerId = provider.key.id
        val offset = element.endOffset
        val text = ParadoxHintsPreviewBundle.get(providerId, offset) ?: return
        sink.addInlinePresentation(offset) { text(text) }
    }
}
