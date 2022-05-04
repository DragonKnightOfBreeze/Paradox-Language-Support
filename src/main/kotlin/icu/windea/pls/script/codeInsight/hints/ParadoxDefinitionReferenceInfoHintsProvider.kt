package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.refactoring.suggested.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

/**
 * 定义引用信息的内嵌提示（对应定义的名字和类型、本地化名字）。
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionReferenceInfoHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
	companion object {
		private val settingsKey: SettingsKey<NoSettings> = SettingsKey("ParadoxDefinitionReferenceInfoHintsSettingsKey")
		private val keyExpressionTypes: Array<CwtKeyExpression.Type> = arrayOf(
			CwtKeyExpression.Type.TypeExpression,
			CwtKeyExpression.Type.TypeExpressionString,
			CwtKeyExpression.Type.AliasName, //需要兼容alias
			CwtKeyExpression.Type.AliasKeysField //需要兼容alias
		)
		private val valueExpressionTypes: Array<CwtValueExpression.Type> = arrayOf(
			CwtValueExpression.Type.TypeExpression,
			CwtValueExpression.Type.TypeExpressionString,
			CwtValueExpression.Type.SingleAliasRight, //需要兼容single_alias
			CwtValueExpression.Type.AliasKeysField, //需要兼容alias
			CwtValueExpression.Type.AliasMatchLeft //需要兼容alias
		)
	}
	
	override val name: String get() = PlsBundle.message("script.hints.definitionReferenceInfo")
	override val description: String get() = PlsBundle.message("script.hints.definitionReferenceInfo.description")
	override val key: SettingsKey<NoSettings> get() = settingsKey
	
	override fun createSettings() = NoSettings()
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, sink: InlayHintsSink): Boolean {
		val elementType = element.elementType ?: return false
		if(elementType == ParadoxScriptElementTypes.ROOT_BLOCK) return true
		if(element is ParadoxScriptPropertyKey) {
			val resolved = resolveKey(element) { it.type in keyExpressionTypes }
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
			val resolved = resolveValue(element) { it.type in valueExpressionTypes }
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
		val name = definitionInfo.name
		//如果definitionName和rootKey相同，则省略definitionName
		if(name.equals(definitionInfo.rootKey, true)) {
			presentations.add(smallText(": "))
		} else {
			presentations.add(smallText("$name: "))
		}
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

