package icu.windea.pls.lang.codeInsight.hints.localisation

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.siblings
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsPreviewUtil
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationScriptedVariableReference

/**
 * 通过内嵌提示显示渲染后的本地化文本，适用于本地化参数中引用的本地化。默认不启用。
 *
 * 如果本地化文本过长则会先被截断。
 */
@Suppress("UnstableApiUsage")
class ParadoxLocalisationReferenceHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.localisation.localisationReference")

    override val name get() = PlsBundle.message("localisation.hints.localisationReference")
    override val description get() = PlsBundle.message("localisation.hints.localisationReference.description")
    override val key get() = settingsKey

    override val renderLocalisation get() = true
    override val renderIcon get() = true

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink) {
        if (element !is ParadoxLocalisationParameter) return
        val ignored = element.firstChild.siblings().any { it is ParadoxLocalisationCommand || it is ParadoxLocalisationScriptedVariableReference }
        if (ignored) return

        val renderer = ParadoxLocalisationTextInlayRenderer(context)
        val presentation = renderer.render(element) ?: return
        sink.addInlinePresentation(element.endOffset) { add(presentation) }
    }

    context(context: ParadoxHintsContext)
    override fun collectForPreview(element: PsiElement, sink: InlayHintsSink) {
        if (element !is ParadoxLocalisationParameter) return
        ParadoxHintsPreviewUtil.fillData(element, sink)
    }
}
