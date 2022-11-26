package icu.windea.pls.core.handler

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理定义元素信息。
 */
object ParadoxDefinitionElementInfoHandler {
	@JvmStatic
	fun get(element: PsiElement): ParadoxDefinitionElementInfo? {
		val targetElement = if(element is ParadoxScriptProperty) element.propertyKey else element
		if(!targetElement.isExpressionElement()) return null
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedDefinitionElementInfoKey) {
			val file = element.containingFile
			val value = resolveDownUp(targetElement)
			CachedValueProvider.Result.create(value, file) //invalidated on file modification
		}
	}
	
	//@JvmStatic
	//fun resolveUpDown(element: LighterASTNode): ParadoxDefinitionElementInfo? {
	//	TODO()
	//}
	
	@JvmStatic
	fun resolveDownUp(element: PsiElement): ParadoxDefinitionElementInfo? {
		//element: ParadoxScriptPropertyKey | ParadoxScriptValue
		//这里输入的element本身可以是定义，这时elementPath会是空字符串
		val (elementPath, definition) = ParadoxElementPathHandler.resolveFromDefinitionWithDefinition(element) ?: return null
		val definitionInfo = definition.definitionInfo ?: return null
		val scope = definitionInfo.subtypeConfigs.find { it.pushScope != null }?.pushScope
		val gameType = definitionInfo.gameType
		val project = definitionInfo.project
		val configGroup = getCwtConfig(project).getValue(gameType)
		return ParadoxDefinitionElementInfo(elementPath, scope, configGroup.gameType, definitionInfo, configGroup, element)
	}
}
