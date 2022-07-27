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
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 定义引用信息的内嵌提示（对应定义的名字和类型、本地化名字）。
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionReferenceInfoHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
	companion object {
		private val settingsKey: SettingsKey<NoSettings> = SettingsKey("ParadoxDefinitionReferenceInfoHintsSettingsKey")
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
	
	override val name: String get() = PlsBundle.message("script.hints.definitionReferenceInfo")
	override val description: String get() = PlsBundle.message("script.hints.definitionReferenceInfo.description")
	override val key: SettingsKey<NoSettings> get() = settingsKey
	
	override fun createSettings() = NoSettings()
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, sink: InlayHintsSink): Boolean {
		if(element is ParadoxScriptPropertyKey) {
			val resolved = CwtConfigHandler.resolveKey(element, file) { it.type in keyExpressionTypes }
			if(resolved is ParadoxDefinitionProperty) {
				val definitionInfo = resolved.definitionInfo
				if(definitionInfo != null) {
					val presentation = collectDefinition(definitionInfo)
					val finalPresentation = presentation.toFinalPresentation(this, file, element.project)
					val endOffset = element.endOffset
					sink.addInlineElement(endOffset, false, finalPresentation, false)
				}
			}
		} else if(element is ParadoxScriptString) {
			val resolved = CwtConfigHandler.resolveValue(element, file) { it.type in valueExpressionTypes }
			if(resolved is ParadoxDefinitionProperty) {
				val definitionInfo = resolved.definitionInfo
				if(definitionInfo != null) {
					val presentation = collectDefinition(definitionInfo)
					val finalPresentation = presentation.toFinalPresentation(this, file, element.project)
					val endOffset = element.endOffset
					sink.addInlineElement(endOffset, false, finalPresentation, false)
				}
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

