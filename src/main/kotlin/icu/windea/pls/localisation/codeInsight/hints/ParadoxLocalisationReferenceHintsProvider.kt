@file:Suppress("UnstableApiUsage")

package icu.windea.pls.localisation.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.renderer.*
import icu.windea.pls.localisation.codeInsight.hints.ParadoxLocalisationReferenceHintsProvider.*
import icu.windea.pls.localisation.psi.*
import javax.swing.*

/**
 * 本地化引用的内嵌提示（对应的本地化的渲染后文本，如果过长则会截断）。
 */
class ParadoxLocalisationReferenceHintsProvider : ParadoxLocalisationHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = PlsInternalSettings.textLengthLimit,
        var iconHeightLimit: Int = PlsInternalSettings.iconHeightLimit,
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
        return ParadoxLocalisationTextInlayRenderer(editor, this).withLimit(settings.textLengthLimit, settings.iconHeightLimit).render(element)
    }
}

