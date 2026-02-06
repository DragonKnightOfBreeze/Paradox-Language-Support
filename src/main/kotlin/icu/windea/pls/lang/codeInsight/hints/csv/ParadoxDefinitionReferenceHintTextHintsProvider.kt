package icu.windea.pls.lang.codeInsight.hints.csv

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProvider
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProviderBase
import icu.windea.pls.lang.codeInsight.PlsCodeInsightService
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsPreviewUtil
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxDefinitionElement

/**
 * 通过内嵌提示显示定义引用的提示文本。
 * 来自本地化名称（即最相关的本地化），或者对应的扩展规则。优先级从低到高。
 *
 * @see ParadoxHintTextProvider
 * @see ParadoxHintTextProviderBase.Definition
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionReferenceHintTextHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.csv.definitionReferenceHintText")

    override val name get() = PlsBundle.message("csv.hints.definitionReferenceHintText")
    override val description get() = PlsBundle.message("csv.hints.definitionReferenceHintText.description")
    override val key get() = settingsKey

    override val renderLocalisation get() = true
    override val renderIcon get() = true

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink) {
        val resolveConstraint = ParadoxResolveConstraint.Definition
        if (!resolveConstraint.canResolveReference(element)) return
        val reference = element.reference ?: return
        if (!resolveConstraint.canResolve(reference)) return
        val resolved = reference.resolve() ?: return
        if (resolved !is ParadoxDefinitionElement) return

        val primaryLocalisation = PlsCodeInsightService.getHintLocalisation(resolved) ?: return
        val renderer = ParadoxLocalisationTextInlayRenderer(context)
        val presentation = renderer.render(primaryLocalisation) ?: return
        sink.addInlinePresentation(element.endOffset) { add(presentation) }
    }

    context(context: ParadoxHintsContext)
    override fun collectForPreview(element: PsiElement, sink: InlayHintsSink) {
        if (element !is ParadoxCsvColumn) return
        ParadoxHintsPreviewUtil.fillData(element, sink)
    }
}
