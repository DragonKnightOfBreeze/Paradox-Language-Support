@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.hints.script.ParadoxComplexEnumValueLocalizedNameHintsProvider.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 复杂枚举值的本地化名字的内嵌提示（来自扩展的CWT规则）。
 */
class ParadoxComplexEnumValueLocalizedNameHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = PlsInternalSettings.textLengthLimit,
        var iconHeightLimit: Int = PlsInternalSettings.iconHeightLimit,
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxComplexEnumValueLocalizedNameHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.complexEnumValueLocalizedName")
    override val description: String get() = PlsBundle.message("script.hints.complexEnumValueLocalizedName.description")
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
        if (element !is ParadoxScriptStringExpressionElement) return true
        if (!element.isExpression()) return true
        val name = element.name
        if (name.isEmpty()) return true
        if (name.isParameterized()) return true

        val info = ParadoxComplexEnumValueManager.getInfo(element)
        if (info != null) {
            val presentation = doCollect(info.name, info.enumName, file, editor, settings) ?: return true
            val finalPresentation = presentation.toFinalPresentation(this, file.project)
            val endOffset = element.endOffset
            sink.addInlineElement(endOffset, true, finalPresentation, false)
            return true
        }

        val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return true
        val type = config.configExpression.type
        if (type != CwtDataTypes.EnumValue) return true
        val enumName = config.configExpression.value ?: return true
        if (enumName in config.configGroup.enums) return true //only for complex enums
        val presentation = doCollect(name, enumName, file, editor, settings) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)

        return true
    }

    private fun PresentationFactory.doCollect(name: String, enumName: String, file: PsiFile, editor: Editor, settings: Settings): InlayPresentation? {
        val hintElement = getNameLocalisationToUse(name, enumName, file) ?: return null
        return ParadoxLocalisationTextInlayRenderer(editor, this).withLimit(settings.textLengthLimit, settings.iconHeightLimit).render(hintElement)
    }

    private fun getNameLocalisationToUse(name: String, enumName: String, file: PsiFile): ParadoxLocalisationProperty? {
        ParadoxComplexEnumValueManager.getNameLocalisationFromExtendedConfig(name, enumName, file)?.let { return it }
        ParadoxComplexEnumValueManager.getNameLocalisation(name, file, ParadoxLocaleManager.getPreferredLocaleConfig())?.let { return it }
        return null
    }
}
