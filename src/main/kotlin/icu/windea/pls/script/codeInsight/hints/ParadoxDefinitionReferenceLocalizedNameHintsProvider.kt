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

/**
 * 定义引用信息的内嵌提示（对应定义的名字和类型、本地化名字）。
 */
@Suppress("UnstableApiUsage")
class ParadoxDefinitionReferenceLocalizedNameHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
	companion object {
		private val settingsKey = SettingsKey<NoSettings>("ParadoxDefinitionReferenceLocalizedNameHintsSettingsKey")
		private val keyExpressionTypes = arrayOf(
			CwtKeyExpression.Type.TypeExpression,
			CwtKeyExpression.Type.TypeExpressionString,
			CwtKeyExpression.Type.AliasName, //需要兼容alias
			CwtKeyExpression.Type.AliasKeysField //需要兼容alias
		)
		private val valueExpressionTypes = arrayOf(
			CwtValueExpression.Type.TypeExpression,
			CwtValueExpression.Type.TypeExpressionString,
			CwtValueExpression.Type.SingleAliasRight, //需要兼容single_alias
			CwtValueExpression.Type.AliasKeysField, //需要兼容alias
			CwtValueExpression.Type.AliasMatchLeft //需要兼容alias
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
			val resolved = resolveKey(element) { it.type in keyExpressionTypes }
			if(resolved is ParadoxDefinitionProperty) {
				val definitionInfo = resolved.definitionInfo
				if(definitionInfo != null) {
					val presentation = collectDefinition(resolved, definitionInfo) ?: return true
					val finalPresentation = presentation.toFinalPresentation(this, file, element.project)
					val endOffset = element.endOffset
					sink.addInlineElement(endOffset, false, finalPresentation, false)
					
				}
			}
		} else if(element is ParadoxScriptValue) {
			val resolved = resolveValue(element) { it.type in valueExpressionTypes }
			if(resolved is ParadoxDefinitionProperty) {
				val definitionInfo = resolved.definitionInfo
				if(definitionInfo != null) {
					val presentation = collectDefinition(resolved, definitionInfo) ?: return true
					val finalPresentation = presentation.toFinalPresentation(this, file, element.project)
					val endOffset = element.endOffset
					sink.addInlineElement(endOffset, false, finalPresentation, false)
				}
			}
		}
		return true
	}
	
	private fun PresentationFactory.collectDefinition(element: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo): InlayPresentation? {
		val project = element.project
		val primaryLocalisationConfigs = definitionInfo.primaryLocalisationConfigs
		for(primaryLocalisationConfig in primaryLocalisationConfigs) {
			val resolved = primaryLocalisationConfig.locationExpression.resolve(definitionInfo.name, inferParadoxLocale(), project)
			val localisation = resolved.second
			if(localisation != null) {
				val localizedName = localisation.extractText().truncate(truncateLimit) //TODO 渲染成富文本
				return smallText(localizedName)
			}
		}
		return null
	}
}