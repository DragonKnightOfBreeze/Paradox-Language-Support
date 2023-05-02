package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.codeInsight.hints.ParadoxLocalisationReferenceInfoHintsProvider.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.localisation.*
import java.util.*
import javax.swing.*

/**
 * 本地化引用信息的内嵌提示（对应的本地化的渲染后文本，如果过长则会截断）。
 */
@Suppress("UnstableApiUsage")
class ParadoxLocalisationReferenceInfoHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    companion object {
        private val settingsKey: SettingsKey<Settings> = SettingsKey("ParadoxLocalisationReferenceInfoHintsSettingsKey")
        private val expressionTypes: EnumSet<CwtDataType> = enumSetOf(
            CwtDataType.Localisation,
            CwtDataType.InlineLocalisation,
            CwtDataType.SyncedLocalisation,
            CwtDataType.AliasName, //需要兼容alias
            CwtDataType.AliasKeysField, //需要兼容alias
            CwtDataType.AliasMatchLeft, //需要兼容alias
            CwtDataType.SingleAliasRight, //需要兼容single_alias
        )
    }
    
    data class Settings(
        var textLengthLimit: Int = 30,
        var iconHeightLimit: Int = 32
    )
    
    override val name: String get() = PlsBundle.message("script.hints.localisationReferenceInfo")
    override val description: String get() = PlsBundle.message("script.hints.localisationReferenceInfo.description")
    override val key: SettingsKey<Settings> get() = settingsKey
    
    override fun createSettings() = Settings()
    
    override fun createConfigurable(settings: Settings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent = panel {
                row {
                    label(PlsBundle.message("script.hints.settings.textLengthLimit")).widthGroup("left")
                        .applyToComponent { toolTipText = PlsBundle.message("script.hints.settings.textLengthLimit.tooltip") }
                    textField()
                        .bindIntText(settings::textLengthLimit)
                        .errorOnApply(PlsBundle.message("script.hints.error.shouldBePositiveOrZero")) { (it.text.toIntOrNull() ?: 0) < 0 }
                }
                row {
                    label(PlsBundle.message("script.hints.settings.iconHeightLimit")).widthGroup("left")
                        .applyToComponent { toolTipText = PlsBundle.message("script.hints.settings.iconHeightLimit.tooltip") }
                    textField()
                        .bindIntText(settings::iconHeightLimit)
                        .errorOnApply(PlsBundle.message("script.hints.error.shouldBePositive")) { (it.text.toIntOrNull() ?: 0) <= 0 }
                }
            }
        }
    }
    
    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
        if(element !is ParadoxScriptStringExpressionElement) return true
        if(!element.isExpression()) return true
        val config = ParadoxConfigHandler.getConfigs(element).firstOrNull()
            ?.takeIf { it.expression.type in expressionTypes }
            ?: return true
        val resolved = ParadoxConfigHandler.resolveScriptExpression(element, null, config, config.expression, config.info.configGroup, true)
        if(resolved is ParadoxLocalisationProperty) {
            val localisationInfo = resolved.localisationInfo
            if(localisationInfo != null) {
                val presentation = doCollect(resolved, editor, settings)
                val finalPresentation = presentation?.toFinalPresentation(this, file.project) ?: return true
                val endOffset = element.endOffset
                sink.addInlineElement(endOffset, true, finalPresentation, false)
            }
        }
        return true
    }
    
    private fun PresentationFactory.doCollect(localisation: ParadoxLocalisationProperty, editor: Editor, settings: Settings): InlayPresentation? {
        return ParadoxLocalisationTextHintsRenderer.render(localisation, this, editor, settings.textLengthLimit, settings.iconHeightLimit)
    }
}

