package icu.windea.pls.lang.config.impl

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于获取直接的CWT规则上下文。
 */
class ParadoxBaseConfigContextProvider : ParadoxConfigContextProvider {
    override fun getConfigContext(element: ParadoxScriptMemberElement, elementPath: ParadoxElementPath, file: PsiFile): ParadoxConfigContext? {
        val vFile = selectFile(file) ?: return null
        if(ParadoxFileManager.isInjectedFile(vFile)) return null //ignored for injected psi
        
        ProgressManager.checkCanceled()
        val fileInfo = vFile.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val definition = element.findParentDefinition()
        if(definition == null) {
            val configGroup = getConfigGroups(file.project).get(gameType)
            val configContext = ParadoxConfigContext(element, fileInfo, elementPath, gameType, configGroup)
            return configContext
        } else {
            val definitionInfo = definition.definitionInfo ?: return null
            val definitionElementPath = definitionInfo.elementPath
            val elementPathFromRoot = definitionElementPath.relativeTo(elementPath) ?: return null
            val configGroup = getConfigGroups(file.project).get(gameType)
            val configContext = ParadoxConfigContext(element, fileInfo, elementPath, gameType, configGroup)
            configContext.definitionInfo = definitionInfo
            configContext.elementPathFromRoot = elementPathFromRoot
            return configContext
        }
    }
    
    override fun getCacheKey(configContext: ParadoxConfigContext, matchOptions: Int): String? {
        val definitionInfo = configContext.definitionInfo ?: return null
        val declarationConfig = definitionInfo.getDeclaration(matchOptions) ?: return null
        val declarationConfigContextCacheKey = declarationConfig.declarationConfigCacheKey ?: return null // null -> unexpected
        val elementPathFromRoot = configContext.elementPathFromRoot ?: return null // null -> unexpected
        return "b@${matchOptions}#${declarationConfigContextCacheKey}\n${elementPathFromRoot}"
    }
    
    override fun getConfigs(configContext: ParadoxConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()
        val elementPathFromRoot = configContext.elementPathFromRoot ?: return null
        val definitionInfo = configContext.definitionInfo ?: return null
        val declarationConfig = definitionInfo.getDeclaration(matchOptions) ?: return null
        val rootConfigs = declarationConfig.toSingletonList()
        val configGroup = configContext.configGroup
        val element = configContext.element
        return ParadoxConfigHandler.getConfigsForConfigContext(element, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
    }
}
