package icu.windea.pls.lang.config.impl

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于获取内联脚本调用中的CWT规则上下文。
 *
 * * 正常提供代码高亮、引用解析、代码补全等高级语言功能。
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
        val configGroup = getConfigGroups(file.project).get(gameType)
        val configContext = ParadoxConfigContext(element, fileInfo, elementPath, gameType, configGroup)
        configContext.elementPathFromRoot = elementPathFromRoot
        return configContext
    }
    
    override fun getCacheKey(configContext: ParadoxConfigContext, matchOptions: Int): String? {
        val path = configContext.fileInfo?.path ?: return null // null -> unexpected
        val elementPathFromRoot = configContext.elementPathFromRoot ?: return null // null -> unexpected
        return "isu@${configContext.gameType.id}:${path.path}\n${elementPathFromRoot.path}"
    }
    
    override fun getConfigs(configContext: ParadoxConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        val elementPathFromRoot = configContext.elementPathFromRoot ?: return null
        val configGroup = configContext.configGroup
        val inlineConfigs = configGroup.inlineConfigGroup[ParadoxInlineScriptHandler.inlineScriptKey] ?: return null
        val element = configContext.element
        val rootConfigs = inlineConfigs.map { ParadoxConfigInlineHandler.inlineWithInlineConfig(it) }
        return ParadoxConfigHandler.getConfigsForConfigContext(element, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
    }
}