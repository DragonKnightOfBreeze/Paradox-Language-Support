package icu.windea.pls.lang.config.impl

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于获取内联脚本调用中的CWT规则上下文。
 */
class ParadoxInlineScriptUsageConfigContextProvider: ParadoxConfigContextProvider {
    //注意：内联脚本调用可以在定义声明之外
    
    override fun getConfigContext(element: ParadoxScriptMemberElement, elementPath: ParadoxElementPath, file: PsiFile): ParadoxConfigContext? {
        val vFile = selectFile(file) ?: return null
        
        //要求当前位置相对于文件的元素路径中包含子路径"inline_script"
        val rootIndex = elementPath.indexOfFirst { it.subPath.equals(ParadoxInlineScriptHandler.inlineScriptKey, true) }
        if(rootIndex == -1) return null
        
        val fileInfo = vFile.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val elementPathFromRoot = ParadoxElementPath.resolve(elementPath.rawSubPaths.let { it.subList(rootIndex + 1, it.size) })
        val configGroup = getCwtConfig(file.project).get(gameType)
        val configContext = ParadoxConfigContext(fileInfo, elementPath, gameType, configGroup, element)
        configContext.elementPathFromRoot = elementPathFromRoot
        return configContext
    }
    
    override fun getConfigs(element: ParadoxScriptMemberElement, configContext: ParadoxConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        val elementPathFromRoot = configContext.elementPathFromRoot ?: return null
        val configGroup = configContext.configGroup
        val inlineConfigs = configGroup.inlineConfigGroup[ParadoxInlineScriptHandler.inlineScriptKey] ?: return null
        val rootConfigs = inlineConfigs.map { ParadoxConfigInlineHandler.inlineWithInlineConfig(it) }
        return ParadoxConfigHandler.getConfigsFromConfigContext(element, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
    }
}