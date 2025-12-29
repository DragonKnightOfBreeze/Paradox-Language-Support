package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProvider
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProviderBase
import icu.windea.pls.lang.codeInsight.PlsCodeInsightService
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 通过内嵌提示显示复杂枚举值的提示文本。
 * 来自本地化名称（即同名的本地化），或者对应的扩展规则。优先级从低到高。
 *
 * @see ParadoxHintTextProvider
 * @see ParadoxHintTextProviderBase.ComplexEnumValue
 */
@Suppress("UnstableApiUsage")
class ParadoxComplexEnumValueHintTextHintsProvider: ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.complexEnumValueHintText")

    override val name: String get() = PlsBundle.message("script.hints.complexEnumValueHintText")
    override val description: String get() = PlsBundle.message("script.hints.complexEnumValueHintText.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override val renderLocalisation: Boolean get() = true
    override val renderIcon: Boolean get() = true

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink): Boolean {
        if (element !is ParadoxScriptStringExpressionElement) return true
        val name = element.name
        if (name.isEmpty()) return true
        if (name.isParameterized()) return true
        val resolveConstraint = ParadoxResolveConstraint.ComplexEnumValue
        if (!resolveConstraint.canResolveReference(element)) return true
        val reference = element.reference ?: return true
        if (!resolveConstraint.canResolve(reference)) return true
        val resolved = reference.resolve() ?: return true
        if (resolved !is ParadoxComplexEnumValueElement) return true
        val presentation = collect(resolved) ?: return true
        val finalPresentation = presentation.toFinalPresentation()
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    context(context: ParadoxHintsContext)
    private fun collect(element: ParadoxComplexEnumValueElement): InlayPresentation? {
        val hintLocalisation = PlsCodeInsightService.getHintLocalisation(element) ?: return null
        val renderer = ParadoxLocalisationTextInlayRenderer(context)
        return renderer.render(hintLocalisation)
    }
}
