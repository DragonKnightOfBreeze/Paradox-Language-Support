@file:Suppress("UnstableApiUsage")

package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.script.codeInsight.hints.ParadoxDefinitionLocalizedNameHintsProvider.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 定义的本地化名字的内嵌提示。
 */
class ParadoxDefinitionLocalizedNameHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = PlsInternalSettings.textLengthLimit,
        var iconHeightLimit: Int = PlsInternalSettings.iconHeightLimit,
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxDefinitionLocalizedNameHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.definitionLocalizedName")
    override val description: String get() = PlsBundle.message("script.hints.definitionLocalizedName.description")
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
        if (element is ParadoxScriptProperty) {
            val presentation = doCollect(element, editor, settings) ?: return true
            val finalPresentation = presentation.toFinalPresentation(this, file.project)
            val endOffset = element.propertyKey.endOffset
            sink.addInlineElement(endOffset, true, finalPresentation, false)
        }
        return true
    }

    private fun PresentationFactory.doCollect(element: ParadoxScriptDefinitionElement, editor: Editor, settings: Settings): InlayPresentation? {
        val primaryLocalisation = ParadoxDefinitionManager.getPrimaryLocalisation(element) ?: return null
        return ParadoxLocalisationTextInlayRenderer(editor, this).withLimit(settings.textLengthLimit, settings.iconHeightLimit).render(primaryLocalisation)
    }
}
