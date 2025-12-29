package icu.windea.pls.lang.codeInsight.hints.csv

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.codeInsight.hints.mergePresentations
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.model.constraints.ParadoxResolveConstraint

/**
 * 通过内嵌提示显示复杂枚举值信息，即枚举名。
 */
@Deprecated("Use `ParadoxCsvComplexEnumValueInfoHintsProviderNew` instead.")
@Suppress("UnstableApiUsage")
class ParadoxCsvComplexEnumValueInfoHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.csv.complexEnumValueInfo")

    override val name: String get() = PlsBundle.message("csv.hints.complexEnumValueInfo")
    override val description: String get() = PlsBundle.message("csv.hints.complexEnumValueInfo.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override fun ParadoxHintsContext.collectFromElement(element: PsiElement, sink: InlayHintsSink): Boolean {
        if (element !is ParadoxCsvColumn) return true
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

    private fun ParadoxHintsContext.collect(element: ParadoxComplexEnumValueElement): InlayPresentation? {
        val enumName = element.name
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        val config = configGroup.complexEnums[enumName] ?: return null
        val presentations = mutableListOf<InlayPresentation>()
        presentations.add(factory.smallText(": "))
        presentations.add(factory.psiSingleReference(factory.smallText(config.name)) { config.pointer.element })
        return presentations.mergePresentations()
    }
}
