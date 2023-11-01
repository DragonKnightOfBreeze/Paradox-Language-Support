package icu.windea.pls.lang.config.impl

import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于获取内联脚本中的CWT规则上下文。
 *
 * * 正常提供代码高亮、引用解析、代码补全等高级语言功能。
 * * 对于顶级成员，禁用以下代码检查：`MissingExpressionInspection`和`TooManyExpressionInspection`。
 * * 会将内联脚本内容内联到对应的调用处，然后再进行相关代码检查。
 */
class CwtInlineScriptConfigContextProvider : CwtConfigContextProvider {
    //注意：内联脚本调用可以在定义声明之外
    
    //TODO 1.1.0+ 支持解析内联脚本文件中的定义声明
    
    //首先推断内联脚本文件的CWT规则上下文：汇总内联脚本调用处的上下文，然后合并得到最终的CWT规则上下文
    //然后再得到当前位置的CWT规则上下文
    
    override fun getContext(element: ParadoxScriptMemberElement, elementPath: ParadoxElementPath, file: PsiFile): CwtConfigContext? {
        if(!getSettings().inference.inlineScriptConfig) return null
        
        val vFile = selectFile(file) ?: return null
        if(ParadoxFileManager.isInjectedFile(vFile)) return null //ignored for injected psi
        
        val inlineScriptExpression = ParadoxInlineScriptHandler.getInlineScriptExpression(vFile)
        if(inlineScriptExpression == null) return null
        
        ProgressManager.checkCanceled()
        val fileInfo = vFile.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val elementPathFromRoot = elementPath
        val configGroup = getConfigGroups(file.project).get(gameType)
        val configContext = CwtConfigContext(element, fileInfo, elementPath, gameType, configGroup)
        if(elementPathFromRoot.isNotEmpty()) {
            configContext.inlineScriptRootConfigContext = CwtConfigHandler.getConfigContext(file) ?: return null
        }
        configContext.inlineScriptExpression = inlineScriptExpression
        configContext.elementPathFromRoot = elementPathFromRoot
        return configContext
    }
    
    override fun getCacheKey(context: CwtConfigContext, matchOptions: Int): String? {
        val gameTypeId = context.gameType.id
        val inlineScriptExpression = context.inlineScriptExpression ?: return null // null -> unexpected
        val elementPathFromRoot = context.elementPathFromRoot ?: return null // null -> unexpected
        val isPropertyValue = context.element is ParadoxScriptValue && context.element.isPropertyValue()
        return "is@$gameTypeId:${matchOptions}#${isPropertyValue.toInt()}#${inlineScriptExpression}\n${elementPathFromRoot.path}"
    }
    
    //获取CWT规则后才能确定是否存在冲突以及是否存在递归
    
    override fun getConfigs(context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()
        val elementPathFromRoot = context.elementPathFromRoot ?: return null
        
        if(elementPathFromRoot.isNotEmpty()) {
            val rootConfigContext = context.inlineScriptRootConfigContext ?: return null
            val element = context.element
            val rootConfigs = rootConfigContext.getConfigs(matchOptions)
            val configGroup = context.configGroup
            return CwtConfigHandler.getConfigsForConfigContext(element, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
        }
        
        val inlineScriptExpression = context.inlineScriptExpression ?: return null
        
        // infer & merge
        val result = Ref.create<List<CwtMemberConfig<*>>>()
        context.inlineScriptHasConflict = false
        context.inlineScriptHasRecursion = false
        withRecursionGuard("icu.windea.pls.lang.config.impl.CwtInlineScriptConfigContextProvider.getConfigsForConfigContext") {
            withCheckRecursion(inlineScriptExpression) {
                val project = context.configGroup.project
                val selector = inlineScriptSelector(project, context.element)
                ParadoxInlineScriptUsageSearch.search(inlineScriptExpression, selector).processQueryAsync p@{ info ->
                    ProgressManager.checkCanceled()
                    val file = info.virtualFile?.toPsiFile(project) ?: return@p true
                    val e = file.findElementAt(info.elementOffset) ?: return@p true
                    val p = e.parentOfType<ParadoxScriptProperty>() ?: return@p true
                    if(!p.name.equals(ParadoxInlineScriptHandler.inlineScriptKey, true)) return@p true
                    val memberElement = p.parentOfType<ParadoxScriptMemberElement>() ?: return@p true
                    val usageConfigContext = CwtConfigHandler.getConfigContext(memberElement) ?: return@p true
                    val usageConfigs = usageConfigContext.getConfigs(matchOptions).orNull()
                    // merge
                    result.mergeValue(usageConfigs) { v1, v2 -> CwtConfigManipulator.mergeConfigs(v1, v2) }.also {
                        if(it) return@also
                        context.inlineScriptHasConflict = true
                        result.set(null)
                    }
                }
            } ?: run {
                context.inlineScriptHasRecursion = true
                result.set(null)
            }
        }
        return result.get()
    }
    
    //skip MissingExpressionInspection and TooManyExpressionInspection at root level
    
    override fun skipMissingExpressionCheck(context: CwtConfigContext): Boolean {
        val elementPathFromRoot = context.elementPathFromRoot ?: return false
        return elementPathFromRoot.isEmpty()
    }
    
    override fun skipTooManyExpressionCheck(context: CwtConfigContext): Boolean {
        val elementPathFromRoot = context.elementPathFromRoot ?: return false
        return elementPathFromRoot.isEmpty()
    }
}

val CwtConfigContext.Keys.inlineScriptRootConfigContext by createKey<CwtConfigContext>("paradox.configContext.inlineScript.rootConfigContext")
val CwtConfigContext.Keys.inlineScriptExpression by createKey<String>("paradox.configContext.inlineScript.expression")
val CwtConfigContext.Keys.inlineScriptHasConflict by createKey<Boolean>("paradox.configContext.inlineScript.hasConflict")
val CwtConfigContext.Keys.inlineScriptHasRecursion by createKey<Boolean>("paradox.configContext.inlineScript.hasRecursion")

var CwtConfigContext.inlineScriptRootConfigContext by CwtConfigContext.Keys.inlineScriptRootConfigContext
var CwtConfigContext.inlineScriptExpression by CwtConfigContext.Keys.inlineScriptExpression
var CwtConfigContext.inlineScriptHasConflict by CwtConfigContext.Keys.inlineScriptHasConflict
var CwtConfigContext.inlineScriptHasRecursion by CwtConfigContext.Keys.inlineScriptHasRecursion