@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.localisation

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
import com.intellij.psi.util.siblings
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.codeInsight.hints.localisation.ParadoxLocalisationReferenceHintsProvider.Settings
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.localisation.psi.ParadoxLocalisationCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationScriptedVariableReference
import javax.swing.JComponent

/**
 * 通过内嵌提示显示渲染后的本地化文本，适用于本地化参数中引用的本地化。
 *
 * 如果本地化文本过长则会先被截断。
 */
class ParadoxLocalisationReferenceHintsProvider : ParadoxLocalisationHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = PlsFacade.getInternalSettings().textLengthLimit,
        var iconHeightLimit: Int = PlsFacade.getInternalSettings().iconHeightLimit,
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxLocalisationReferenceHintsSettingsKey")

    override val name: String get() = PlsBundle.message("localisation.hints.localisationReference")
    override val description: String get() = PlsBundle.message("localisation.hints.localisationReference.description")
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
        if (element !is ParadoxLocalisationParameter) return true
        if (isIgnored(element)) return true
        val presentation = doCollect(element, editor, settings)
        val finalPresentation = presentation?.toFinalPresentation(this, file.project) ?: return true
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun isIgnored(element: ParadoxLocalisationParameter): Boolean {
        return element.firstChild.siblings().any { it is ParadoxLocalisationCommand || it is ParadoxLocalisationScriptedVariableReference }
    }

    private fun PresentationFactory.doCollect(element: ParadoxLocalisationParameter, editor: Editor, settings: Settings): InlayPresentation? {
        val renderer = ParadoxLocalisationTextInlayRenderer(editor, this).withLimit(settings.textLengthLimit, settings.iconHeightLimit)
        return renderer.render(element)
    }
}

