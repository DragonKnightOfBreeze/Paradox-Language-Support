package icu.windea.pls.script.codeInsight.navigation

import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.pom.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.support.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.lang.support.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.support.*
import icu.windea.pls.script.psi.*

//com.intellij.testIntegration.GotoTestOrCodeHandler

@Suppress("DialogTitleCapitalization")
class GotoRelatedCwtConfigHandler : GotoTargetHandler() {
	override fun getFeatureUsedKey(): String {
		return "navigation.goto.paradoxRelatedCwtConfig"
	}
	
	override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
		val offset = editor.caretModel.offset
		val location = findElement(file, offset) ?: return null
		//获取所有匹配的CWT规则，不存在匹配的CWT规则时，选用所有默认的CWT规则（对于propertyConfig来说是匹配key的，对于valueConfig来说是所有）
		//包括内联规则（例如alias，显示时使用特殊的别名图标）
		//如果对应，也包括一些相关的规则，如modifierConfig
		val isKey = location is ParadoxScriptPropertyKey
		val configs = ParadoxCwtConfigHandler.resolveConfigs(location, true, isKey)
		val targets = buildSet {
			for(config in configs) {
				config.pointer.element?.let { add(it) }
				config.resolvedOrNull()?.pointer?.element?.let { add(it) }
				
				if(location is ParadoxScriptStringExpressionElement) {
					val configGroup = config.info.configGroup
					val name = location.value 
					val dataType = config.expression.type
					when {
						dataType == CwtDataType.Enum -> {
							configGroup.enums[name]?.pointer?.element?.let { add(it) }
							configGroup.complexEnums[name]?.pointer?.element?.let { add(it) }
						}
						dataType.isValueSetValueType() -> {
							configGroup.values[name]?.pointer?.element?.let { add(it) }
						}
						dataType == CwtDataType.Modifier -> {
							//这里需要遍历所有解析器
							configGroup.predefinedModifiers[name]?.pointer?.element?.let { add(it) }
							ParadoxModifierResolver.EP_NAME.extensions.forEach { resolver ->
								val modifierElement = resolver.resolveModifier(name, location, configGroup)
								val configElement = modifierElement?.modifierConfig?.pointer?.element
								configElement?.let { add(it) }
							}
						}
						else -> pass()
					}
				}
			}
		}
		return GotoData(location, targets.toTypedArray(), emptyList())
	}
	
	private fun findElement(file: PsiFile, offset: Int): PsiElement? {
		return file.findElementAt(offset) {
			it.parentOfTypes(ParadoxScriptPropertyKey::class, ParadoxScriptValue::class)
		}?.takeIf { it.isExpression() }
	}
	
	override fun shouldSortTargets(): Boolean {
		return false
	}
	
	override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
		val expression = sourceElement.castOrNull<ParadoxTypedElement>()?.expression ?: PlsConstants.unknownString
		return PlsBundle.message("script.goto.relatedCwtConfig.chooseTitle", expression.escapeXml())
	}
	
	override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
		val expression = sourceElement.castOrNull<ParadoxTypedElement>()?.expression ?: PlsConstants.unknownString
		return PlsBundle.message("script.goto.relatedCwtConfig.findUsagesTitle", expression)
	}
	
	override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
		return PlsBundle.message("script.goto.relatedCwtConfig.notFoundMessage")
	}
	
	override fun useEditorFont(): Boolean {
		return false
	}
	
	override fun navigateToElement(descriptor: Navigatable) {
		if(descriptor is PsiElement) {
			NavigationUtil.activateFileWithPsiElement(descriptor, true)
		} else {
			descriptor.navigate(true)
		}
	}
}
