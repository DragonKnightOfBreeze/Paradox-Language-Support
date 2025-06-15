@file:Suppress("UnstableApiUsage")

package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderer.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constants.PlsSettingConstants
import icu.windea.pls.script.codeInsight.hints.ParadoxScriptedVariableLocalizedNameHintsProvider.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 封装变量的本地化名字的内嵌提示。
 */
class ParadoxScriptedVariableLocalizedNameHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = PlsSettingConstants.textLengthLimit,
        var iconHeightLimit: Int = PlsSettingConstants.iconHeightLimit,
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxScriptedVariableLocalizedNameHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.scriptedVariableLocalizedName")
    override val description: String get() = PlsBundle.message("script.hints.scriptedVariableLocalizedName.description")
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
        //only for scripted variables, not for scripted variable references

        if (element !is ParadoxScriptScriptedVariable) return true
        val name = element.name
        if (name.isNullOrEmpty()) return true
        if (name.isParameterized()) return true

        val presentation = doCollect(name, file, editor, settings) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.scriptedVariableName.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.doCollect(name: String, file: PsiFile, editor: Editor, settings: Settings): InlayPresentation? {
        val hintElement = getNameLocalisationToUse(name, file) ?: return null
        return ParadoxLocalisationTextInlayRenderer.render(hintElement, this, editor, settings.textLengthLimit, settings.iconHeightLimit)
    }

    private fun getNameLocalisationToUse(name: String, file: PsiFile): ParadoxLocalisationProperty? {
        ParadoxScriptedVariableManager.getNameLocalisationFromExtendedConfig(name, file)?.let { return it }
        ParadoxScriptedVariableManager.getNameLocalisation(name, file, ParadoxLocaleManager.getPreferredLocaleConfig())?.let { return it }
        return null
    }
}
