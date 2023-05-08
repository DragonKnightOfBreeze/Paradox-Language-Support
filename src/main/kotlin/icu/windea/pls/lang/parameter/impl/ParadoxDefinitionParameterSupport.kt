package icu.windea.pls.lang.parameter.impl

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.psi.*

open class ParadoxDefinitionParameterSupport : ParadoxParameterSupport {
    companion object {
        @JvmField val containingContext = Key.create<SmartPsiElementPointer<ParadoxScriptDefinitionElement>>("paradox.parameterElement.containingContext")
        @JvmField val definitionNameKey = Key.create<String>("paradox.parameterElement.definitionName")
        @JvmField val definitionTypesKey = Key.create<List<String>>("paradox.parameterElement.definitionTypes")
    }
    
    override fun isContext(element: ParadoxScriptDefinitionElement): Boolean {
        if(element !is ParadoxScriptProperty) return false
        val definitionInfo = element.definitionInfo ?: return false
        //NOTE 简单判断 - 目前不需要兼容子类型
        return definitionInfo.type in definitionInfo.configGroup.definitionTypesSupportParameters
    }
    
    override fun findContext(element: PsiElement): ParadoxScriptDefinitionElement? {
        val context = element.findParentDefinition()
        return context?.takeIf { isContext(it) }
    }
    
    override fun findContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        var contextConfig: CwtPropertyConfig? = null
        var contextReferenceElement: ParadoxScriptProperty? = null
        var completionOffset = -1
        when(from) {
            //extraArgs: config, completionOffset
            ParadoxParameterContextReferenceInfo.From.Argument -> {
                val config = extraArgs.getOrNull(0)?.castOrNull<CwtDataConfig<*>>() ?: return null
                completionOffset = extraArgs.getOrNull(1)?.castOrNull<Int>() ?: -1
                //infer context config
                contextConfig = config.castOrNull<CwtPropertyConfig>()?.parent?.castOrNull<CwtPropertyConfig>() ?: return null
                if(contextConfig.expression.type != CwtDataType.Definition) return null
                contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
            }
            //extraArgs: contextConfig
            ParadoxParameterContextReferenceInfo.From.ContextReference -> {
                
            }
            //extraArgs: offset
            ParadoxParameterContextReferenceInfo.From.InContextReference -> {
                
            }
        }
        if(contextConfig == null || contextReferenceElement == null) return null
        val rangeInElement = contextReferenceElement.propertyKey.textRangeInParent
        val definitionName = contextReferenceElement.name
        val definitionTypes = contextConfig.expression.value?.split('.') ?: return null
        val argumentNames = mutableSetOf<String>()
        contextReferenceElement.block?.processProperty p@{
            if(completionOffset != -1 && completionOffset in it.textRange) return@p true
            val argumentName = it.propertyKey.name
            argumentNames.add(argumentName)
            true
        }
        val gameType = contextConfig.info.configGroup.gameType ?: return null
        val project = contextConfig.info.configGroup.project
        val info = ParadoxParameterContextReferenceInfo(contextReferenceElement.createPointer(), rangeInElement, definitionName, argumentNames, gameType, project)
        info.putUserData(definitionNameKey, definitionName)
        info.putUserData(definitionTypesKey, definitionTypes)
        return info
    }
    
    override fun resolveParameter(element: ParadoxParameter): ParadoxParameterElement? {
        val name = element.name ?: return null
        return doResolveParameterOrArgument(element, name)
    }
    
    override fun resolveConditionParameter(element: ParadoxConditionParameter): ParadoxParameterElement? {
        val name = element.name ?: return null
        return doResolveParameterOrArgument(element, name)
    }
    
    private fun doResolveParameterOrArgument(element: PsiElement, name: String): ParadoxParameterElement? {
        val context = findContext(element) ?: return null
        val definitionInfo = context.definitionInfo ?: return null
        val definitionName = context.name
        val definitionTypes = definitionInfo.types
        val readWriteAccess = getReadWriteAccess(element)
        val contextKey = "definition@$definitionName: ${definitionTypes.joinToString(",")}"
        val gameType = definitionInfo.gameType
        val project = definitionInfo.project
        val result = ParadoxParameterElement(element, name, definitionName, contextKey, readWriteAccess, gameType, project)
        result.putUserData(containingContext, context.createPointer())
        result.putUserData(definitionNameKey, definitionName)
        result.putUserData(definitionTypesKey, definitionTypes)
        return result
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, vararg extraArgs: Any?): ParadoxParameterElement? {
        val config = extraArgs.getOrNull(0)?.castOrNull<CwtDataConfig<*>>() ?: return null
        if(config !is CwtPropertyConfig || config.expression.type != CwtDataType.Parameter) return null
        //infer context config
        val contextConfig = config.castOrNull<CwtPropertyConfig>()?.parent?.castOrNull<CwtPropertyConfig>() ?: return null
        if(contextConfig.expression.type != CwtDataType.Definition) return null
        val contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
        val definitionName = contextReferenceElement.name
        val definitionTypes = contextConfig.expression.value?.split('.') ?: return null
        val name = element.name
        val readWriteAccess = getReadWriteAccess(element)
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
        element is ParadoxConditionParameter -> ReadWriteAccessDetector.Access.Read
        else -> ReadWriteAccessDetector.Access.Write
    }
    
    override fun getContainingContext(element: ParadoxParameterElement): ParadoxScriptDefinitionElement? {
        return element.getUserData(containingContext)?.element
    }
    
    override fun processContext(element: ParadoxParameterElement, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val definitionName = element.getUserData(definitionNameKey)?.takeIf { !it.isParameterizedExpression() } ?: return false
        val definitionTypes = element.getUserData(definitionTypesKey) ?: return false
        val definitionType = definitionTypes.joinToString(".")
        val project = element.project
        val selector = definitionSelector(project, element).contextSensitive()
        ParadoxDefinitionSearch.search(definitionName, definitionType, selector).processQueryAsync(processor)
        return true
    }
    
    override fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val definitionName = contextReferenceInfo.getUserData(definitionNameKey)?.takeIf { !it.isParameterizedExpression() } ?: return false
        val definitionTypes = contextReferenceInfo.getUserData(definitionTypesKey) ?: return false
        val definitionType = definitionTypes.joinToString(".")
        val project = contextReferenceInfo.project
        val selector = definitionSelector(project, element).contextSensitive()
        ParadoxDefinitionSearch.search(definitionName, definitionType, selector).processQueryAsync(processor)
        return true
    }
    
    override fun buildDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean = with(builder) {
        val definitionName = element.getUserData(definitionNameKey) ?: return false
        val definitionType = element.getUserData(definitionTypesKey) ?: return false
        if(definitionType.isEmpty()) return false
        
        //不加上文件信息
        
        //加上名字
        val name = element.name
        append(PlsBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        
        //加上所属定义信息
        val gameType = element.gameType
        appendBr().appendIndent()
        append(PlsBundle.message("ofDefinition")).append(" ")
        appendDefinitionLink(gameType, definitionName, definitionType.first(), element)
        append(": ")
        val type = definitionType.first()
        val typeLink = "${gameType.linkToken}types/${type}"
        appendCwtLink(typeLink, type)
        for((index, t) in definitionType.withIndex()) {
            if(index == 0) continue
            append(", ")
            val subtypeLink = "$typeLink/${t}"
            appendCwtLink(subtypeLink, t)
        }
        return true
    }
}
