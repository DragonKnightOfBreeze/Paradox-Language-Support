package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理定义成员。
 * 
 * @see ParadoxScriptMemberElement
 * @see ParadoxDefinitionMemberInfo
 */
object ParadoxMemberHandler {
    fun getDefinitionMemberInfo(element: ParadoxScriptMemberElement): ParadoxDefinitionMemberInfo? {
        return doGetDefinitionMemberInfoFromCache(element)
    }
    
    private fun doGetDefinitionMemberInfoFromCache(element: ParadoxScriptMemberElement): ParadoxDefinitionMemberInfo? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedDefinitionMemberInfoKey) {
            ProgressManager.checkCanceled()
            val value = doGetDefinitionMemberDownUp(element)
            //invalidated on ScriptFileTracker
            //to optimize performance, do not invoke file.containingFile here
            val tracker = ParadoxPsiModificationTracker.getInstance(element.project).ScriptFileTracker
            CachedValueProvider.Result.create(value, tracker)
        }
    }
    
    private fun doGetDefinitionMemberDownUp(element: ParadoxScriptMemberElement): ParadoxDefinitionMemberInfo? {
        //input element here can also be a definition, at this time element path will be empty
        val (elementPath, definition) = ParadoxElementPathHandler.getFromDefinitionWithDefinition(element, true) ?: return null
        val definitionInfo = definition.definitionInfo ?: return null
        val configGroup = definitionInfo.configGroup
        val gameType = definitionInfo.gameType
        doHandleDefinitionMemberInfo(definition, definitionInfo, configGroup)
        return ParadoxDefinitionMemberInfo(elementPath, definitionInfo, gameType, configGroup, element)
    }
    
    private fun doHandleDefinitionMemberInfo(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, configGroup: CwtConfigGroup) {
        //bind missing declaration config for swap types
        val definitionType = definitionInfo.type
        if(!configGroup.declarations.containsKey(definitionType)) {
            val (upElementPath, upDefinition) = ParadoxElementPathHandler.getFromDefinitionWithDefinition(definition, false) ?: return
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
