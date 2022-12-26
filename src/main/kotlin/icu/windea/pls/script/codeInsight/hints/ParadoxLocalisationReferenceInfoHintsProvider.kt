package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.handler.ParadoxCwtConfigHandler.resolvePropertyConfigs
import icu.windea.pls.core.handler.ParadoxCwtConfigHandler.resolveValueConfigs
import icu.windea.pls.core.tool.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.codeInsight.hints.ParadoxLocalisationReferenceInfoHintsProvider.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 本地化引用信息的内嵌提示（对应的本地化的渲染后文本，如果过长则会截断）。
 */
@Suppress("UnstableApiUsage")
class ParadoxLocalisationReferenceInfoHintsProvider : ParadoxScriptHintsProvider<Settings>() {
	companion object {
		private val settingsKey: SettingsKey<Settings> = SettingsKey("ParadoxLocalisationReferenceInfoHintsSettingsKey")
		private val keyExpressionTypes: Array<CwtDataType> = arrayOf(
			CwtDataType.Localisation,
			CwtDataType.InlineLocalisation,
			CwtDataType.SyncedLocalisation,
			CwtDataType.AliasName, //需要兼容alias
			CwtDataType.AliasKeysField //需要兼容alias
		)
		private val valueExpressionTypes: Array<CwtDataType> = arrayOf(
			CwtDataType.Localisation,
			CwtDataType.InlineLocalisation,
			CwtDataType.SyncedLocalisation,
			CwtDataType.StellarisNameFormat,
			CwtDataType.SingleAliasRight, //需要兼容single_alias
			CwtDataType.AliasKeysField, //需要兼容alias
			CwtDataType.AliasMatchLeft //需要兼容alias
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
						.errorOnApply(PlsBundle.message("script.hints.error.shouldBePositive")) { (it.text.toIntOrNull() ?: 0) <= 0 }
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
		val resolved = when(element) {
			is ParadoxScriptPropertyKey -> {
				val config = resolvePropertyConfigs(element).firstOrNull()
					?.takeIf { it.expression.type in keyExpressionTypes }
					?: return true
				CwtConfigHandler.resolveScriptExpression(element, null, config, config.info.configGroup, true)
			}
			is ParadoxScriptString -> {
				val config = resolveValueConfigs(element).firstOrNull()
					?.takeIf { it.expression.type in valueExpressionTypes }
					?: return true
				CwtConfigHandler.resolveScriptExpression(element, null, config, config.info.configGroup, false)
			}
			else -> return true
		}
		if(resolved is ParadoxLocalisationProperty) {
			val localisationInfo = resolved.localisationInfo
			if(localisationInfo != null) {
				val presentation = collectLocalisation(resolved, editor, settings)
				val finalPresentation = presentation?.toFinalPresentation(this, file.project) ?: return true
				val endOffset = element.endOffset
				sink.addInlineElement(endOffset, true, finalPresentation, false)
			}
		}
		return true
	}
	
	private fun PresentationFactory.collectLocalisation(localisation: ParadoxLocalisationProperty, editor: Editor, settings: Settings): InlayPresentation? {
		return ParadoxLocalisationTextHintsRenderer.render(localisation, this, editor, settings.textLengthLimit, settings.iconHeightLimit)
	}
}

