@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.NoSettings
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.codeInsight.hints.presentation.SequencePresentation
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.complexEnums
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 通过内嵌提示显示复杂枚举值信息，即枚举名。
 */
class ParadoxComplexEnumValueInfoHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
    private val settingsKey = SettingsKey<NoSettings>("ParadoxComplexEnumValueInfoHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.complexEnumValueInfo")
    override val description: String get() = PlsBundle.message("script.hints.complexEnumValueInfo.description")
    override val key: SettingsKey<NoSettings> get() = settingsKey

    override fun createSettings() = NoSettings()

    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink): Boolean {
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
        val presentation = doCollect(resolved) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.doCollect(element: ParadoxComplexEnumValueElement): InlayPresentation? {
        val enumName = element.enumName
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        val config = configGroup.complexEnums[enumName] ?: return null
        val presentations = mutableListOf<InlayPresentation>()
        presentations.add(smallText(": "))
        presentations.add(psiSingleReference(smallText(enumName)) { config.pointer.element })
        return SequencePresentation(presentations)
    }
}
