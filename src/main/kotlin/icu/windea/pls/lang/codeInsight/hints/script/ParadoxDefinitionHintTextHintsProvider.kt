package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProvider
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProviderBase
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey

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

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink) {
        if (element !is ParadoxScriptPropertyKey) return
        val definition = element.parent as? ParadoxScriptProperty ?: return

        val primaryLocalisation = ParadoxDefinitionManager.getPrimaryLocalisation(definition) ?: return
        val renderer = ParadoxLocalisationTextInlayRenderer(context)
        val presentation = renderer.render(primaryLocalisation) ?: return
        sink.addInlinePresentation(element.endOffset, smallInset = true) { add(presentation) }
    }
}
