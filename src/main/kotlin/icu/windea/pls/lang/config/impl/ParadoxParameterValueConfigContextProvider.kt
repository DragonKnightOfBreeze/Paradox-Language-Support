package icu.windea.pls.lang.config.impl

import com.intellij.lang.injection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageEditorUtil
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtilBase
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*
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
 * @see ParadoxScriptInjector
 */
class ParadoxParameterValueConfigContextProvider : ParadoxConfigContextProvider {
    override fun getConfigContext(element: ParadoxScriptMemberElement, elementPath: ParadoxElementPath, file: PsiFile): ParadoxConfigContext? {
        if(!getSettings().inference.parameterConfig) return null
        
        val vFile = selectFile(file) ?: return null
        if(!ParadoxFileManager.isInjectedFile(vFile)) return null //limited for injected psi
        
        val injectionHost = InjectedLanguageManager.getInstance(file.project).getInjectionHost(file)
        if(injectionHost !is ParadoxScriptString) return null
        
        val argumentNameElement = injectionHost.propertyKey ?: return null
        val argumentNameConfig = ParadoxConfigHandler.getConfigs(argumentNameElement).firstOrNull() ?: return null
        if(argumentNameConfig.expression.type != CwtDataType.Parameter) return null
        val parameterElement = ParadoxParameterSupport.resolveArgument(argumentNameElement, null, argumentNameConfig) ?: return null
        
        ProgressManager.checkCanceled()
        val gameType = parameterElement.gameType
        val elementPathFromRoot = elementPath
        val configGroup = getCwtConfig(file.project).get(gameType)
        val configContext = ParadoxConfigContext(null, elementPath, gameType, configGroup, element)
        if(elementPathFromRoot.isNotEmpty()) {
            configContext.snippetFromParameterValueRootConfigContext = ParadoxConfigHandler.getConfigContext(file) ?: return null
        }
        configContext.elementPathFromRoot = elementPathFromRoot
        configContext.parameterElement = parameterElement
        return configContext
    }
    
    override fun getConfigs(element: ParadoxScriptMemberElement, configContext: ParadoxConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        val elementPathFromRoot = configContext.elementPathFromRoot ?: return null
        
        if(elementPathFromRoot.isNotEmpty()) {
            val rootConfigContext = configContext.snippetFromParameterValueRootConfigContext ?: return null
            val rootConfigs = rootConfigContext.getConfigs(matchOptions)
            val configGroup = configContext.configGroup
                return ParadoxConfigHandler.getConfigsFromConfigContext(element, rootConfigs, elementPathFromRoot, configGroup, matchOptions)
        }
        
        val parameterElement = configContext.parameterElement ?: return null
        
        //unsupported -> return null
        val inferredContextConfigs = ParadoxParameterHandler.getInferredContextConfigs(parameterElement)
        if(inferredContextConfigs.singleOrNull() == CwtValueConfig.EmptyConfig) return null
        return inferredContextConfigs
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

val ParadoxConfigContext.Keys.snippetFromParameterValueRootConfigContext by lazy { Key.create<ParadoxConfigContext>("paradox.configContext.snippetFromParameterValue.rootConfigContext") }
val ParadoxConfigContext.Keys.parameterElement by lazy { Key.create<ParadoxParameterElement>("paradox.configContext.snippetFromParameterValue.parameterElement") }

var ParadoxConfigContext.snippetFromParameterValueRootConfigContext by ParadoxConfigContext.Keys.snippetFromParameterValueRootConfigContext
var ParadoxConfigContext.parameterElement by ParadoxConfigContext.Keys.parameterElement