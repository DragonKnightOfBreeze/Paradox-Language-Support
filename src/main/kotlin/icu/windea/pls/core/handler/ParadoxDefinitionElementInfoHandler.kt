package icu.windea.pls.core.handler

import com.intellij.lang.LighterASTNode
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理定义元素信息。
 */
object ParadoxDefinitionElementInfoHandler {
	@JvmStatic
	fun get(element: PsiElement): ParadoxDefinitionElementInfo? {
		//必须是脚本语言的PsiElement
		val targetElement = if(element is ParadoxScriptPropertyKey) element.parent ?: return null else element
		if(targetElement.language != ParadoxScriptLanguage) return null
		return targetElement.getOrPutUserData(PlsKeys.definitionElementInfoKey) {
			resolveDownUp(targetElement)
		}
	}
	
	@JvmStatic
	fun resolveUpDown(element: LighterASTNode): ParadoxDefinitionElementInfo? {
		TODO()
	}
	
	@JvmStatic
	fun resolveDownUp(element: PsiElement): ParadoxDefinitionElementInfo? {
		//这里输入的element本身可以是定义，这时elementPath会是空字符串
		val (elementPath, definition) = ParadoxElementPathHandler.resolveFromDefinition(element) ?: return null
		val definitionInfo = definition.definitionInfo ?: return null
		val scope = definitionInfo.subtypeConfigs.find { it.pushScope != null }?.pushScope
		val gameType = definitionInfo.gameType
		val project = definitionInfo.project
		val configGroup = getCwtConfig(project).getValue(gameType)
		return ParadoxDefinitionElementInfo(elementPath, scope, configGroup.gameType, definitionInfo, configGroup, element)
	}
}