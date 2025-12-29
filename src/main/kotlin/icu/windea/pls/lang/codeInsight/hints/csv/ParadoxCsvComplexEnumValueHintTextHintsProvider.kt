package icu.windea.pls.lang.codeInsight.hints.csv

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.InlayHintsUtils
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.MenuOnClickPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.platform.ide.progress.ModalTaskOwner.project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProvider
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProviderBase
import icu.windea.pls.lang.codeInsight.PlsCodeInsightService
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.codeInsight.hints.csv.ParadoxCsvComplexEnumValueHintTextHintsProvider.*
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.settings.PlsInternalSettings
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import javax.swing.JComponent

/**
 * 通过内嵌提示显示复杂枚举值的提示文本。
 * 来自本地化名称（即同名的本地化），或者对应的扩展规则。优先级从低到高。
 *
 * @see ParadoxHintTextProvider
 * @see ParadoxHintTextProviderBase.ComplexEnumValue
 */
@Suppress("UnstableApiUsage")
class ParadoxCsvComplexEnumValueHintTextHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("ParadoxCsvComplexEnumValueHintTextHintsSettingsKey")

    override val name: String get() = PlsBundle.message("csv.hints.complexEnumValueHintText")
    override val description: String get() = PlsBundle.message("csv.hints.complexEnumValueHintText.description")
    override val key: SettingsKey<Settings> get() = settingsKey

    override val renderLocalisation: Boolean get() = true
    override val renderIcon: Boolean get() = true

    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
        if (element !is ParadoxCsvColumn) return true
        val resolveConstraint = ParadoxResolveConstraint.ComplexEnumValue
        if (!resolveConstraint.canResolveReference(element)) return true
        val reference = element.reference ?: return true
        if (!resolveConstraint.canResolve(reference)) return true
        val resolved = reference.resolve() ?: return true
        if (resolved !is ParadoxComplexEnumValueElement) return true
        val presentation = doCollect(resolved, editor, settings) ?: return true
        var presentation1: InlayPresentation = if (false) {
            roundWithBackgroundAndSmallInset(presentation)
        } else {
            roundWithBackground(presentation)
        }
        if (project != null) {
            presentation1 = MenuOnClickPresentation(presentation1, project) {
                InlayHintsUtils.getDefaultInlayHintsProviderPopupActions(file.project.key) { file.project.name }
            }
        }
        val finalPresentation = presentation1
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.doCollect(element: ParadoxComplexEnumValueElement, editor: Editor, settings: Settings): InlayPresentation? {
        val hintLocalisation = PlsCodeInsightService.getHintLocalisation(element) ?: return null
        val renderer = ParadoxLocalisationTextInlayRenderer(editor, this, settings.textLengthLimit, settings.iconHeightLimit)
        return renderer.render(hintLocalisation)
    }
}
