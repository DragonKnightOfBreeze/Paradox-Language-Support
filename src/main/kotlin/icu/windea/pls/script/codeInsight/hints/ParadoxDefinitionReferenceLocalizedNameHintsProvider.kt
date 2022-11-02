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
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.script.codeInsight.hints.ParadoxDefinitionReferenceLocalizedNameHintsProvider.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 定义引用信息的内嵌提示（对应定义的名字和类型、本地化名字）。
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionReferenceLocalizedNameHintsProvider : ParadoxScriptHintsProvider<Settings>() {
	companion object {
		private val settingsKey: SettingsKey<Settings> = SettingsKey("ParadoxDefinitionReferenceLocalizedNameHintsSettingsKey")
		private val keyExpressionTypes: Array<CwtKeyDataType> = arrayOf(
			CwtDataTypes.TypeExpression,
			CwtDataTypes.TypeExpressionString,
			CwtDataTypes.AliasName, //需要兼容alias
			CwtDataTypes.AliasKeysField //需要兼容alias
		)
		private val valueExpressionTypes: Array<CwtValueDataType> = arrayOf(
			CwtDataTypes.TypeExpression,
			CwtDataTypes.TypeExpressionString,
			CwtDataTypes.SingleAliasRight, //需要兼容single_alias
			CwtDataTypes.AliasKeysField, //需要兼容alias
			CwtDataTypes.AliasMatchLeft //需要兼容alias
		)
	}
	
	data class Settings(
		var textLengthLimit: Int = 20,
		var iconHeightLimit: Int = 32
	)
	
	override val name: String get() = PlsBundle.message("script.hints.definitionReferenceLocalizedName")
	override val description: String get() = PlsBundle.message("script.hints.definitionReferenceLocalizedName.description")
	override val key: SettingsKey<Settings> get() = settingsKey
	
	override val previewText: String get() = ParadoxScriptHintsPreviewProvider.civicPreview
	
	override fun createFile(project: Project, fileType: FileType, document: Document): PsiFile {
		return super.createFile(project, fileType, document)
			.also { file -> ParadoxScriptHintsPreviewProvider.handleCivicPreviewFile(file) }
	}
	
	override fun createSettings() = Settings()
	
	override fun createConfigurable(settings: Settings): ImmediateConfigurable {
		return object : ImmediateConfigurable {
			override fun createComponent(listener: ChangeListener): JComponent = panel {
				row {
					label(PlsBundle.message("script.hints.settings.textLengthLimit"))
						.applyToComponent { toolTipText = PlsBundle.message("script.hints.settings.textLengthLimit.tooltip") }
					textField()
						.bindIntText(settings::textLengthLimit)
						.errorOnApply("Int should be positive") { (it.text.toIntOrNull() ?: 0) <= 0 }
				}
				row {
					label(PlsBundle.message("script.hints.settings.iconHeightLimit"))
						.applyToComponent { toolTipText = PlsBundle.message("script.hints.settings.iconHeightLimit.tooltip") }
					textField()
						.bindIntText(settings::iconHeightLimit)
						.errorOnApply("Int should be positive") { (it.text.toIntOrNull() ?: 0) <= 0 }
				}
			}
		}
	}
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: Settings, sink: InlayHintsSink): Boolean {
		val resolved = when(element) {
			is ParadoxScriptPropertyKey -> {
				val config = ParadoxCwtConfigHandler.resolvePropertyConfig(element)
					?.takeIf { it.expression.type in keyExpressionTypes }
					?: return true
				CwtConfigHandler.resolveScriptExpression(element, null, config.expression, config, true)
			}
			is ParadoxScriptString -> {
				val config = ParadoxCwtConfigHandler.resolveValueConfig(element)
					?.takeIf { it.expression.type in valueExpressionTypes }
					?: return true
				CwtConfigHandler.resolveScriptExpression(element, null, config.expression, config, false)
			}
			else -> return true
		}
		if(resolved is ParadoxDefinitionProperty) {
			val definitionInfo = resolved.definitionInfo
			if(definitionInfo != null) {
				val presentation = collectDefinition(resolved, definitionInfo, editor, settings) ?: return true
				val finalPresentation = presentation.toFinalPresentation(this, file.project)
				val endOffset = element.endOffset
				sink.addInlineElement(endOffset, true, finalPresentation, false)
			}
		}
		return true
	}
	
	private fun PresentationFactory.collectDefinition(definition: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo, editor: Editor, settings: Settings): InlayPresentation? {
		val primaryLocalisation = definitionInfo.resolvePrimaryLocalisation(definition) ?: return null
		return ParadoxLocalisationTextHintsRenderer.render(primaryLocalisation, this, editor, settings.textLengthLimit, settings.iconHeightLimit)
	}
}