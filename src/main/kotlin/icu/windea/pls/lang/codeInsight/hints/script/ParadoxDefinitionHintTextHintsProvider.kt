package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProvider
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProviderBase
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 通过内嵌提示显示定义的提示文本。
 * 来自本地化名称（即最相关的本地化），或者对应的扩展规则。优先级从低到高。
 *
 * @see ParadoxHintTextProvider
 * @see ParadoxHintTextProviderBase.Definition
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionHintTextHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.script.definitionHintText")

    override val name: String get() = PlsBundle.message("script.hints.definitionHintText")
    override val description: String get() = PlsBundle.message("script.hints.definitionHintText.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override val renderLocalisation: Boolean get() = true
    override val renderIcon: Boolean get() = true

    override fun PresentationFactory.collectFromElement(element: PsiElement, file: PsiFile, editor: Editor, settings: ParadoxHintsSettings, sink: InlayHintsSink): Boolean {
        if (element is ParadoxScriptProperty) {
            val presentation = collect(element, editor, settings) ?: return true
            val finalPresentation = presentation.toFinalPresentation(this, file.project)
            val endOffset = element.propertyKey.endOffset
            sink.addInlineElement(endOffset, true, finalPresentation, false)
        }
        return true
    }

    private fun PresentationFactory.collect(element: ParadoxScriptDefinitionElement, editor: Editor, settings: ParadoxHintsSettings): InlayPresentation? {
        val primaryLocalisation = ParadoxDefinitionManager.getPrimaryLocalisation(element) ?: return null
        val renderer = ParadoxLocalisationTextInlayRenderer(editor, this, settings.textLengthLimit, settings.iconHeightLimit)
        return renderer.render(primaryLocalisation)
    }
}
