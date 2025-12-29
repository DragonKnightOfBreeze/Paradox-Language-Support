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
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.codeInsight.hints.mergePresentations
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 通过内嵌提示显示复杂枚举值信息，即枚举名。
 */
@Deprecated("Use `ParadoxComplexEnumValueInfoHintsProviderNew` instead.")
@Suppress("UnstableApiUsage")
class ParadoxComplexEnumValueInfoHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.script.complexEnumValueInfo")

    override val name: String get() = PlsBundle.message("script.hints.complexEnumValueInfo")
    override val description: String get() = PlsBundle.message("script.hints.complexEnumValueInfo.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override fun PresentationFactory.collectFromElement(element: PsiElement, file: PsiFile, editor: Editor, settings: ParadoxHintsSettings, sink: InlayHintsSink): Boolean {
        if (element !is ParadoxScriptStringExpressionElement) return true
        val expression = element.name
        if (expression.isEmpty()) return true
        if (expression.isParameterized()) return true
        val resolveConstraint = ParadoxResolveConstraint.ComplexEnumValue
        if (!resolveConstraint.canResolveReference(element)) return true
        val reference = element.reference ?: return true
        if (!resolveConstraint.canResolve(reference)) return true
        val resolved = reference.resolve() ?: return true
        if (resolved !is ParadoxComplexEnumValueElement) return true
        val presentation = collect(resolved) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.collect(element: ParadoxComplexEnumValueElement): InlayPresentation? {
        val enumName = element.enumName
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        val config = configGroup.complexEnums[enumName] ?: return null
        val presentations = mutableListOf<InlayPresentation>()
        presentations.add(smallText(": "))
        presentations.add(psiSingleReference(smallText(config.name)) { config.pointer.element })
        return presentations.mergePresentations()
    }
}
