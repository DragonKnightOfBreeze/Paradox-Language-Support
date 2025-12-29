package icu.windea.pls.lang.codeInsight.hints.csv

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProvider
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProviderBase
import icu.windea.pls.lang.codeInsight.PlsCodeInsightService
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 通过内嵌提示显示定义引用的提示文本。
 * 来自本地化名称（即最相关的本地化），或者对应的扩展规则。优先级从低到高。
 *
 * @see ParadoxHintTextProvider
 * @see ParadoxHintTextProviderBase.Definition
 */
@Suppress("UnstableApiUsage")
class ParadoxCsvDefinitionReferenceHintTextHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.csv.definitionReferenceHintText")

    override val name: String get() = PlsBundle.message("csv.hints.definitionReferenceHintText")
    override val description: String get() = PlsBundle.message("csv.hints.definitionReferenceHintText.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override val renderLocalisation: Boolean get() = true
    override val renderIcon: Boolean get() = true

    override fun PresentationFactory.collectFromElement(element: PsiElement, file: PsiFile, editor: Editor, settings: ParadoxHintsSettings, sink: InlayHintsSink): Boolean {
        if (element !is ParadoxCsvColumn) return true
        if (!ParadoxResolveConstraint.Definition.canResolveReference(element)) return true
        val reference = element.reference ?: return true
        if (!ParadoxResolveConstraint.Definition.canResolve(reference)) return true
        val resolved = reference.resolve() ?: return true
        if (resolved !is ParadoxScriptDefinitionElement) return true
        val presentation = collect(resolved, editor, settings) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.collect(element: ParadoxScriptDefinitionElement, editor: Editor, settings: ParadoxHintsSettings): InlayPresentation? {
        val primaryLocalisation = PlsCodeInsightService.getHintLocalisation(element) ?: return null
        val renderer = ParadoxLocalisationTextInlayRenderer(editor, this, settings.textLengthLimit, settings.iconHeightLimit)
        return renderer.render(primaryLocalisation)
    }
}
