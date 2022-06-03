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
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*

/**
 * 本地化引用信息的内嵌提示（对应的本地化的渲染后文本，如果过长则会截断）。
 */
@Suppress("UnstableApiUsage")
class ParadoxLocalisationReferenceInfoHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
	companion object {
		private val settingsKey: SettingsKey<NoSettings> = SettingsKey("ParadoxLocalisationReferenceInfoHintsSettingsKey")
		private val keyExpressionTypes: Array<CwtKeyDataType> = arrayOf(
			CwtDataTypes.Localisation,
			CwtDataTypes.InlineLocalisation,
			CwtDataTypes.SyncedLocalisation,
			CwtDataTypes.AliasName, //需要兼容alias
			CwtDataTypes.AliasKeysField //需要兼容alias
		)
		private val valueExpressionTypes: Array<CwtValueDataType> = arrayOf(
			CwtDataTypes.Localisation,
			CwtDataTypes.InlineLocalisation,
			CwtDataTypes.SyncedLocalisation,
			CwtDataTypes.SingleAliasRight, //需要兼容single_alias
			CwtDataTypes.AliasKeysField, //需要兼容alias
			CwtDataTypes.AliasMatchLeft //需要兼容alias
		)
	}
	
	override val name: String get() = PlsBundle.message("script.hints.localisationReferenceInfo")
	override val description: String get() = PlsBundle.message("script.hints.localisationReferenceInfo.description")
	override val key: SettingsKey<NoSettings> get() = settingsKey
	
	override fun createSettings() = NoSettings()
	
	override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, sink: InlayHintsSink): Boolean {
		val elementType = element.elementType ?: return false
		if(elementType == ParadoxScriptElementTypes.ROOT_BLOCK) return true
		if(element is ParadoxScriptPropertyKey) {
			val resolved = CwtConfigHandler.resolveKey(element) { it.type in keyExpressionTypes }
			if(resolved is ParadoxLocalisationProperty) {
				val localisationInfo = resolved.localisationInfo
				if(localisationInfo != null) {
					val presentation = collectLocalisation(resolved, editor)
					val finalPresentation = presentation?.toFinalPresentation(this, file, element.project) ?: return true
					val endOffset = element.endOffset
					sink.addInlineElement(endOffset, false, finalPresentation, false)
				}
			}
		} else if(element is ParadoxScriptString) {
			val resolved = CwtConfigHandler.resolveValue(element) { it.type in valueExpressionTypes }
			if(resolved is ParadoxLocalisationProperty) {
				val localisationInfo = resolved.localisationInfo
				if(localisationInfo != null) {
					val presentation = collectLocalisation(resolved, editor)
					val finalPresentation = presentation?.toFinalPresentation(this, file, element.project) ?: return true
					val endOffset = element.endOffset
					sink.addInlineElement(endOffset, false, finalPresentation, false)
				}
			}
		}
		return true
	}
	
	private fun PresentationFactory.collectLocalisation(localisation: ParadoxLocalisationProperty, editor: Editor): InlayPresentation? {
		return ParadoxLocalisationTextHintsRenderer.render(localisation,this, editor)
	}
}

