package icu.windea.pls.core

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.model.*

/**
 * 用于处理定义元素信息。
 */
object ParadoxDefinitionElementInfoHandler {
	fun resolve(element: PsiElement): ParadoxDefinitionElementInfo? {
		return resolveUpDown(element)
	}
	
	fun resolveUpDown(element: PsiElement): ParadoxDefinitionElementInfo? {
		TODO()
	}
	
	@Deprecated("Use resolveUpDown(element) instead")
	fun resolveDownUp(element: PsiElement): ParadoxDefinitionElementInfo? {
		//这里输入的element本身可以是定义，这时elementPath会是空字符串
		val elementPath = ParadoxElementPath.resolveFromDefinition(element) ?: return null
		val definition = elementPath.rootElement ?: return null
		val definitionInfo = definition.definitionInfo ?: return null
		val scope = definitionInfo.subtypeConfigs.find { it.pushScope != null }?.pushScope
		val gameType = definitionInfo.gameType
		val project = element.project
		val configGroup = getCwtConfig(project).getValue(gameType)
		return ParadoxDefinitionElementInfo(elementPath, scope, configGroup.gameType, definitionInfo, configGroup, element)
	}
}