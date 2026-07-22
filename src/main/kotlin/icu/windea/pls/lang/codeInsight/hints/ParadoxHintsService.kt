package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import com.intellij.testFramework.LightVirtualFile
import icu.windea.pls.base.annotations.ChronicleAnnotationService
import icu.windea.pls.core.collections.forEachReversedFast
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.recursion.RecursionService
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProvider
import icu.windea.pls.lang.psi.ParadoxFile
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

@Suppress("UnstableApiUsage")
object ParadoxHintsService {
    /**
     * @see ParadoxHintTextProvider.getHintText
     */
    @Suppress("unused")
    fun getHintText(element: PsiElement): String? {
        val gameType = selectGameType(element)
        ParadoxHintTextProvider.EP_NAME.extensionList.forEachReversedFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ep.getHintText(element)?.orNull()?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxHintTextProvider.getHintLocalisation
     */
    fun getHintLocalisation(element: PsiElement): ParadoxLocalisationProperty? {
        val gameType = selectGameType(element)
        ParadoxHintTextProvider.EP_NAME.extensionList.forEachReversedFast f@{ ep ->
            if (!ChronicleAnnotationService.check(ep, gameType)) return@f
            ep.getHintLocalisation(element)?.let { return it }
        }
        return null
    }

    fun detectPreview(file: ParadoxFile, index: Int): Boolean {
        // 首先判断是否是内存文件，然后再根据堆栈来判断
        val vFile = file.viewProvider.virtualFile
        if (vFile !is LightVirtualFile) return false
        val currentStackTrace = RecursionService.getCurrentStackTrace()
        val toDetect = currentStackTrace.getOrNull(index) ?: return false
        if (toDetect.className != "com.intellij.codeInsight.hints.settings.language.NewInlayProviderSettingsModel") return false
        if (toDetect.methodName != "collectData") return false
        return true
    }

    context(provider: ParadoxHintsProvider, context: ParadoxHintsContext)
    fun fillPreviewData(element: PsiElement, sink: InlayHintsSink) {
        val providerId = provider.key.id
        val offset = element.endOffset
        val text = ParadoxHintsPreviewBundle.get(providerId, offset) ?: return
        sink.addInlinePresentation(offset) { text(text) }
    }
}
