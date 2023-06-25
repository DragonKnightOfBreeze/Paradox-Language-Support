package icu.windea.pls.lang.config.impl

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
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
 * 用于获取脚本参数值中的CWT规则上下文。
 * 
 * 脚本参数值是一个引号括起的字符串，对这个字符串应用自动语言注入（注入为脚本片段），然后获取这个脚本片段中的CWT规则上下文。
 */
class ParadoxScriptSnippetFromParameterValueConfigContextProvider : ParadoxConfigContextProvider {
    override fun getConfigContext(element: ParadoxScriptMemberElement, elementPath: ParadoxElementPath, file: PsiFile): ParadoxConfigContext? {
        if(!getSettings().inference.argumentValueConfig) return null
        
        val vFile = selectFile(file) ?: return null
        if(!ParadoxFileManager.isInjectedFile(vFile)) return null //limited for injected psi
        
        val injectionHost = InjectedLanguageManager.getInstance(file.project).getInjectionHost(file)
        if(injectionHost !is ParadoxScriptString) return null
        
        val argumentNameElement = injectionHost.propertyKey ?: return null
        val argumentNameConfig = ParadoxConfigHandler.getConfigs(argumentNameElement).firstOrNull() ?: return null
        if(argumentNameConfig.expression.type != CwtDataType.Parameter) return null
        val parameterElement = ParadoxParameterSupport.resolveArgument(argumentNameElement, null, argumentNameConfig) ?: return null
        
        val fileInfo = vFile.fileInfo ?: return null
        val gameType = fileInfo.rootInfo.gameType
        val elementPathFromRoot = elementPath
        val configGroup = getCwtConfig(file.project).get(gameType)
        val configContext = ParadoxConfigContext(fileInfo, elementPath, gameType, configGroup, element)
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
        return ParadoxParameterHandler.getInferredContainingConfigs(parameterElement)
    }
}

val ParadoxConfigContext.Keys.snippetFromParameterValueRootConfigContext by lazy { Key.create<ParadoxConfigContext>("paradox.configContext.snippetFromParameterValue.rootConfigContext") }
val ParadoxConfigContext.Keys.parameterElement by lazy { Key.create<ParadoxParameterElement>("paradox.configContext.snippetFromParameterValue.parameterElement") }

var ParadoxConfigContext.snippetFromParameterValueRootConfigContext by ParadoxConfigContext.Keys.snippetFromParameterValueRootConfigContext
var ParadoxConfigContext.parameterElement by ParadoxConfigContext.Keys.parameterElement