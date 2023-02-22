package icu.windea.pls.lang.support

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

class ParadoxDefinitionParameterSupport : ParadoxParameterSupport {
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
        val configExpression = config.expression
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
    
    override fun processContextFromInvocationExpression(element: ParadoxScriptProperty, config: CwtPropertyConfig, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val configExpression = config.expression
        if(configExpression.type != CwtDataType.Definition) return false
        val definitionType = configExpression.value ?: return false
        val definitionName = element.name
        val configGroup = config.info.configGroup
        val project = configGroup.project
        val selector = definitionSelector(project, element).preferSameRoot()
        ParadoxDefinitionSearch.search(definitionName, definitionType, selector).processQuery(processor)
        return true
    }
    
    override fun buildDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean = with(builder) {
        val definitionName = element.getUserData(definitionNameKey) ?: return false
        val definitionType = element.getUserData(definitionTypesKey) ?: return false
        if(definitionType.isEmpty()) return false
        
        //不加上文件信息
        
        //加上名字
        val name = element.name
        append(PlsDocBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        
        //加上所属定义信息
        val gameType = element.gameType
        appendBr().appendIndent()
        append(PlsDocBundle.message("ofDefinition")).append(" ")
        appendDefinitionLink(gameType, definitionName, definitionType.first(), element)
        append(": ")
        
        val type = definitionType.first()
        val typeLink = "${gameType.id}/types/${type}"
        appendCwtLink(type, typeLink)
        for((index, t) in definitionType.withIndex()) {
            if(index == 0) continue
            append(", ")
            val subtypeLink = "$typeLink/${t}"
            appendCwtLink(t, subtypeLink)
        }
        return true
    }
}

