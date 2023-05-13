package icu.windea.pls.core.codeInsight.navigation

import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.pom.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

//com.intellij.testIntegration.GotoTestOrCodeHandler

@Suppress("DialogTitleCapitalization")
class GotoRelatedCwtConfigsHandler : GotoTargetHandler() {
	override fun getFeatureUsedKey(): String {
		return "navigation.goto.paradoxRelatedCwtConfigs"
	}
	
	override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
		val offset = editor.caretModel.offset
		val location = findElement(file, offset) ?: return null
		//获取所有匹配的CWT规则，不存在匹配的CWT规则时，选用所有默认的CWT规则（对于propertyConfig来说是匹配key的，对于valueConfig来说是所有）
		//包括内联规则（例如alias，显示时使用特殊的别名图标）
		//如果对应，也包括一些相关的规则，如modifierConfig
		val isKey = location is ParadoxScriptPropertyKey
		val configs = ParadoxConfigHandler.getConfigs(location, true, isKey)
		val targets = buildSet {
			for(config in configs) {
				val configGroup = config.info.configGroup
				
				if(location is ParadoxScriptStringExpressionElement) {
					val name = location.value
					when {
						config.isRoot -> {
							configGroup.declarations.get(name)?.pointer?.element?.let { add(it) }
							when {
								name == "game_rule" -> {
									configGroup.gameRules.get(name)?.pointer?.element?.let { add(it) }
								}
								name == "on_action" -> {
									configGroup.onActions.getByTemplate(name, location, configGroup)?.pointer?.element?.let { add(it) }
								}
							}
						}
					}
				}
				
				config.pointer.element?.let { add(it) }
				config.resolvedOrNull()?.pointer?.element?.let { add(it) }
				
				if(location is ParadoxScriptStringExpressionElement) {
					val name = location.value
					val configExpression = config.expression
					when {
						configExpression.type == CwtDataType.EnumValue -> {
							configGroup.enums[name]?.pointer?.element?.let { add(it) }
							configGroup.complexEnums[name]?.pointer?.element?.let { add(it) }
						}
						configExpression.type.isValueSetValueType() -> {
							configGroup.values[name]?.pointer?.element?.let { add(it) }
						}
						configExpression.type == CwtDataType.Modifier -> {
							val modifierElement = ParadoxModifierHandler.resolveModifier(name, location, configGroup)
							val configElement = modifierElement?.getUserData(ParadoxModifierHandler.modifierConfigKey)?.pointer?.element
							configElement?.let { add(it) }
						}
						else -> pass()
					}
				}
			}
		}
		return GotoData(location, targets.toTypedArray(), emptyList())
	}
	
	private fun findElement(file: PsiFile, offset: Int): ParadoxScriptExpressionElement? {
		return ParadoxPsiFinder.findScriptExpression(file, offset)
	}
	
	override fun shouldSortTargets(): Boolean {
		return false
	}
	
	override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
		val expression = sourceElement.castOrNull<ParadoxTypedElement>()?.expression ?: PlsConstants.unknownString
		return PlsBundle.message("script.goto.relatedCwtConfigs.chooseTitle", expression.escapeXml())
	}
	
	override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
		val expression = sourceElement.castOrNull<ParadoxTypedElement>()?.expression ?: PlsConstants.unknownString
		return PlsBundle.message("script.goto.relatedCwtConfigs.findUsagesTitle", expression)
	}
	
	override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
		return PlsBundle.message("script.goto.relatedCwtConfigs.notFoundMessage")
	}
	
	override fun navigateToElement(descriptor: Navigatable) {
		if(descriptor is PsiElement) {
			NavigationUtil.activateFileWithPsiElement(descriptor, true)
		} else {
			descriptor.navigate(true)
		}
	}
}
