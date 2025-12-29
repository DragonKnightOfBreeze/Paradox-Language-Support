package icu.windea.pls.lang.codeInsight.hints.localisation

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.siblings
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
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
class ParadoxLocalisationReferenceHintsProvider: ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.localisation.localisationReference")

    override val name: String get() = PlsBundle.message("localisation.hints.localisationReference")
    override val description: String get() = PlsBundle.message("localisation.hints.localisationReference.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override val renderLocalisation: Boolean get() = true
    override val renderIcon: Boolean get() = true

    override fun PresentationFactory.collectFromElement(element: PsiElement, file: PsiFile, editor: Editor, settings: ParadoxHintsSettings, sink: InlayHintsSink): Boolean {
        if (element !is ParadoxLocalisationParameter) return true
        if (isIgnored(element)) return true
        val presentation = collect(element, editor, settings)
        val finalPresentation = presentation?.toFinalPresentation(this, file.project) ?: return true
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun isIgnored(element: ParadoxLocalisationParameter): Boolean {
        return element.firstChild.siblings().any { it is ParadoxLocalisationCommand || it is ParadoxLocalisationScriptedVariableReference }
    }

    private fun PresentationFactory.collect(element: ParadoxLocalisationParameter, editor: Editor, settings: ParadoxHintsSettings): InlayPresentation? {
        val renderer = ParadoxLocalisationTextInlayRenderer(editor, this, settings.textLengthLimit, settings.iconHeightLimit)
        return renderer.render(element)
    }
}
