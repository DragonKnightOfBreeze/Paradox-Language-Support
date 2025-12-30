package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProvider
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProviderBase
import icu.windea.pls.lang.codeInsight.PlsCodeInsightService
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isExpression

/**
 * 通过内嵌提示显示动态值的提示文本。
 * 来自本地化名称（即同名的本地化），或者对应的扩展规则。优先级从低到高。
 *
 * @see ParadoxHintTextProvider
 * @see ParadoxHintTextProviderBase.DynamicValue
 */
@Suppress("UnstableApiUsage")
class ParadoxDynamicValueHintTextHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.script.dynamicValueHintText")

    override val name: String get() = PlsBundle.message("script.hints.dynamicValueHintText")
    override val description: String get() = PlsBundle.message("script.hints.dynamicValueHintText.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override val renderLocalisation: Boolean get() = true
    override val renderIcon: Boolean get() = true

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink) {
        // ignored for `value_field` or `variable_field` or other variants

        if (element !is ParadoxScriptStringExpressionElement) return
        if (!element.isExpression()) return
        val expression = element.name
        if (expression.isEmpty()) return
        if (expression.isParameterized()) return
        val resolveConstraint = ParadoxResolveConstraint.DynamicValueStrictly
        val resolved = element.references.reversed().filter { resolveConstraint.canResolve(it) }.firstNotNullOfOrNull { it.resolve() }
        if (resolved !is ParadoxDynamicValueElement) return

        val hintLocalisation = PlsCodeInsightService.getHintLocalisation(resolved) ?: return
        val renderer = ParadoxLocalisationTextInlayRenderer(context)
        val presentation = renderer.render(hintLocalisation) ?: return
        sink.addInlinePresentation(element.endOffset) { add(presentation) }
    }
}
