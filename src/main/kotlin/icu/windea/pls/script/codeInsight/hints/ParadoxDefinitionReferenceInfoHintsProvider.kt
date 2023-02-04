package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 定义引用信息的内嵌提示（对应定义的名字和类型、本地化名字）。
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionReferenceInfoHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
	companion object {
		private val settingsKey: SettingsKey<NoSettings> = SettingsKey("ParadoxDefinitionReferenceInfoHintsSettingsKey")
		private val keyExpressionTypes: Array<CwtDataType> = arrayOf(
			CwtDataType.Definition,
			CwtDataType.AliasName, //需要兼容alias
			CwtDataType.AliasKeysField //需要兼容alias
		)
		private val valueExpressionTypes: Array<CwtDataType> = arrayOf(
			CwtDataType.Definition,
			CwtDataType.SingleAliasRight, //需要兼容single_alias
			CwtDataType.AliasKeysField, //需要兼容alias
			CwtDataType.AliasMatchLeft //需要兼容alias
		)
	}
	
	override val name: String get() = PlsBundle.message("script.hints.definitionReferenceInfo")
	override val description: String get() = PlsBundle.message("script.hints.definitionReferenceInfo.description")
	override val key: SettingsKey<NoSettings> get() = settingsKey
	
	override fun createSettings() = NoSettings()
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink): Boolean {
		val resolved = when(element) {
			is ParadoxScriptPropertyKey -> {
				val config = ParadoxCwtConfigHandler.resolvePropertyConfigs(element).firstOrNull()
					?.takeIf { it.expression.type in keyExpressionTypes }
					?: return true
				CwtConfigHandler.resolveScriptExpression(element, null, config, config.expression, config.info.configGroup, true)
			}
			is ParadoxScriptString -> {
				val config = ParadoxCwtConfigHandler.resolveValueConfigs(element).firstOrNull()
					?.takeIf { it.expression.type in valueExpressionTypes }
					?: return true
				CwtConfigHandler.resolveScriptExpression(element, null, config, config.expression, config.info.configGroup, false)
			}
			//这也是需要判断的
			is ParadoxScriptInt -> {
				val config = ParadoxCwtConfigHandler.resolveValueConfigs(element).firstOrNull()
					?.takeIf { it.expression.type in valueExpressionTypes }
					?: return true
				CwtConfigHandler.resolveScriptExpression(element, null, config, config.expression, config.info.configGroup, false)
			}
			else -> return true
		}
		if(resolved is ParadoxScriptDefinitionElement) {
			val definitionInfo = resolved.definitionInfo
			if(definitionInfo != null) {
				val presentation = collectDefinition(definitionInfo)
				val finalPresentation = presentation.toFinalPresentation(this, file.project)
				val endOffset = element.endOffset
				sink.addInlineElement(endOffset, true, finalPresentation, false)
			}
		}
		return true
	}
	
	private fun PresentationFactory.collectDefinition(definitionInfo: ParadoxDefinitionInfo): InlayPresentation {
		val presentations: MutableList<InlayPresentation> = SmartList()
		//省略definitionName
		presentations.add(smallText(": "))
		val typeConfig = definitionInfo.typeConfig
		presentations.add(psiSingleReference(smallText(typeConfig.name)) { typeConfig.pointer.element })
		val subtypeConfigs = definitionInfo.subtypeConfigs
		for(subtypeConfig in subtypeConfigs) {
			presentations.add(smallText(", "))
			presentations.add(psiSingleReference(smallText(subtypeConfig.name)) { subtypeConfig.pointer.element })
		}
		return SequencePresentation(presentations)
	}
}

