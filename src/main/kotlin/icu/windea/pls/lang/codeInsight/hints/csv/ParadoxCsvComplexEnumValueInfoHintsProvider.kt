@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.csv

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.model.constraints.*

/**
 * 通过内嵌提示显示复杂枚举值信息，即枚举名。
 */
class ParadoxCsvComplexEnumValueInfoHintsProvider : ParadoxCsvHintsProvider<NoSettings>() {
    private val settingsKey = SettingsKey<NoSettings>("ParadoxCsvComplexEnumValueInfoHintsSettingsKey")

    override val name: String get() = PlsBundle.message("csv.hints.complexEnumValueInfo")
    override val description: String get() = PlsBundle.message("csv.hints.complexEnumValueInfo.description")
    override val key: SettingsKey<NoSettings> get() = settingsKey

    override fun createSettings() = NoSettings()

    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink): Boolean {
        if (element !is ParadoxCsvColumn) return true
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
        val enumName = element.name
        val configGroup = PlsFacade.getConfigGroup(element.project, element.gameType)
        val config = configGroup.complexEnums[enumName] ?: return null
        val presentations = mutableListOf<InlayPresentation>()
        presentations.add(smallText(": "))
        presentations.add(psiSingleReference(smallText(config.name)) { config.pointer.element })
        return SequencePresentation(presentations)
    }
}
