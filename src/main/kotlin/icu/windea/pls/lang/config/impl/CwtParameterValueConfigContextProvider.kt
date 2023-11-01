package icu.windea.pls.lang.config.impl

import com.intellij.lang.injection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.model.*
import icu.windea.pls.script.injection.*
import icu.windea.pls.script.psi.*

/**
 * 用于获取脚本参数的传入值和默认值中的CWT规则上下文。
 *
 * * 基于语言注入功能实现。
 * * 正常提供代码高亮、引用解析、代码补全等高级语言功能。
 * * 对于顶级成员，禁用以下代码检查：`MissingExpressionInspection`和`TooManyExpressionInspection`。
 * * 不会将参数值内容内联到对应的调用处，然后再进行相关代码检查。
 * * 不会将参数值内容内联到对应的调用处，然后检查语法是否合法。
 *
 * @see ParadoxScriptLanguageInjector
 */
class CwtParameterValueConfigContextProvider : CwtConfigContextProvider {
    override fun getContext(element: ParadoxScriptMemberElement, elementPath: ParadoxElementPath, file: PsiFile): CwtConfigContext? {
        if(!getSettings().inference.parameterConfig) return null
        
        //unnecessary check
        //val vFile = selectFile(file) ?: return null
        //if(ParadoxFileManager.isInjectedFile(vFile)) return null //ignored for injected psi
        
        val host = InjectedLanguageManager.getInstance(file.project).getInjectionHost(file)
        if(host == null) return null
        val parameterElement = getParameterElement(file, host)
        if(parameterElement == null) return null
        
        ProgressManager.checkCanceled()
        val gameType = parameterElement.gameType
        val elementPathFromRoot = elementPath
        val configGroup = getConfigGroups(file.project).get(gameType)
        val configContext = CwtConfigContext(element, null, elementPath, gameType, configGroup)
        if(elementPathFromRoot.isNotEmpty()) {
            configContext.snippetFromParameterValueRootConfigContext = CwtConfigHandler.getConfigContext(file) ?: return null
        }
        configContext.elementPathFromRoot = elementPathFromRoot
        configContext.parameterElement = parameterElement
        return configContext
    }
    
    private fun getParameterElement(file: PsiFile, host: PsiElement): ParadoxParameterElement? {
        val injectionInfos = host.getUserData(ParadoxScriptLanguageInjector.Keys.parameterValueInjectionInfos)
        if(injectionInfos.isNullOrEmpty()) return null
        return when {
            host is ParadoxScriptStringExpressionElement -> {
                val shreds = file.getShreds()
                val shred = shreds?.singleOrNull()
                val rangeInsideHost = shred?.rangeInsideHost ?: return null
                val injectionInfo = injectionInfos.find { it.rangeInsideHost == rangeInsideHost } ?: return null
                injectionInfo.parameterElement
            }
            host is ParadoxParameter -> {
                //just use the only one
                val injectionInfo = injectionInfos.singleOrNull() ?: return null
                injectionInfo.parameterElement
            }
            else -> null
        }
    }
    
    override fun getCacheKey(context: CwtConfigContext, matchOptions: Int): String? {
        val gameTypeId = context.gameType.id
        val parameterElement = context.parameterElement ?: return null // null -> unexpected
        val elementPathFromRoot = context.elementPathFromRoot ?: return null // null -> unexpected
        val isPropertyValue = context.element is ParadoxScriptValue && context.element.isPropertyValue()
        return "is@$gameTypeId:${matchOptions}#${isPropertyValue.toInt()}#${parameterElement.contextKey}@${parameterElement.name}\n${elementPathFromRoot.path}"
    }
    
    override fun getConfigs(context: CwtConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        ProgressManager.checkCanceled()
        val elementPathFromRoot = context.elementPathFromRoot ?: return null
        
        if(elementPathFromRoot.isNotEmpty()) {
            val rootConfigContext = context.snippetFromParameterValueRootConfigContext ?: return null
            val element = context.element
            val rootConfigs = rootConfigContext.getConfigs(matchOptions)
            val configGroup = context.configGroup
            return CwtConfigHandler.getConfigsForConfigContext(element, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
        }
        
        val parameterElement = context.parameterElement ?: return null
        
        return ParadoxParameterHandler.getInferredContextConfigs(parameterElement)
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

val CwtConfigContext.Keys.snippetFromParameterValueRootConfigContext by createKey<CwtConfigContext>("paradox.configContext.snippetFromParameterValue.rootConfigContext")
val CwtConfigContext.Keys.parameterElement by createKey<ParadoxParameterElement>("paradox.configContext.snippetFromParameterValue.parameterElement")

var CwtConfigContext.snippetFromParameterValueRootConfigContext by CwtConfigContext.Keys.snippetFromParameterValueRootConfigContext
var CwtConfigContext.parameterElement by CwtConfigContext.Keys.parameterElement