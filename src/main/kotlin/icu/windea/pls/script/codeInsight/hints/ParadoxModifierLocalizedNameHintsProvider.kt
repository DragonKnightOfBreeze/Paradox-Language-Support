@file:Suppress("UnstableApiUsage")

package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderer.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.codeInsight.hints.ParadoxModifierLocalizedNameHintsProvider.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 修正的本地化名字的内嵌提示
 */
class ParadoxModifierLocalizedNameHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = 30,
        var iconHeightLimit: Int = 32
    )
    
    private val settingsKey = SettingsKey<Settings>("ParadoxModifierLocalizedNameHintsSettingsKey")
    
    override val name: String get() = PlsBundle.message("script.hints.modifierLocalizedName")
    override val description: String get() = PlsBundle.message("script.hints.modifierLocalizedName.description")
    override val key: SettingsKey<Settings> get() = settingsKey
    
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
        if(element !is ParadoxScriptStringExpressionElement) return true
        if(!element.isExpression()) return true
        val config = CwtConfigHandler.getConfigs(element).firstOrNull() ?: return true
        val type = config.expression.type
        if(type == CwtDataTypes.Modifier) {
            val name = element.value
            if(name.isEmpty()) return true
            if(name.isParameterized()) return true
            val configGroup = config.configGroup
            val project = configGroup.project
            val keys = ParadoxModifierHandler.getModifierNameKeys(name, element)
            val localisation = keys.firstNotNullOfOrNull { key ->
                val selector = localisationSelector(project, element).contextSensitive()
                    .preferLocale(ParadoxLocaleHandler.getPreferredLocale())
                    .withConstraint(ParadoxLocalisationConstraint.Modifier)
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
        return ParadoxLocalisationTextInlayRenderer.render(localisation, this, editor, settings.textLengthLimit, settings.iconHeightLimit)
    }
}

