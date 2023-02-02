package icu.windea.pls.config.core.component

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

class ParadoxDefinitionParameterResolver: ParadoxParameterResolver {
    companion object {
        @JvmField val definitionNameKey = Key.create<String>("paradox.parameterElement.definitionName")
        @JvmField val definitionTypesKey = Key.create<List<String>>("paradox.parameterElement.definitionTypes")
    }
    
    override fun supports(context: ParadoxScriptDefinitionElement): Boolean {
        if(context !is ParadoxScriptProperty) return false
        val definitionInfo = context.definitionInfo ?: return false
        val configGroup = definitionInfo.configGroup
        //NOTE 简单判断 - 目前不需要兼容子类型
        return definitionInfo.type in configGroup.definitionTypesSupportParameters
    }
    
    override fun findContext(element: PsiElement, file: PsiFile?): ParadoxScriptDefinitionElement? {
        if(element !is ParadoxParameter && element !is ParadoxArgument) return null
        return element.findParentDefinition()?.takeIf { supports(it) }
    }
    
     override fun resolveParameter(name: String, element: PsiElement, context: ParadoxScriptDefinitionElement): ParadoxParameterElement? {
        val definitionInfo = context.definitionInfo ?: return null
        val definitionName = context.name
        val definitionTypes = definitionInfo.types
        val readWriteAccess = getReadWriteAccess(element)
        val contextKey = "definition@$definitionName: ${definitionTypes.joinToString(",")}"
        val gameType = definitionInfo.gameType
        val project = definitionInfo.project
        val result = ParadoxParameterElement(element, name, definitionName, contextKey, readWriteAccess, gameType, project)
        result.putUserData(definitionNameKey, definitionName)
        result.putUserData(definitionTypesKey, definitionTypes)
        return result
    }
    
    override fun resolveParameterFromInvocationExpression(name: String, element: ParadoxScriptProperty, config: CwtPropertyConfig): ParadoxParameterElement? {
        val configExpression = config.resolved().expression
        if(configExpression.type != CwtDataType.Definition) return null
        val definitionTypes = configExpression.value?.split('.') ?: return null
        val definitionName = element.name
        val readWriteAccess = ReadWriteAccessDetector.Access.Write
        val contextKey = "definition@$definitionName: ${definitionTypes.joinToString(",")}"
        val gameType = config.info.configGroup.gameType ?: return null
        val project = config.info.configGroup.project
        val result = ParadoxParameterElement(element, name, definitionName, contextKey, readWriteAccess, gameType, project)
        result.putUserData(definitionNameKey, definitionName)
        result.putUserData(definitionTypesKey, definitionTypes)
        return result
    }
    
    private fun getReadWriteAccess(element: PsiElement) = when {
        element is ParadoxParameter -> ReadWriteAccessDetector.Access.Read
        element is ParadoxArgument -> ReadWriteAccessDetector.Access.Write
        else -> ReadWriteAccessDetector.Access.ReadWrite
    }
}

