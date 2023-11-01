package icu.windea.pls.lang.config.impl

import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于获取内联脚本调用中的CWT规则上下文。
 *
 * * 正常提供代码高亮、引用解析、代码补全等高级语言功能。
 */
class CwtInlineScriptUsageConfigContextProvider: CwtConfigContextProvider {
    //注意：内联脚本调用可以在定义声明之外
    
    override fun getContext(element: ParadoxScriptMemberElement, elementPath: ParadoxElementPath, file: PsiFile): CwtConfigContext? {
        val vFile = selectFile(file) ?: return null
        
        //要求当前位置相对于文件的元素路径中包含子路径"inline_script"
        val rootIndex = elementPath.indexOfFirst { it.subPath.equals(ParadoxInlineScriptHandler.inlineScriptKey, true) }
        if(rootIndex == -1) return null
        
        val fileInfo = vFile.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val elementPathFromRoot = ParadoxElementPath.resolve(elementPath.rawSubPaths.let { it.subList(rootIndex + 1, it.size) })
        val configGroup = getConfigGroups(file.project).get(gameType)
        val configContext = CwtConfigContext(element, fileInfo, elementPath, gameType, configGroup)
        configContext.elementPathFromRoot = elementPathFromRoot
        return configContext
    }
    
    override fun getCacheKey(context: CwtConfigContext, matchOptions: Int): String? {
        val gameTypeId = context.gameType.id
        val path = context.fileInfo?.path ?: return null // null -> unexpected
        val elementPathFromRoot = context.elementPathFromRoot ?: return null // null -> unexpected
        val isPropertyValue = context.element is ParadoxScriptValue && context.element.isPropertyValue()
        return "isu@$gameTypeId:${matchOptions}#${isPropertyValue.toInt()}#${path.path}\n${elementPathFromRoot.path}"
    }
    
    override fun getConfigs(context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        val elementPathFromRoot = context.elementPathFromRoot ?: return null
        val configGroup = context.configGroup
        val inlineConfigs = configGroup.inlineConfigGroup[ParadoxInlineScriptHandler.inlineScriptKey] ?: return null
        val element = context.element
        val rootConfigs = inlineConfigs.map { it.inline() }
        return CwtConfigHandler.getConfigsForConfigContext(element, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
    }
}