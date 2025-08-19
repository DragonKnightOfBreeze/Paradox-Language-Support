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
import icu.windea.pls.lang.codeInsight.hints.csv.ParadoxCsvDefinitionReferenceLocalizedNameHintsProvider.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * CSV文件中的定义引用的本地化名字的内嵌提示。
 */
class ParadoxCsvDefinitionReferenceLocalizedNameHintsProvider : ParadoxCsvHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = PlsFacade.getInternalSettings().textLengthLimit,
        var iconHeightLimit: Int = PlsFacade.getInternalSettings().iconHeightLimit,
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxCsvDefinitionReferenceLocalizedNameHintsSettingsKey")

    override val name: String get() = PlsBundle.message("csv.hints.definitionReferenceLocalizedName")
    override val description: String get() = PlsBundle.message("csv.hints.definitionReferenceLocalizedName.description")
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
        if (!ParadoxResolveConstraint.Definition.canResolveReference(element)) return true
        val reference = element.reference ?: return true
        if (!ParadoxResolveConstraint.Definition.canResolve(reference)) return true
        val resolved = reference.resolve() ?: return true
        if (resolved !is ParadoxScriptDefinitionElement) return true
        val presentation = doCollect(resolved, editor, settings) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.doCollect(element: ParadoxScriptDefinitionElement, editor: Editor, settings: Settings): InlayPresentation? {
        val primaryLocalisation = ParadoxDefinitionManager.getPrimaryLocalisation(element) ?: return null
        return ParadoxLocalisationTextInlayRenderer(editor, this).withLimit(settings.textLengthLimit, settings.iconHeightLimit).render(primaryLocalisation)
    }
}
