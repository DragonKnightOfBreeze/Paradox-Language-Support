@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.NoSettings
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isExpression

/**
 * 通过内嵌提示显示动态值信息，即类型。
 */
@Suppress("UnstableApiUsage")
class ParadoxDynamicValueInfoHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
    private val settingsKey = SettingsKey<NoSettings>("ParadoxDynamicValueInfoHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.dynamicValueInfo")
    override val description: String get() = PlsBundle.message("script.hints.dynamicValueInfo.description")
    override val key: SettingsKey<NoSettings> get() = settingsKey

    override fun createSettings() = NoSettings()

    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink): Boolean {
        //ignored for value_field or variable_field or other variants

        if (element !is ParadoxScriptStringExpressionElement) return true
        if (!element.isExpression()) return true
        val expression = element.name
        if (expression.isEmpty()) return true
        if (expression.isParameterized()) return true
        val resolveConstraint = ParadoxResolveConstraint.DynamicValueStrictly
        val resolved = element.references.reversed().filter { resolveConstraint.canResolve(it) }.firstNotNullOfOrNull { it.resolve() }
        if (resolved !is ParadoxDynamicValueElement) return true
        val presentation = doCollect(resolved) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.doCollect(element: ParadoxDynamicValueElement): InlayPresentation? {
        val name = element.name
        if (name.isEmpty()) return null
        if (name.isParameterized()) return null
        val type = element.dynamicValueType
        return smallText(": $type")
    }
}
