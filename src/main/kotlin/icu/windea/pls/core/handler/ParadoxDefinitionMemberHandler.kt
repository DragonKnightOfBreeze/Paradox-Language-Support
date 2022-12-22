package icu.windea.pls.core.handler

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理定义成员信息。
 */
object ParadoxDefinitionMemberHandler {
	@JvmStatic
	fun getInfo(element: ParadoxScriptMemberElement): ParadoxDefinitionMemberInfo? {
		//注意：element.stub可能会导致ProcessCanceledException
		ProgressManager.checkCanceled()
		return getInfoFromCache(element)
	}
	
	private fun getInfoFromCache(element: ParadoxScriptMemberElement): ParadoxDefinitionMemberInfo? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedDefinitionMemberInfoKey) {
			val file = element.containingFile
			val value = resolveInfoDownUp(element)
			CachedValueProvider.Result.create(value, file) //invalidated on file modification
		}
	}
	
	//@JvmStatic
	//fun resolveInfoUpDown(element: LighterASTNode): ParadoxDefinitionMemberInfo? {
	//	TODO()
	//}
	
	@JvmStatic
	fun resolveInfoDownUp(element: ParadoxScriptMemberElement): ParadoxDefinitionMemberInfo? {
		//element: ParadoxScriptPropertyKey | ParadoxScriptValue
		//这里输入的element本身可以是定义，这时elementPath会是空字符串
		val (elementPath, definition) = ParadoxElementPathHandler.resolveFromDefinitionWithDefinition(element) ?: return null
		val definitionInfo = definition.definitionInfo ?: return null
		val scope = definitionInfo.subtypeConfigs.find { it.pushScope != null }?.pushScope
		val gameType = definitionInfo.gameType
		val project = definitionInfo.project
		val configGroup = getCwtConfig(project).getValue(gameType)
		return ParadoxDefinitionMemberInfo(elementPath, scope, gameType, definitionInfo, configGroup, element)
	}
}
