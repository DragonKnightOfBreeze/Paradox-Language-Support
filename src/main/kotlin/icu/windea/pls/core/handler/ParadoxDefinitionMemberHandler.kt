package icu.windea.pls.core.handler

import com.intellij.openapi.progress.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
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
		val (elementPath, definition) = ParadoxElementPathHandler.resolveFromDefinitionWithDefinition(element, true) ?: return null
		val definitionInfo = definition.definitionInfo ?: return null
		val configGroup = definitionInfo.configGroup
		val gameType = definitionInfo.gameType
		beforeResolveDefinitionMemberInfo(definition, definitionInfo, configGroup)
		return ParadoxDefinitionMemberInfo(elementPath, gameType, definitionInfo, configGroup, element)
	}
	
	private fun beforeResolveDefinitionMemberInfo(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, configGroup: CwtConfigGroup) {
		//bind missing declaration config for swap types
		val definitionType = definitionInfo.type
		if(!configGroup.declarations.containsKey(definitionType)) {
			val (upElementPath, upDefinition) = ParadoxElementPathHandler.resolveFromDefinitionWithDefinition(definition, false) ?: return
			val upDeclaration = upDefinition.definitionInfo?.declaration ?: return
			var declaration = upDeclaration
			for((key) in upElementPath) {
				declaration = declaration.properties?.find { it.key == key } ?: return
			}
			val declarationConfig = CwtDeclarationConfig(declaration.pointer, declaration.info, declaration.key, declaration)
			configGroup.declarations[definitionType] = declarationConfig
		}
	}
}
