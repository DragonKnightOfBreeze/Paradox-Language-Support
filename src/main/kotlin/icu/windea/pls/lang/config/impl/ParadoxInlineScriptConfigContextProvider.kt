package icu.windea.pls.lang.config.impl

import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于获取内联脚本中的CWT规则上下文。
 */
class ParadoxInlineScriptConfigContextProvider : ParadoxConfigContextProvider {
    //注意：内联脚本调用可以在定义声明之外
    
    //TODO 1.1.0+ 支持解析内联脚本文件中的定义声明
    
    override fun getConfigContext(element: ParadoxScriptMemberElement, elementPath: ParadoxElementPath, file: PsiFile): ParadoxConfigContext? {
        if(!getSettings().inference.inlineScriptConfig) return null
        
        val vFile = selectFile(file) ?: return null
        if(ParadoxFileManager.isInjectedFile(vFile)) return null //ignored for injected psi
        
        val inlineScriptExpression = ParadoxInlineScriptHandler.getInlineScriptExpression(vFile)
        if(inlineScriptExpression == null) return null
        
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
        val elementPathFromRoot = configContext.elementPathFromRoot ?: return null
        
        if(elementPathFromRoot.isNotEmpty()) {
            val rootConfigContext = configContext.inlineScriptRootConfigContext ?: return null
            val rootConfigs = rootConfigContext.getConfigs(matchOptions)
            val configGroup = configContext.configGroup
            return ParadoxConfigHandler.getConfigsFromConfigContext(element, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
        }
        
        // infer & merge
        val inlineScriptExpression = configContext.inlineScriptExpression ?: return null
        var configs: List<CwtMemberConfig<*>>? = null
        configContext.inlineScriptHasConflict = false
        configContext.inlineScriptHasRecursion = false
        withRecursionGuard("icu.windea.pls.lang.config.impl.ParadoxInlineScriptConfigContextProvider.getConfigs") {
            withRecursionGuard(inlineScriptExpression) {
                val project = configContext.configGroup.project
                val selector = inlineScriptSelector(project, configContext.element)
                ParadoxInlineScriptSearch.search(inlineScriptExpression, selector).processQueryAsync p@{ info ->
                    ProgressManager.checkCanceled()
                    val e = info.file?.findElementAt(info.elementOffset) ?: return@p true
                    val p = e.parentOfType<ParadoxScriptProperty>() ?: return@p true
                    if(p.name.lowercase() != ParadoxInlineScriptHandler.inlineScriptKey) return@p true
                    val memberElement = p.parentOfType<ParadoxScriptMemberElement>() ?: return@p true
                    val usageConfigContext = ParadoxConfigHandler.getConfigContext(memberElement) ?: return@p true
                    val usageConfigs = usageConfigContext.getConfigs(matchOptions)
                    if(configs == null) {
                        configs = usageConfigs
                    } else {
                        if(usageConfigs.isEmpty()) {
                            if(configs!!.isEmpty()) {
                                return@p true
                            } else {
                                configContext.inlineScriptHasConflict = true
                                return@p false
                            }
                        }
                        // merge
                        val mergedConfigs = ParadoxConfigMergeHandler.mergeConfigs(configs!!, usageConfigs)
                        if(mergedConfigs.isEmpty()) {
                            configContext.inlineScriptHasConflict = true
                            return@p false
                        } else {
                            configs = mergedConfigs
                        }
                    }
                    true
                }
            } ?: run {
                configContext.inlineScriptHasRecursion = true
                configs = null
            }
        }
        
        return configs
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