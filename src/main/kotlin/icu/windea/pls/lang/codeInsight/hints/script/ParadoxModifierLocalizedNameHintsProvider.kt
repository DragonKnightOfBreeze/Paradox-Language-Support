package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withConstraint
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constraints.ParadoxIndexConstraint
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isExpression

/**
 * 通过内嵌提示显示修正的本地化名称。
 * 来自对应的本地化。
 */
@Suppress("UnstableApiUsage")
class ParadoxModifierLocalizedNameHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.script.modifierHintText")

    override val name: String get() = PlsBundle.message("script.hints.modifierHintText")
    override val description: String get() = PlsBundle.message("script.hints.modifierHintText.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override val renderLocalisation: Boolean get() = true
    override val renderIcon: Boolean get() = true

    override fun ParadoxHintsContext.collectFromElement(element: PsiElement, sink: InlayHintsSink): Boolean {
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
                ParadoxLocalisationSearch.searchNormal(key, selector).find()
            } ?: return true
            val presentation = collect(localisation)
            val finalPresentation = presentation?.toFinalPresentation() ?: return true
            val endOffset = element.endOffset
            sink.addInlineElement(endOffset, true, finalPresentation, false)
        }
        return true
    }

    private fun ParadoxHintsContext.collect(localisation: ParadoxLocalisationProperty): InlayPresentation? {
        val renderer = ParadoxLocalisationTextInlayRenderer(editor, factory, settings.textLengthLimit, settings.iconHeightLimit)
        return renderer.render(localisation)
    }
}
