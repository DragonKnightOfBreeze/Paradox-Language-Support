package icu.windea.pls.lang.config.impl

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于获取直接的CWT规则上下文。
 */
class ParadoxBaseConfigContextProvider : ParadoxConfigContextProvider {
    override fun getConfigContext(element: ParadoxScriptMemberElement, elementPath: ParadoxElementPath, file: PsiFile): ParadoxConfigContext? {
        val vFile = selectFile(file) ?: return null
        if(ParadoxFileManager.isInjectedFile(vFile)) return null //ignored for injected psi
        
        val fileInfo = vFile.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val definition = element.findParentDefinition()
        if(definition == null) {
            val configGroup = getCwtConfig(file.project).get(gameType)
            val configContext = ParadoxConfigContext(fileInfo, elementPath, gameType, configGroup, element)
            return configContext
        } else {
            val definitionInfo = definition.definitionInfo ?: return null
            val definitionElementPath = definitionInfo.elementPath
            val elementPathFromRoot = definitionElementPath.relativeTo(elementPath) ?: return null
            val configGroup = getCwtConfig(file.project).get(gameType)
            val configContext = ParadoxConfigContext(fileInfo, elementPath, gameType, configGroup, element)
            configContext.definitionInfo = definitionInfo
            configContext.elementPathFromRoot = elementPathFromRoot
            return configContext
        }
    }
    
    override fun getConfigs(element: ParadoxScriptMemberElement, configContext: ParadoxConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        val elementPathFromRoot = configContext.elementPathFromRoot ?: return null
        val configGroup = configContext.configGroup
        val definitionInfo = configContext.definitionInfo ?: return null
        val declaration = definitionInfo.getDeclaration(matchOptions) ?: return null
        val rootConfigs = declaration.toSingletonList()
        return ParadoxConfigHandler.getConfigsFromConfigContext(element, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
    }
}
