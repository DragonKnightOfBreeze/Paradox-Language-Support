@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.csv

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.codeInsight.hints.csv.ParadoxCsvComplexEnumValueLocalizedNameHintsProvider.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constraints.*
import javax.swing.*

/**
 * 复杂枚举值的本地化名字的内嵌提示（来自扩展的CWT规则）。
 */
class ParadoxCsvComplexEnumValueLocalizedNameHintsProvider : ParadoxCsvHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = PlsFacade.getInternalSettings().textLengthLimit,
        var iconHeightLimit: Int = PlsFacade.getInternalSettings().iconHeightLimit,
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxCsvComplexEnumValueLocalizedNameHintsSettingsKey")

    override val name: String get() = PlsBundle.message("csv.hints.complexEnumValueLocalizedName")
    override val description: String get() = PlsBundle.message("csv.hints.complexEnumValueLocalizedName.description")
    override val key: SettingsKey<Settings> get() = settingsKey

    override val renderLocalisation: Boolean get() = true
    override val renderIcon: Boolean get() = true

    override fun createSettings() = Settings()

    override fun createConfigurable(settings: Settings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent = panel {
                createTextLengthLimitRow(settings::textLengthLimit)
                createIconHeightLimitRow(settings::iconHeightLimit)
            }
        }
    }

    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
        if (element !is ParadoxCsvColumn) return true
        if (element.isHeaderColumn()) return true
        if (!ParadoxResolveConstraint.ComplexEnumValue.canResolveReference(element)) return true
        val reference = element.reference ?: return true
        if (!ParadoxResolveConstraint.ComplexEnumValue.canResolve(reference)) return true
        val resolved = reference.resolve() ?: return true
        if (resolved !is ParadoxComplexEnumValueElement) return true
        val presentation = doCollect(resolved, file, editor, settings) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.doCollect(element: ParadoxComplexEnumValueElement, file: PsiFile, editor: Editor, settings: Settings): InlayPresentation? {
        val name = element.name
        val enumName = element.enumName
        val hintElement = getNameLocalisationToUse(name, enumName, file) ?: return null
        return ParadoxLocalisationTextInlayRenderer(editor, this).withLimit(settings.textLengthLimit, settings.iconHeightLimit).render(hintElement)
    }

    private fun getNameLocalisationToUse(name: String, enumName: String, file: PsiFile): ParadoxLocalisationProperty? {
        ParadoxComplexEnumValueManager.getNameLocalisationFromExtendedConfig(name, enumName, file)?.let { return it }
        ParadoxComplexEnumValueManager.getNameLocalisation(name, file, ParadoxLocaleManager.getPreferredLocaleConfig())?.let { return it }
        return null
    }
}
