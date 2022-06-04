package icu.windea.pls.script.codeInsight.hints

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.refactoring.suggested.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*

/**
 * 定义引用信息的内嵌提示（对应定义的名字和类型、本地化名字）。
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionReferenceLocalizedNameHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
	companion object {
		private val settingsKey: SettingsKey<NoSettings> = SettingsKey("ParadoxDefinitionReferenceLocalizedNameHintsSettingsKey")
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
	
	override val name: String get() = PlsBundle.message("script.hints.definitionReferenceLocalizedName")
	override val description: String get() = PlsBundle.message("script.hints.definitionReferenceLocalizedName.description")
	override val key: SettingsKey<NoSettings> get() = settingsKey
	
	override fun createSettings() = NoSettings()
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, sink: InlayHintsSink): Boolean {
		val elementType = element.elementType ?: return false
		if(elementType == ParadoxScriptElementTypes.ROOT_BLOCK) return true
		if(element is ParadoxScriptPropertyKey) {
			val resolved = CwtConfigHandler.resolveKey(element) { it.type in keyExpressionTypes }
			if(resolved is ParadoxDefinitionProperty) {
				val definitionInfo = resolved.definitionInfo
				if(definitionInfo != null) {
					val presentation = collectDefinition(resolved, definitionInfo, editor) ?: return true
					val finalPresentation = presentation.toFinalPresentation(this, file, element.project)
					val endOffset = element.endOffset
					sink.addInlineElement(endOffset, false, finalPresentation, false)
					
				}
			}
		} else if(element is ParadoxScriptString) {
			val resolved = CwtConfigHandler.resolveValue(element) { it.type in valueExpressionTypes }
			if(resolved is ParadoxDefinitionProperty) {
				val definitionInfo = resolved.definitionInfo
				if(definitionInfo != null) {
					val presentation = collectDefinition(resolved, definitionInfo, editor) ?: return true
					val finalPresentation = presentation.toFinalPresentation(this, file, element.project)
					val endOffset = element.endOffset
					sink.addInlineElement(endOffset, false, finalPresentation, false)
				}
			}
		}
		return true
	}
	
	private fun PresentationFactory.collectDefinition(definition: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo, editor: Editor): InlayPresentation? {
		val primaryLocalisation = definitionInfo.resolvePrimaryLocalisation(definition) ?: return null
		return ParadoxLocalisationTextHintsRenderer.render(primaryLocalisation, this, editor)
	}
}