@file:Suppress("UnstableApiUsage")

package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderer.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constants.PlsSettingConstants
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.codeInsight.hints.ParadoxDynamicValueLocalizedNameHintsProvider.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 动态值的本地化名字的内嵌提示。
 */
class ParadoxDynamicValueLocalizedNameHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = PlsSettingConstants.textLengthLimit,
        var iconHeightLimit: Int = PlsSettingConstants.iconHeightLimit,
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxDynamicValueLocalizedNameHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.dynamicValueLocalizedName")
    override val description: String get() = PlsBundle.message("script.hints.dynamicValueLocalizedName.description")
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
        //ignored for value_field or variable_field or other variants

        if (element !is ParadoxScriptStringExpressionElement) return true
        if (!element.isExpression()) return true
        val resolveConstraint = ParadoxResolveConstraint.DynamicValueStrictly
        val resolved = element.references.filter { resolveConstraint.canResolve(it) }.mapNotNull { it.resolve() }.lastOrNull()
            ?.castOrNull<ParadoxDynamicValueElement>()
            ?: return true
        val name = resolved.name
        if (name.isEmpty()) return true
        if (name.isParameterized()) return true

        val presentation = doCollect(resolved.name, resolved.dynamicValueTypes, file, editor, settings) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.doCollect(name: String, types: Set<String>, file: PsiFile, editor: Editor, settings: Settings): InlayPresentation? {
        val hintElement = getNameLocalisationToUse(name, types, file) ?: return null
        return ParadoxLocalisationTextInlayRenderer.render(hintElement, this, editor, settings.textLengthLimit, settings.iconHeightLimit)
    }

    private fun getNameLocalisationToUse(name: String, types: Set<String>, file: PsiFile): ParadoxLocalisationProperty? {
        ParadoxDynamicValueManager.getNameLocalisationFromExtendedConfig(name, types, file)?.let { return it }
        ParadoxDynamicValueManager.getNameLocalisation(name, file, ParadoxLocaleManager.getPreferredLocaleConfig())?.let { return it }
        return null
    }
}
