@file:Suppress("UnstableApiUsage")

package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*

/**
 * 动态值信息的内嵌提示（类型）。
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
        val resolveConstraint = ParadoxResolveConstraint.DynamicValueStrictly
        val resolved = element.references.filter { resolveConstraint.canResolve(it) }.mapNotNull { it.resolve() }.lastOrNull()
            ?.castOrNull<ParadoxDynamicValueElement>()
            ?: return true
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

