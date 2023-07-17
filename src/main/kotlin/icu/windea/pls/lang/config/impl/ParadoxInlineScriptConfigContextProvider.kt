package icu.windea.pls.lang.config.impl

import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于获取内联脚本中的CWT规则上下文。
 * 
 * * 正常提供代码高亮、引用解析、代码补全等高级语言功能。
 * * 对于顶级成员，禁用以下代码检查：`MissingExpressionInspection`和`TooManyExpressionInspection`。
 * * 会将内联脚本内容内联到对应的调用处，然后再进行相关代码检查。
 */
class ParadoxInlineScriptConfigContextProvider : ParadoxConfigContextProvider {
    //注意：内联脚本调用可以在定义声明之外
    
    //TODO 1.1.0+ 支持解析内联脚本文件中的定义声明
    
    //首先推断内联脚本文件的CWT规则上下文：汇总内联脚本调用处的上下文，然后合并得到最终的CWT规则上下文
    //然后再得到当前位置的CWT规则上下文
    
    override fun getConfigContext(element: ParadoxScriptMemberElement, elementPath: ParadoxElementPath, file: PsiFile): ParadoxConfigContext? {
        if(!getSettings().inference.inlineScriptConfig) return null
        
        val vFile = selectFile(file) ?: return null
        if(ParadoxFileManager.isInjectedFile(vFile)) return null //ignored for injected psi
        
        val inlineScriptExpression = ParadoxInlineScriptHandler.getInlineScriptExpression(vFile)
        if(inlineScriptExpression == null) return null
        
        ProgressManager.checkCanceled()
        val fileInfo = vFile.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val elementPathFromRoot = elementPath
        val configGroup = getCwtConfig(file.project).get(gameType)
        val configContext = ParadoxConfigContext(fileInfo, elementPath, gameType, configGroup, element)
        if(elementPathFromRoot.isNotEmpty()) {
            configContext.inlineScriptRootConfigContext = ParadoxConfigHandler.getConfigContext(file) ?: return null
        }
        configContext.elementPathFromRoot = elementPathFromRoot
        configContext.inlineScriptExpression = inlineScriptExpression
        return configContext
    }
    
    //获取CWT规则后才能确定是否存在冲突以及是否存在递归
    
    override fun getConfigs(element: ParadoxScriptMemberElement, configContext: ParadoxConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()
        val elementPathFromRoot = configContext.elementPathFromRoot ?: return null
        
        if(elementPathFromRoot.isNotEmpty()) {
            val rootConfigContext = configContext.inlineScriptRootConfigContext ?: return null
            val rootConfigs = rootConfigContext.getConfigs(matchOptions)
            val configGroup = configContext.configGroup
            return ParadoxConfigHandler.getConfigsFromConfigContext(element, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
        }
        
        val inlineScriptExpression = configContext.inlineScriptExpression ?: return null
        
        // infer & merge
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        configContext.inlineScriptHasConflict = false
        configContext.inlineScriptHasRecursion = false
        withRecursionGuard("icu.windea.pls.lang.config.impl.ParadoxInlineScriptConfigContextProvider.getConfigs") {
            withCheckRecursion(inlineScriptExpression) {
                val project = configContext.configGroup.project
                val selector = inlineScriptSelector(project, configContext.element)
                ParadoxInlineScriptUsageSearch.search(inlineScriptExpression, selector).processQueryAsync p@{ info ->
                    ProgressManager.checkCanceled()
                    val e = info.file?.findElementAt(info.elementOffset) ?: return@p true
                    val p = e.parentOfType<ParadoxScriptProperty>() ?: return@p true
                    if(p.name.lowercase() != ParadoxInlineScriptHandler.inlineScriptKey) return@p true
                    val memberElement = p.parentOfType<ParadoxScriptMemberElement>() ?: return@p true
                    val usageConfigContext = ParadoxConfigHandler.getConfigContext(memberElement) ?: return@p true
                    val usageConfigs = usageConfigContext.getConfigs(matchOptions).takeIfNotEmpty()
                    // merge
                    result.mergeValue(usageConfigs) { v1, v2 -> ParadoxConfigMergeHandler.mergeConfigs(v1, v2) }.also {
                        if(it) return@also
                        configContext.inlineScriptHasConflict = true
                        result.set(null)
                    }
                }
            } ?: run {
                configContext.inlineScriptHasRecursion = true
                result.set(null)
            }
        }
        return result.get()
    }
    
    //skip MissingExpressionInspection and TooManyExpressionInspection at root level
    
    override fun skipMissingExpressionCheck(configContext: ParadoxConfigContext): Boolean {
        val elementPathFromRoot = configContext.elementPathFromRoot ?: return false
        return elementPathFromRoot.isEmpty()
    }
    
    override fun skipTooManyExpressionCheck(configContext: ParadoxConfigContext): Boolean {
        val elementPathFromRoot = configContext.elementPathFromRoot ?: return false
        return elementPathFromRoot.isEmpty()
    }
}

val ParadoxConfigContext.Keys.inlineScriptRootConfigContext by lazy { Key.create<ParadoxConfigContext>("paradox.configContext.inlineScript.rootConfigContext") }
val ParadoxConfigContext.Keys.inlineScriptExpression by lazy { Key.create<String>("paradox.configContext.inlineScript.expression") }
val ParadoxConfigContext.Keys.inlineScriptHasConflict by lazy { Key.create<Boolean>("paradox.configContext.inlineScript.hasConflict") }
val ParadoxConfigContext.Keys.inlineScriptHasRecursion by lazy { Key.create<Boolean>("paradox.configContext.inlineScript.hasRecursion") }

var ParadoxConfigContext.inlineScriptRootConfigContext by ParadoxConfigContext.Keys.inlineScriptRootConfigContext
var ParadoxConfigContext.inlineScriptExpression by ParadoxConfigContext.Keys.inlineScriptExpression
var ParadoxConfigContext.inlineScriptHasConflict by ParadoxConfigContext.Keys.inlineScriptHasConflict
var ParadoxConfigContext.inlineScriptHasRecursion by ParadoxConfigContext.Keys.inlineScriptHasRecursion