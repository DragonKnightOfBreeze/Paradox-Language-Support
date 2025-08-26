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
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.hints.script.ParadoxModifierLocalizedNameHintsProvider.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 通过内嵌提示显示修正的本地化后的名字。
 * 来自对应的本地化。
 */
class ParadoxModifierLocalizedNameHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = PlsFacade.getInternalSettings().textLengthLimit,
        var iconHeightLimit: Int = PlsFacade.getInternalSettings().iconHeightLimit,
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxModifierLocalizedNameHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.modifierHintText")
    override val description: String get() = PlsBundle.message("script.hints.modifierHintText.description")
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
        val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return true
        val type = config.configExpression.type
        if (type == CwtDataTypes.Modifier) {
            val name = element.value
            if (name.isEmpty()) return true
            if (name.isParameterized()) return true
            val configGroup = config.configGroup
            val project = configGroup.project
            val keys = ParadoxModifierManager.getModifierNameKeys(name, element)
            val localisation = keys.firstNotNullOfOrNull { key ->
                val selector = selector(project, element).localisation().contextSensitive()
                    .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                    .withConstraint(ParadoxIndexConstraint.Localisation.Modifier)
                ParadoxLocalisationSearch.search(key, selector).find()
            } ?: return true
            val presentation = doCollect(localisation, editor, settings)
            val finalPresentation = presentation?.toFinalPresentation(this, file.project) ?: return true
            val endOffset = element.endOffset
            sink.addInlineElement(endOffset, true, finalPresentation, false)
        }
        return true
    }

    private fun PresentationFactory.doCollect(localisation: ParadoxLocalisationProperty, editor: Editor, settings: Settings): InlayPresentation? {
        return ParadoxLocalisationTextInlayRenderer(editor, this).withLimit(settings.textLengthLimit, settings.iconHeightLimit).render(localisation)
    }
}

