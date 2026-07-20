package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProvider
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProviderBase
import icu.windea.pls.lang.codeInsight.ParadoxCodeInsightService
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsPreviewUtil
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.model.constraints.ParadoxReferenceConstraint
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isDataExpression

/**
 * 通过内嵌提示显示动态值的提示文本。
 * 来自展示名字（即同名的本地化文本），或者对应的扩展规则。优先级从低到高。
 *
 * @see ParadoxHintTextProvider
 * @see ParadoxHintTextProviderBase.DynamicValue
 */
@Suppress("UnstableApiUsage")
class ParadoxDynamicValueHintTextHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.script.dynamicValueHintText")

    override val name get() = ChronicleBundle.message("script.hints.dynamicValueHintText")
    override val description get() = ChronicleBundle.message("script.hints.dynamicValueHintText.description")
    override val key get() = settingsKey

    override val renderLocalisation get() = true
    override val renderIcon get() = true

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink) {
        if (element !is ParadoxScriptStringExpressionElement) return
        if (!element.isDataExpression()) return
        val expression = element.name
        if (expression.isEmpty()) return
        if (expression.isParameterized()) return
        val resolveConstraint = ParadoxReferenceConstraint.DynamicValueReference
        val resolved = element.references.reversed().filter { resolveConstraint.canResolve(it) }.firstNotNullOfOrNull { it.resolve() }
        if (resolved !is ParadoxDynamicValueLightElement) return

        val hintLocalisation = ParadoxCodeInsightService.getHintLocalisation(resolved) ?: return
        val renderer = ParadoxLocalisationTextInlayRenderer(context)
        val presentation = renderer.render(hintLocalisation) ?: return
        sink.addInlinePresentation(element.endOffset) { add(presentation) }
    }

    context(context: ParadoxHintsContext)
    override fun collectForPreview(element: PsiElement, sink: InlayHintsSink) {
        if (element !is ParadoxScriptStringExpressionElement) return
        ParadoxHintsPreviewUtil.fillData(element, sink)
    }
}
