@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.csv

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProvider
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProviderBase
import icu.windea.pls.lang.codeInsight.hints.csv.ParadoxCsvDefinitionReferenceHintTextHintsProvider.Settings
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import javax.swing.JComponent

/**
 * 通过内嵌提示显示定义引用的提示文本。
 * 来自本地化名称（即最相关的本地化），或者对应的扩展规则。优先级从低到高。
 *
 * @see ParadoxHintTextProvider
 * @see ParadoxHintTextProviderBase.Definition
 */
class ParadoxCsvDefinitionReferenceHintTextHintsProvider : ParadoxCsvHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = PlsFacade.getInternalSettings().textLengthLimitForInlay,
        var iconHeightLimit: Int = PlsFacade.getInternalSettings().iconHeightLimitForInlay,
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxCsvDefinitionReferenceHintTextHintsSettingsKey")

    override val name: String get() = PlsBundle.message("csv.hints.definitionReferenceHintText")
    override val description: String get() = PlsBundle.message("csv.hints.definitionReferenceHintText.description")
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
        val primaryLocalisation = ParadoxHintTextProvider.getHintLocalisation(element) ?: return null
        val renderer = ParadoxLocalisationTextInlayRenderer(editor, this).withLimit(settings.textLengthLimit, settings.iconHeightLimit)
        return renderer.render(primaryLocalisation)
    }
}
