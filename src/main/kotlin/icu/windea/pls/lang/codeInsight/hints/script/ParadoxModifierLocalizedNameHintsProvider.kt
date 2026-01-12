package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
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
import icu.windea.pls.model.constraints.ParadoxLocalisationIndexConstraint
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

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink) {
        if (element !is ParadoxScriptStringExpressionElement) return
        if (!element.isExpression()) return
        val config = ParadoxExpressionManager.getConfigs(element).firstOrNull() ?: return
        val type = config.configExpression.type
        if (type != CwtDataTypes.Modifier) return
        val name = element.value
        if (name.isEmpty()) return
        if (name.isParameterized()) return
        val configGroup = config.configGroup
        val project = configGroup.project
        val keys = ParadoxModifierManager.getModifierNameKeys(name, element)
        val localisation = keys.firstNotNullOfOrNull { key ->
            val selector = selector(project, element).localisation().contextSensitive()
                .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                .withConstraint(ParadoxLocalisationIndexConstraint.Modifier)
            ParadoxLocalisationSearch.searchNormal(key, selector).find()
        } ?: return

        val renderer = ParadoxLocalisationTextInlayRenderer(context)
        val presentation = renderer.render(localisation) ?: return
        sink.addInlinePresentation(element.endOffset) { add(presentation) }
    }
}
