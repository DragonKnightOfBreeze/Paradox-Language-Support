@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.ep.codeInsight.hints.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.hints.script.ParadoxScriptedVariableHintTextHintsProvider.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 封装变量的提示文本的内嵌提示。
 *
 * 来自同名的本地化，或者对应的扩展规则。优先级从低到高。
 *
 * @see ParadoxHintTextProvider
 * @see ParadoxHintTextProviderBase.ScriptedVariable
 */
class ParadoxScriptedVariableHintTextHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = PlsFacade.getInternalSettings().textLengthLimit,
        var iconHeightLimit: Int = PlsFacade.getInternalSettings().iconHeightLimit,
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxScriptedVariableLocalizedNameHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.scriptedVariableHintText")
    override val description: String get() = PlsBundle.message("script.hints.scriptedVariableHintText.description")
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

        val presentation = doCollect(element, editor, settings) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.scriptedVariableName.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.doCollect(element: ParadoxScriptScriptedVariable, editor: Editor, settings: Settings): InlayPresentation? {
        val hintLocalisation = ParadoxHintTextProvider.getHintLocalisation(element) ?: return null
        val renderer = ParadoxLocalisationTextInlayRenderer(editor, this).withLimit(settings.textLengthLimit, settings.iconHeightLimit)
        return renderer.render(hintLocalisation)
    }
}
