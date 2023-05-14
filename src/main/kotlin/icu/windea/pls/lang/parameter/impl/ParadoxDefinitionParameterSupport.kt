package icu.windea.pls.lang.parameter.impl

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.psi.*

open class ParadoxDefinitionParameterSupport : ParadoxParameterSupport {
    companion object {
        @JvmField val containingContextKey = Key.create<SmartPsiElementPointer<ParadoxScriptDefinitionElement>>("paradox.parameterElement.containingContext")
        @JvmField val containingContextReferenceKey = Key.create<SmartPsiElementPointer<ParadoxScriptDefinitionElement>>("paradox.parameterElement.contextReference")
        @JvmField val definitionNameKey = Key.create<String>("paradox.parameterElement.definitionName")
        @JvmField val definitionTypesKey = Key.create<List<String>>("paradox.parameterElement.definitionTypes")
        @JvmField val modificationTrackerKey = Key.create<ModificationTracker>("paradox.definition.parameter.modificationTracker")
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
    
    override fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
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
                contextConfig = extraArgs.getOrNull(0)?.castOrNull<CwtPropertyConfig>() ?: return null
                if(contextConfig.expression.type != CwtDataType.Definition) return null
                contextReferenceElement = element.castOrNull() ?: return null
            }
            //extraArgs: offset
            ParadoxParameterContextReferenceInfo.From.InContextReference -> {
                val parentBlock = when(element.elementType) {
                    ParadoxScriptElementTypes.LEFT_BRACE -> element.parent.parentOfType<ParadoxScriptBlock>()
                    else -> element.parentOfType<ParadoxScriptBlock>()
                } ?: return null
                val parentProperties = parentBlock.parentsOfType<ParadoxScriptProperty>(withSelf = false)
                for(prop in parentProperties) {
                    //infer context config
                    val propConfig = ParadoxConfigHandler.getPropertyConfigs(prop).firstOrNull() ?: continue
                    if(propConfig.expression.type != CwtDataType.Definition) continue
                    if(propConfig.configs?.any { it is CwtPropertyConfig && it.expression.type == CwtDataType.Parameter } != true) continue
                    contextConfig = propConfig
                    contextReferenceElement = prop
                    break
                }
            }
        }
        if(contextConfig == null || contextReferenceElement == null) return null
        val rangeInElement = contextReferenceElement.propertyKey.textRangeInParent
        val definitionName = contextReferenceElement.name.takeIfNotEmpty() ?: return null
        if(definitionName.isParameterized()) return null //skip if context name is parameterized
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
        result.putUserData(containingContextKey, context.createPointer())
        result.putUserData(definitionNameKey, definitionName)
        result.putUserData(definitionTypesKey, definitionTypes)
        result.putUserData(ParadoxParameterHandler.supportKey, this)
        return result
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, vararg extraArgs: Any?): ParadoxParameterElement? {
        //extraArgs: config
        val config = extraArgs.getOrNull(0)?.castOrNull<CwtDataConfig<*>>() ?: return null
        if(config !is CwtPropertyConfig || config.expression.type != CwtDataType.Parameter) return null
        //infer context config
        val contextConfig = config.castOrNull<CwtPropertyConfig>()?.parent?.castOrNull<CwtPropertyConfig>() ?: return null
        if(contextConfig.expression.type != CwtDataType.Definition) return null
        val contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
        val definitionName = contextReferenceElement.name.takeIfNotEmpty() ?: return null
        if(definitionName.isParameterized()) return null //skip if context name is parameterized
        val definitionTypes = contextConfig.expression.value?.split('.') ?: return null
        val name = element.name
        val readWriteAccess = getReadWriteAccess(element)
        val contextKey = "definition@$definitionName: ${definitionTypes.joinToString(",")}"
        val gameType = config.info.configGroup.gameType ?: return null
        val project = config.info.configGroup.project
        val result = ParadoxParameterElement(element, name, definitionName, contextKey, readWriteAccess, gameType, project)
        result.putUserData(definitionNameKey, definitionName)
        result.putUserData(definitionTypesKey, definitionTypes)
        result.putUserData(ParadoxParameterHandler.supportKey, this)
        return result
    }
    
    private fun getReadWriteAccess(element: PsiElement) = when {
        element is ParadoxParameter -> ReadWriteAccessDetector.Access.Read
        element is ParadoxConditionParameter -> ReadWriteAccessDetector.Access.Read
        else -> ReadWriteAccessDetector.Access.Write
    }
    
    override fun getContainingContextReference(element: ParadoxParameterElement): ParadoxScriptDefinitionElement? {
        return element.getUserData(containingContextReferenceKey)?.element
    }
    
    override fun getContainingContext(element: ParadoxParameterElement): ParadoxScriptDefinitionElement? {
        return element.getUserData(containingContextKey)?.element
    }
    
    override fun processContext(element: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val definitionName = element.getUserData(definitionNameKey) ?: return false
        val definitionTypes = element.getUserData(definitionTypesKey) ?: return false
        if(definitionName.isParameterized()) return false //skip if context name is parameterized
        val definitionType = definitionTypes.joinToString(".")
        val project = element.project
        val selector = definitionSelector(project, element).contextSensitive()
        ParadoxDefinitionSearch.search(definitionName, definitionType, selector).processQueryAsync(onlyMostRelevant, processor)
        return true
    }
    
    override fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val definitionName = contextReferenceInfo.getUserData(definitionNameKey) ?: return false
        val definitionTypes = contextReferenceInfo.getUserData(definitionTypesKey) ?: return false
        if(definitionName.isParameterized()) return false //skip if context name is parameterized
        val definitionType = definitionTypes.joinToString(".")
        val project = contextReferenceInfo.project
        val selector = definitionSelector(project, element).contextSensitive()
        ParadoxDefinitionSearch.search(definitionName, definitionType, selector).processQueryAsync(onlyMostRelevant, processor)
        return true
    }
    
    override fun getModificationTracker(parameterElement: ParadoxParameterElement): ModificationTracker {
        //return ParadoxModificationTrackerProvider.getInstance().ScriptFileTracker
        
        val project = parameterElement.project
        val configGroup = getCwtConfig(project).get(parameterElement.gameType)
        return configGroup.getOrPutUserData(modificationTrackerKey) {
            val definitionTypes = configGroup.definitionTypesSupportParameters
            val builder = StringBuilder()
            var isFirst = true
            for(definitionType in definitionTypes) {
                val typeConfig = configGroup.types.get(definitionType) ?: continue
                val filePath = typeConfig.pathFile ?: continue
                val fileExtension = typeConfig.pathExtension
                if(isFirst) isFirst = false else builder.append('|')
                builder.append(filePath)
                if(fileExtension != null) builder.append(':').append(fileExtension)
            }
            ParadoxModificationTrackerProvider.getInstance().ScriptFileTracker(builder.toString())
        }
    }
    
    override fun buildDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean = with(builder) {
        val definitionName = element.getUserData(definitionNameKey) ?: return false
        val definitionType = element.getUserData(definitionTypesKey) ?: return false
        if(definitionType.isEmpty()) return false
        
        //不加上文件信息
        
        //加上名字
        val name = element.name
        append(PlsBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        //加上推断得到的规则信息
        val inferredConfig = ParadoxParameterHandler.inferEntireConfig(element)
        if(inferredConfig != null) {
            append(": ")
            append(inferredConfig.expression.expressionString.escapeXml())
        }
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
