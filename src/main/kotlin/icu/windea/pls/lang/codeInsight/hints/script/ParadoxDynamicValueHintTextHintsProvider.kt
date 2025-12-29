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
import icu.windea.pls.lang.codeInsight.PlsCodeInsightService
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
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

    override fun PresentationFactory.collectFromElement(element: PsiElement, file: PsiFile, editor: Editor, settings: ParadoxHintsSettings, sink: InlayHintsSink): Boolean {
        // ignored for `value_field` or `variable_field` or other variants

        if (element !is ParadoxScriptStringExpressionElement) return true
        if (!element.isExpression()) return true
        val expression = element.name
        if (expression.isEmpty()) return true
        if (expression.isParameterized()) return true
        val resolveConstraint = ParadoxResolveConstraint.DynamicValueStrictly
        val resolved = element.references.reversed().filter { resolveConstraint.canResolve(it) }.firstNotNullOfOrNull { it.resolve() }
        if (resolved !is ParadoxDynamicValueElement) return true
        val presentation = collect(resolved, editor, settings) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.collect(element: ParadoxDynamicValueElement, editor: Editor, settings: ParadoxHintsSettings): InlayPresentation? {
        val name = element.name
        if (name.isEmpty()) return null
        if (name.isParameterized()) return null
        val hintLocalisation = PlsCodeInsightService.getHintLocalisation(element) ?: return null
        val renderer = ParadoxLocalisationTextInlayRenderer(editor, this, settings.textLengthLimit, settings.iconHeightLimit)
        return renderer.render(hintLocalisation)
    }
}
