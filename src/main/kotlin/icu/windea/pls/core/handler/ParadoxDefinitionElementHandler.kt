package icu.windea.pls.core.handler

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理定义元素信息。
 */
object ParadoxDefinitionElementHandler {
	@JvmStatic
	fun getInfo(element: PsiElement): ParadoxDefinitionElementInfo? {
		//注意：element.stub可能会导致ProcessCanceledException
		ProgressManager.checkCanceled()
		val targetElement = if(element is ParadoxScriptProperty) element.propertyKey else element
		//这里可以是rootBlock，也可以是expressionElement
		if(!(targetElement is ParadoxScriptRootBlock || targetElement.isExpression())) return null
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedDefinitionElementInfoKey) {
			val file = element.containingFile
			val value = resolveInfoDownUp(targetElement)
			CachedValueProvider.Result.create(value, file) //invalidated on file modification
		}
	}
	
	//@JvmStatic
	//fun resolveInfoUpDown(element: LighterASTNode): ParadoxDefinitionElementInfo? {
	//	TODO()
	//}
	
	@JvmStatic
	fun resolveInfoDownUp(element: PsiElement): ParadoxDefinitionElementInfo? {
		//element: ParadoxScriptPropertyKey | ParadoxScriptValue
		//这里输入的element本身可以是定义，这时elementPath会是空字符串
		val (elementPath, definition) = ParadoxElementPathHandler.resolveFromDefinitionWithDefinition(element) ?: return null
		val definitionInfo = definition.definitionInfo ?: return null
		val scope = definitionInfo.subtypeConfigs.find { it.pushScope != null }?.pushScope
		val gameType = definitionInfo.gameType
		val project = definitionInfo.project
		val configGroup = getCwtConfig(project).getValue(gameType)
		return ParadoxDefinitionElementInfo(elementPath, scope, gameType, definitionInfo, configGroup, element)
	}
}
