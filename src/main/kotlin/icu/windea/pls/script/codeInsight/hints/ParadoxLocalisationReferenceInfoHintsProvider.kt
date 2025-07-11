@file:Suppress("UnstableApiUsage")

package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.codeInsight.hints.ParadoxLocalisationReferenceInfoHintsProvider.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 本地化引用信息的内嵌提示（对应的本地化的渲染后文本，如果过长则会截断）。
 */
class ParadoxLocalisationReferenceInfoHintsProvider : ParadoxScriptHintsProvider<Settings>() {
    data class Settings(
        var textLengthLimit: Int = PlsInternalSettings.textLengthLimit,
        var iconHeightLimit: Int = PlsInternalSettings.iconHeightLimit,
    )

    private val settingsKey = SettingsKey<Settings>("ParadoxLocalisationReferenceInfoHintsSettingsKey")
    private val expressionTypes = mutableSetOf(
        CwtDataTypes.Localisation,
        CwtDataTypes.InlineLocalisation,
        CwtDataTypes.SyncedLocalisation,
        CwtDataTypes.AliasName, //需要兼容alias
        CwtDataTypes.AliasKeysField, //需要兼容alias
        CwtDataTypes.AliasMatchLeft, //需要兼容alias
        CwtDataTypes.SingleAliasRight, //需要兼容single_alias
    )

    override val name: String get() = PlsBundle.message("script.hints.localisationReferenceInfo")
    override val description: String get() = PlsBundle.message("script.hints.localisationReferenceInfo.description")
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
        val config = ParadoxExpressionManager.getConfigs(element).firstOrNull()
            ?.takeIf { it.configExpression.type in expressionTypes }
            ?: return true
        val resolved = ParadoxExpressionManager.resolveScriptExpression(element, null, config, config.configExpression, true)
        if (resolved is ParadoxLocalisationProperty) {
            val localisationInfo = resolved.localisationInfo
            if (localisationInfo != null) {
                val presentation = doCollect(resolved, editor, settings)
                val finalPresentation = presentation?.toFinalPresentation(this, file.project) ?: return true
                val endOffset = element.endOffset
                sink.addInlineElement(endOffset, true, finalPresentation, false)
            }
        }
        return true
    }

    private fun PresentationFactory.doCollect(localisation: ParadoxLocalisationProperty, editor: Editor, settings: Settings): InlayPresentation? {
        return ParadoxLocalisationTextInlayRenderer(editor, this).withLimit(settings.textLengthLimit, settings.iconHeightLimit).render(localisation)
    }
}

