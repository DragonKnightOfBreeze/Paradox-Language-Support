package icu.windea.pls.lang.parameter.impl

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.model.*
import icu.windea.pls.model.data.*
import icu.windea.pls.script.psi.*

open class ParadoxDefinitionParameterSupport : ParadoxParameterSupport {
    override fun isContext(element: ParadoxScriptDefinitionElement): Boolean {
        if(element !is ParadoxScriptProperty) return false
        val definitionInfo = element.definitionInfo ?: return false
        //NOTE 简单判断 - 目前不需要兼容子类型
        return definitionInfo.type in definitionInfo.configGroup.definitionTypesSupportParameters
    }
    
    override fun findContext(element: PsiElement): ParadoxScriptDefinitionElement? {
        //NOTE 这里需要兼容通过语言注入注入到脚本文件中的脚本片段中的参数（此时需要先获取最外面的injectionHost）
        val finalElement = element.findTopHostElementOrThis(element.project)
        val context = finalElement.findParentDefinition()
        return context?.takeIf { isContext(it) }
    }
    
    override fun getContextInfo(element: ParadoxScriptDefinitionElement): ParadoxParameterContextInfo? {
        if(!isContext(element)) return null
        return ParadoxParameterHandler.getContextInfo(element)
    }
    
    override fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        var contextConfig: CwtPropertyConfig? = null
        var contextReferenceElement: ParadoxScriptProperty? = null
        var completionOffset = -1
        when(from) {
            //extraArgs: config, completionOffset?
            ParadoxParameterContextReferenceInfo.From.Argument -> {
                val config = extraArgs.getOrNull(0)?.castOrNull<CwtMemberConfig<*>>() ?: return null
                completionOffset = extraArgs.getOrNull(1)?.castOrNull<Int>() ?: -1
                //infer context config
                contextConfig = config.castOrNull<CwtPropertyConfig>()?.parentConfig?.castOrNull<CwtPropertyConfig>() ?: return null
                if(contextConfig.expression.type != CwtDataType.Definition) return null
                contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
            }
            //extraArgs: contextConfig
            ParadoxParameterContextReferenceInfo.From.ContextReference -> {
                contextConfig = extraArgs.getOrNull(0)?.castOrNull<CwtPropertyConfig>() ?: return null
                if(contextConfig.expression.type != CwtDataType.Definition) return null
                contextReferenceElement = element.castOrNull() ?: return null
            }
            //extraArgs: offset?
            ParadoxParameterContextReferenceInfo.From.InContextReference -> {
                val parentBlock = when(element.elementType) {
                    ParadoxScriptElementTypes.LEFT_BRACE -> element.parent.parentOfType<ParadoxScriptBlock>()
                    else -> element.parentOfType<ParadoxScriptBlock>()
                } ?: return null
                val parentProperties = parentBlock.parentsOfType<ParadoxScriptProperty>(withSelf = false)
                for(prop in parentProperties) {
                    //infer context config
                    val propConfig = CwtConfigHandler.getConfigs(prop).firstOrNull() as? CwtPropertyConfig ?: continue
                    if(propConfig.expression.type != CwtDataType.Definition) continue
                    if(propConfig.configs?.any { it is CwtPropertyConfig && it.expression.type == CwtDataType.Parameter } != true) continue
                    contextConfig = propConfig
                    contextReferenceElement = prop
                    break
                }
            }
        }
        if(contextConfig == null || contextReferenceElement == null) return null
        val configGroup = contextConfig.info.configGroup
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val definitionName = contextReferenceElement.name.orNull() ?: return null
        if(definitionName.isParameterized()) return null //skip if context name is parameterized
        val definitionTypes = contextConfig.expression.value?.split('.') ?: return null
        val contextName = definitionName
        val contextNameElement = contextReferenceElement.propertyKey
        val arguments = mutableListOf<ParadoxParameterContextReferenceInfo.Argument>()
        contextReferenceElement.block?.processProperty p@{
            if(completionOffset != -1 && completionOffset in it.textRange) return@p true
            val k = it.propertyKey
            val v = it.propertyValue
            val argumentName = k.name
            arguments += ParadoxParameterContextReferenceInfo.Argument(argumentName, k.createPointer(project), k.textRange, v?.createPointer(project), v?.textRange)
            true
        }
        val info = ParadoxParameterContextReferenceInfo(contextReferenceElement.createPointer(project), contextName, contextNameElement.createPointer(project), contextNameElement.textRange, arguments, gameType, project)
        info.definitionName = definitionName
        info.definitionTypes = definitionTypes
        return info
    }
    
    override fun resolveParameter(element: ParadoxParameter): ParadoxParameterElement? {
        val name = element.name ?: return null
        return doResolveParameter(element, name)
    }
    
    override fun resolveConditionParameter(element: ParadoxConditionParameter): ParadoxParameterElement? {
        val name = element.name ?: return null
        return doResolveParameter(element, name)
    }
    
    private fun doResolveParameter(element: PsiElement, name: String): ParadoxParameterElement? {
        val context = findContext(element) ?: return null
        val definitionInfo = context.definitionInfo ?: return null
        val definitionName = context.name
        val definitionTypes = definitionInfo.types
        val contextName = definitionName
        val contextIcon = PlsIcons.Definition
        val contextKey = "definition@$definitionName: ${definitionTypes.joinToString(",")}"
        val rangeInParent = TextRange.create(0, element.textLength)
        val readWriteAccess = ParadoxParameterHandler.getReadWriteAccess(element)
        val gameType = definitionInfo.gameType
        val project = definitionInfo.project
        val result = ParadoxParameterElement(element, name, contextName, contextIcon, contextKey, rangeInParent, readWriteAccess, gameType, project)
        result.containingContext = context.createPointer(project)
        result.definitionName = definitionName
        result.definitionTypes = definitionTypes
        result.support = this
        return result
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement? {
        if(config !is CwtPropertyConfig || config.expression.type != CwtDataType.Parameter) return null
        return doResolveArgument(element, rangeInElement, config)
    }
    
    private fun doResolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtPropertyConfig): ParadoxParameterElement? {
        val contextConfig = config.castOrNull<CwtPropertyConfig>()?.parentConfig?.castOrNull<CwtPropertyConfig>() ?: return null
        if(contextConfig.expression.type != CwtDataType.Definition) return null
        val contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
        val definitionName = contextReferenceElement.name.orNull() ?: return null
        if(definitionName.isParameterized()) return null //skip if context name is parameterized
        val definitionTypes = contextConfig.expression.value?.split('.') ?: return null
        val name = element.name
        val contextName = definitionName
        val contextIcon = PlsIcons.Definition
        val contextKey = "definition@$definitionName: ${definitionTypes.joinToString(",")}"
        val rangeInParent = rangeInElement ?: TextRange.create(0, element.textLength)
        val readWriteAccess = ParadoxParameterHandler.getReadWriteAccess(element)
        val gameType = config.info.configGroup.gameType ?: return null
        val project = config.info.configGroup.project
        val result = ParadoxParameterElement(element, name, contextName, contextIcon, contextKey, rangeInParent, readWriteAccess, gameType, project)
        result.definitionName = definitionName
        result.definitionTypes = definitionTypes
        result.support = this
        return result
    }
    
    override fun processContext(parameterElement: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val definitionName = parameterElement.definitionName ?: return false
        val definitionTypes = parameterElement.definitionTypes ?: return false
        if(definitionName.isParameterized()) return false //skip if context name is parameterized
        val definitionType = definitionTypes.joinToString(".")
        val project = parameterElement.project
        val selector = definitionSelector(project, parameterElement).contextSensitive()
        ParadoxDefinitionSearch.search(definitionName, definitionType, selector).processQueryAsync(onlyMostRelevant, processor)
        return true
    }
    
    override fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val definitionName = contextReferenceInfo.definitionName ?: return false
        val definitionTypes = contextReferenceInfo.definitionTypes ?: return false
        if(definitionName.isParameterized()) return false //skip if context name is parameterized
        val definitionType = definitionTypes.joinToString(".")
        val project = contextReferenceInfo.project
        val selector = definitionSelector(project, element).contextSensitive()
        ParadoxDefinitionSearch.search(definitionName, definitionType, selector).processQueryAsync(onlyMostRelevant, processor)
        return true
    }
    
    override fun getModificationTracker(parameterData: ParadoxParameterData): ModificationTracker? {
        val project = parameterData.project
        val configGroup = getConfigGroups(project).get(parameterData.gameType)
        return configGroup.getOrPutUserData(CwtConfigGroup.Keys.parameterModificationTracker) {
            val definitionTypes = configGroup.definitionTypesSupportParameters
            val builder = StringBuilder()
            var isFirst = true
            for(definitionType in definitionTypes) {
                val typeConfig = configGroup.types.get(definitionType) ?: continue
                val filePath = typeConfig.pathFile ?: typeConfig.path ?: continue
                val fileExtension = typeConfig.pathExtension
                if(isFirst) isFirst = false else builder.append('|')
                builder.append(filePath)
                if(fileExtension != null) builder.append(':').append(fileExtension)
            }
            ParadoxPsiModificationTracker.getInstance(project).ScriptFileTracker(builder.toString())
        }
    }
    
    override fun buildDocumentationDefinition(parameterElement: ParadoxParameterElement, builder: StringBuilder): Boolean = with(builder) {
        val definitionName = parameterElement.definitionName ?: return false
        val definitionType = parameterElement.definitionTypes ?: return false
        if(definitionType.isEmpty()) return false
        
        //不加上文件信息
        
        //加上名字
        val name = parameterElement.name
        append(PlsBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        //加上推断得到的规则信息
        val inferredConfig = ParadoxParameterHandler.getInferredConfig(parameterElement)
        if(inferredConfig != null) {
            append(": ")
            append(inferredConfig.expression.expressionString.escapeXml())
        }
        //加上所属定义信息
        val gameType = parameterElement.gameType
        appendBr().appendIndent()
        append(PlsBundle.message("ofDefinition")).append(" ")
        appendDefinitionLink(gameType, definitionName, definitionType.first(), parameterElement)
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
