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
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.model.constraints.ParadoxResolveConstraint

/**
 * 通过内嵌提示显示复杂枚举值的提示文本。
 * 来自本地化名称（即同名的本地化），或者对应的扩展规则。优先级从低到高。
 *
 * @see ParadoxHintTextProvider
 * @see ParadoxHintTextProviderBase.ComplexEnumValue
 */
@Suppress("UnstableApiUsage")
class ParadoxComplexEnumValueHintTextHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.csv.complexEnumValueHintText")

    override val name: String get() = PlsBundle.message("csv.hints.complexEnumValueHintText")
    override val description: String get() = PlsBundle.message("csv.hints.complexEnumValueHintText.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override val renderLocalisation: Boolean get() = true
    override val renderIcon: Boolean get() = true

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink) {
        if (element !is ParadoxCsvColumn) return
        val expression = element.name
        if (expression.isEmpty()) return

        val resolveConstraint = ParadoxResolveConstraint.ComplexEnumValue
        if (!resolveConstraint.canResolveReference(element)) return
        val reference = element.reference ?: return
        if (!resolveConstraint.canResolve(reference)) return
        val resolved = reference.resolve() ?: return
        if (resolved !is ParadoxComplexEnumValueElement) return

        val hintLocalisation = PlsCodeInsightService.getHintLocalisation(resolved) ?: return
        val renderer = ParadoxLocalisationTextInlayRenderer(context)
        val presentation = renderer.render(hintLocalisation) ?: return
        sink.addInlinePresentation(element.endOffset) { add(presentation) }
    }

    context(context: ParadoxHintsContext)
    override fun collectForPreview(element: PsiElement, sink: InlayHintsSink) {
        if (element !is ParadoxCsvColumn) return
        ParadoxHintsPreviewUtil.fillData(element, sink)
    }
}
