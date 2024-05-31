package icu.windea.pls.ep.parameter

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.documentation.*
import icu.windea.pls.lang.documentation.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.elementInfo.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.model.expression.complex.nodes.*
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
    
    override fun getContextKeyFromContext(context: ParadoxScriptDefinitionElement): String? {
        val definitionInfo = context.definitionInfo ?: return null
        return "${definitionInfo.types.joinToString(".")}@${definitionInfo.name}"
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
                if(contextConfig.expression.type != CwtDataTypes.Definition) return null
                contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
            }
            //extraArgs: contextConfig
            ParadoxParameterContextReferenceInfo.From.ContextReference -> {
                contextConfig = extraArgs.getOrNull(0)?.castOrNull<CwtPropertyConfig>() ?: return null
                if(contextConfig.expression.type != CwtDataTypes.Definition) return null
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
                    if(propConfig.expression.type != CwtDataTypes.Definition) continue
                    if(propConfig.configs?.any { it is CwtPropertyConfig && it.expression.type == CwtDataTypes.Parameter } != true) continue
                    contextConfig = propConfig
                    contextReferenceElement = prop
                    break
                }
            }
        }
        if(contextConfig == null || contextReferenceElement == null) return null
        val configGroup = contextConfig.configGroup
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val definitionName = contextReferenceElement.name.orNull() ?: return null
        if(definitionName.isParameterized()) return null //skip if context name is parameterized
        val definitionTypes = contextConfig.expression.value?.split('.') ?: return null
        val contextName = definitionName
        val contextIcon = PlsIcons.Nodes.Definition(definitionTypes[0])
        val contextKey = "${definitionTypes.joinToString(".")}@${definitionName}"
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
        val info = ParadoxParameterContextReferenceInfo(
            contextReferenceElement.createPointer(project),
            contextName, contextIcon, contextKey,
            contextNameElement.createPointer(project), contextNameElement.textRange, arguments, gameType, project
        )
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
        val contextIcon = PlsIcons.Nodes.Definition(definitionInfo.type)
        val contextKey = "${definitionTypes.joinToString(".")}@${definitionName}"
        val rangeInParent = TextRange.create(0, element.textLength)
        val readWriteAccess = ParadoxParameterHandler.getReadWriteAccess(element)
        val gameType = definitionInfo.gameType
        val project = definitionInfo.project
        val result = ParadoxParameterElement(element, name, contextName, contextIcon, contextKey, rangeInParent, readWriteAccess, gameType, project)
        result.containingContext = context.createPointer(project)
        result.definitionName = definitionName
        result.definitionTypes = definitionTypes
        return result
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement? {
        if(config !is CwtPropertyConfig || config.expression.type != CwtDataTypes.Parameter) return null
        return doResolveArgument(element, rangeInElement, config)
    }
    
    private fun doResolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtPropertyConfig): ParadoxParameterElement? {
        val contextConfig = config.castOrNull<CwtPropertyConfig>()?.parentConfig?.castOrNull<CwtPropertyConfig>() ?: return null
        if(contextConfig.expression.type != CwtDataTypes.Definition) return null
        val contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
        val definitionName = contextReferenceElement.name.orNull() ?: return null
        if(definitionName.isParameterized()) return null //skip if context name is parameterized
        val definitionTypes = contextConfig.expression.value?.split('.') ?: return null
        val name = element.name
        val contextName = definitionName
        val contextIcon = PlsIcons.Nodes.Definition(definitionTypes[0])
        val contextKey = "${definitionTypes.joinToString(".")}@${definitionName}"
        val rangeInParent = rangeInElement ?: TextRange.create(0, element.textLength)
        val readWriteAccess = ParadoxParameterHandler.getReadWriteAccess(element)
        val gameType = config.configGroup.gameType ?: return null
        val project = config.configGroup.project
        val result = ParadoxParameterElement(element, name, contextName, contextIcon, contextKey, rangeInParent, readWriteAccess, gameType, project)
        result.definitionName = definitionName
        result.definitionTypes = definitionTypes
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
    
    override fun getModificationTracker(parameterInfo: ParadoxParameterInfo): ModificationTracker? {
        val configGroup = getConfigGroup(parameterInfo.project, parameterInfo.gameType)
        return configGroup.definitionParameterModificationTracker
    }
    
    override fun buildDocumentationDefinition(parameterElement: ParadoxParameterElement, builder: DocumentationBuilder): Boolean = with(builder) {
        val definitionName = parameterElement.definitionName ?: return false
        val definitionType = parameterElement.definitionTypes ?: return false
        if(definitionType.isEmpty()) return false
        
        //不加上文件信息
        
        //加上名字
        val name = parameterElement.name
        append(PlsBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        //加上推断得到的类型信息
        val inferredType = ParadoxParameterHandler.getInferredType(parameterElement)
        if(inferredType != null) {
            append(": ").append(inferredType.escapeXml())
        }
        //加上所属定义信息
        val gameType = parameterElement.gameType
        appendBr().appendIndent()
        append(PlsBundle.message("ofDefinition")).append(" ")
        appendDefinitionLink(gameType, definitionName, definitionType.first(), parameterElement)
        append(": ")
        val type = definitionType.first()
        val typeLink = "${gameType.prefix}types/${type}"
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

/**
 * @see icu.windea.pls.model.expression.complex.ParadoxScriptValueExpression
 * @see icu.windea.pls.model.expression.complex.nodes.ParadoxScriptValueArgumentNode
 * @see icu.windea.pls.model.expression.complex.nodes.ParadoxScriptValueArgumentValueNode
 */
class ParadoxScriptValueInlineParameterSupport : ParadoxParameterSupport {
    override fun isContext(element: ParadoxScriptDefinitionElement) = false
    
    override fun findContext(element: PsiElement) = null
    
    override fun getContextKeyFromContext(context: ParadoxScriptDefinitionElement) = null
    
    override fun resolveParameter(element: ParadoxParameter) = null
    
    override fun resolveConditionParameter(element: ParadoxConditionParameter) = null
    
    override fun getContextInfo(element: ParadoxScriptDefinitionElement) = null
    
    override fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        var expressionElement: ParadoxScriptStringExpressionElement? = null
        var text: String? = null
        var expressionElementConfig: CwtMemberConfig<*>? = null
        var completionOffset = -1
        when(from) {
            //extraArgs: config, completionOffset?
            ParadoxParameterContextReferenceInfo.From.Argument -> {
                val config = extraArgs.getOrNull(0)?.castOrNull<CwtMemberConfig<*>>() ?: return null
                completionOffset = extraArgs.getOrNull(1)?.castOrNull<Int>() ?: -1
                expressionElement = when {
                    element is ParadoxScriptProperty -> element.propertyKey
                    element is ParadoxScriptStringExpressionElement -> element
                    else -> return null
                }
                text = expressionElement.text ?: return null
                if(text.isLeftQuoted()) return null
                if(!text.contains("value:")) return null //快速判断
                expressionElementConfig = config
            }
            //extraArgs: contextConfig
            ParadoxParameterContextReferenceInfo.From.ContextReference -> {
                val contextConfig = extraArgs.getOrNull(0)?.castOrNull<CwtMemberConfig<*>>() ?: return null
                expressionElement = when {
                    element is ParadoxScriptProperty -> element.propertyKey
                    element is ParadoxScriptStringExpressionElement -> element
                    else -> return null
                }
                text = expressionElement.text ?: return null
                if(text.isLeftQuoted()) return null
                if(!text.contains("value:")) return null //快速判断
                expressionElementConfig = contextConfig
            }
            //extraArgs: offset?
            ParadoxParameterContextReferenceInfo.From.InContextReference -> {
                val offset = extraArgs.getOrNull(0)?.castOrNull<Int>() ?: -1
                expressionElement = element.parentOfType<ParadoxScriptStringExpressionElement>(withSelf = true) ?: return null
                text = expressionElement.text ?: return null
                if(text.isLeftQuoted()) return null
                if(!text.contains("value:")) return null //快速判断
                val pipeIndex = text.indexOf('|', text.indexOf("value:").let { if(it != -1) it + 6 else return null })
                if(pipeIndex == -1) return null
                if(offset != -1 && pipeIndex >= offset - expressionElement.startOffset) return null //要求光标在管道符之后（如果offset不为-1）
                expressionElementConfig = CwtConfigHandler.getConfigs(expressionElement).firstOrNull() ?: return null
            }
        }
        if(expressionElementConfig.expression.type !in CwtDataTypeGroups.ValueField) return null
        val configGroup = expressionElementConfig.configGroup
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val range = TextRange.create(0, text.length)
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(text, range, configGroup) ?: return null
        val scriptValueExpression = valueFieldExpression.scriptValueExpression ?: return null
        val definitionName = scriptValueExpression.scriptValueNode.text.orNull() ?: return null
        if(definitionName.isParameterized()) return null //skip if context name is parameterized
        val definitionTypes = listOf("script_value")
        val contextName = definitionName
        val contextIcon = PlsIcons.Nodes.Definition(definitionTypes[0])
        val contextKey = "script_value@${definitionName}"
        val startOffset = element.startOffset
        val contextNameRange = scriptValueExpression.scriptValueNode.rangeInExpression.shiftRight(startOffset) //text range of script value name
        val arguments = mutableListOf<ParadoxParameterContextReferenceInfo.Argument>()
        val pointer = expressionElement.createPointer(project)
        val offset = expressionElement.startOffset
        scriptValueExpression.argumentNodes.forEach f@{ (nameNode, valueNode) ->
            if(completionOffset != -1 && completionOffset in nameNode.rangeInExpression.shiftRight(offset)) return@f
            val argumentName = nameNode.text
            arguments += ParadoxParameterContextReferenceInfo.Argument(argumentName, pointer, nameNode.rangeInExpression.shiftRight(startOffset), pointer, valueNode?.rangeInExpression?.shiftRight(startOffset))
        }
        val info = ParadoxParameterContextReferenceInfo(
            pointer,
            contextName, contextIcon, contextKey,
            pointer, contextNameRange, arguments, gameType, project
        )
        info.definitionName = definitionName
        info.definitionTypes = definitionTypes
        return info
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement? {
        if(rangeInElement == null) return null
        if(config !is CwtMemberConfig<*>) return null
        if(config.expression.type !in CwtDataTypeGroups.ValueField) return null
        val text = element.text
        if(text.isLeftQuoted()) return null
        if(!text.contains("value:")) return null //快速判断
        val range = TextRange.create(0, text.length)
        val configGroup = config.configGroup
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(text, range, configGroup) ?: return null
        val scriptValueExpression = valueFieldExpression.scriptValueExpression ?: return null
        val scriptValueNode = scriptValueExpression.scriptValueNode
        val definitionName = scriptValueNode.text
        if(definitionName.isParameterized()) return null //skip if context name is parameterized
        val definitionTypes = listOf("script_value")
        val argumentNode = scriptValueExpression.nodes.find f@{
            if(it !is ParadoxScriptValueArgumentNode) return@f false
            if(it.rangeInExpression != rangeInElement) return@f false
            true
        } as? ParadoxScriptValueArgumentNode ?: return null
        val name = argumentNode.text
        val contextName = definitionName
        val contextIcon = PlsIcons.Nodes.Definition(definitionTypes[0])
        val contextKey = "script_value@${definitionName}"
        val rangeInParent = rangeInElement
        val readWriteAccess = ReadWriteAccessDetector.Access.Write
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val result = ParadoxParameterElement(element, name, contextName, contextIcon, contextKey, rangeInParent, readWriteAccess, gameType, project)
        result.definitionName = definitionName
        result.definitionTypes = definitionTypes
        return result
    }
    
    override fun processContext(parameterElement: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean) = false
    
    override fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean) = false
}

open class ParadoxInlineScriptParameterSupport : ParadoxParameterSupport {
    override fun isContext(element: ParadoxScriptDefinitionElement): Boolean {
        if(element !is ParadoxScriptFile) return false
        return ParadoxInlineScriptHandler.getInlineScriptExpression(element) != null
    }
    
    override fun findContext(element: PsiElement): ParadoxScriptDefinitionElement? {
        //NOTE 这里需要兼容通过语言注入注入到脚本文件中的脚本片段中的参数（此时需要先获取最外面的injectionHost）
        val finalElement = element.findTopHostElementOrThis(element.project)
        val context = finalElement.containingFile?.castOrNull<ParadoxScriptFile>()
        return context?.takeIf { isContext(it) }
    }
    
    override fun getContextKeyFromContext(context: ParadoxScriptDefinitionElement): String? {
        if(context !is ParadoxScriptFile) return null
        val expression = ParadoxInlineScriptHandler.getInlineScriptExpression(context) ?: return null
        return "inline_script@$expression"
    }
    
    override fun getContextInfo(element: ParadoxScriptDefinitionElement): ParadoxParameterContextInfo? {
        if(!isContext(element)) return null
        return ParadoxParameterHandler.getContextInfo(element)
    }
    
    override fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        var inlineConfig: CwtInlineConfig? = null
        var contextReferenceElement: ParadoxScriptProperty? = null
        var completionOffset = -1
        when(from) {
            //extraArgs: config, completionOffset?
            ParadoxParameterContextReferenceInfo.From.Argument -> {
                val config = extraArgs.getOrNull(0)?.castOrNull<CwtMemberConfig<*>>() ?: return null
                completionOffset = extraArgs.getOrNull(1)?.castOrNull<Int>() ?: -1
                if(config !is CwtPropertyConfig || config.expression.type != CwtDataTypes.Parameter) return null
                //infer inline config
                val contextConfig = config.castOrNull<CwtPropertyConfig>()?.parentConfig?.castOrNull<CwtPropertyConfig>() ?: return null
                inlineConfig = contextConfig.inlineableConfig?.castOrNull<CwtInlineConfig>()?.takeIf { it.name == ParadoxInlineScriptHandler.inlineScriptKey } ?: return null
                contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
            }
            //extraArgs: contextConfig
            ParadoxParameterContextReferenceInfo.From.ContextReference -> {
                val contextConfig = extraArgs.getOrNull(0)?.castOrNull<CwtPropertyConfig>() ?: return null
                inlineConfig = contextConfig.inlineableConfig?.castOrNull<CwtInlineConfig>()?.takeIf { it.name == ParadoxInlineScriptHandler.inlineScriptKey } ?: return null
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
                    val propConfig = CwtConfigHandler.getConfigs(prop).firstOrNull() ?: continue
                    val propInlineConfig = propConfig.inlineableConfig?.castOrNull<CwtInlineConfig>()?.takeIf { it.name == ParadoxInlineScriptHandler.inlineScriptKey } ?: continue
                    if(propInlineConfig.config.configs?.any { it is CwtPropertyConfig && it.expression.type == CwtDataTypes.Parameter } != true) continue
                    inlineConfig = propInlineConfig
                    contextReferenceElement = prop
                    break
                }
            }
        }
        if(inlineConfig == null || contextReferenceElement == null) return null
        val configGroup = inlineConfig.configGroup
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val expression = ParadoxInlineScriptHandler.getInlineScriptExpressionFromInlineConfig(contextReferenceElement, inlineConfig) ?: return null
        if(expression.isParameterized()) return null //skip if context name is parameterized
        val contextName = expression
        val contextIcon = PlsIcons.Nodes.InlineScript
        val contextKey = "inline_script@$expression"
        val contextNameElement = contextReferenceElement.propertyKey
        val arguments = mutableListOf<ParadoxParameterContextReferenceInfo.Argument>()
        contextReferenceElement.block?.processProperty p@{
            if(completionOffset != -1 && completionOffset in it.textRange) return@p true
            val k = it.propertyKey
            val v = it.propertyValue
            val argumentName = k.name
            if(argumentName == "script") return@p true //hardcoded
            arguments += ParadoxParameterContextReferenceInfo.Argument(argumentName, k.createPointer(project), k.textRange, v?.createPointer(project), v?.textRange)
            true
        }
        val info = ParadoxParameterContextReferenceInfo(
            contextReferenceElement.createPointer(project),
            contextName, contextIcon, contextKey,
            contextNameElement.createPointer(project), contextNameElement.textRange, arguments, gameType, project
        )
        info.inlineScriptExpression = expression
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
        val context = findContext(element) as? ParadoxScriptFile ?: return null
        val expression = ParadoxInlineScriptHandler.getInlineScriptExpression(context) ?: return null
        val contextName = expression
        val contextIcon = PlsIcons.Nodes.InlineScript
        val contextKey = "inline_script@$expression"
        val rangeInParent = TextRange.create(0, element.textLength)
        val readWriteAccess = ParadoxParameterHandler.getReadWriteAccess(element)
        val gameType = selectGameType(context) ?: return null
        val project = context.project
        val result = ParadoxParameterElement(element, name, contextName, contextIcon, contextKey, rangeInParent, readWriteAccess, gameType, project)
        result.containingContext = context.createPointer(project)
        result.inlineScriptExpression = expression
        return result
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement? {
        if(config !is CwtPropertyConfig || config.expression.type != CwtDataTypes.Parameter) return null
        return doResolveArgument(element, rangeInElement, config)
    }
    
    private fun doResolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtPropertyConfig): ParadoxParameterElement? {
        val contextConfig = config.castOrNull<CwtPropertyConfig>()?.parentConfig?.castOrNull<CwtPropertyConfig>() ?: return null
        val inlineConfig = contextConfig.inlineableConfig?.castOrNull<CwtInlineConfig>()?.takeIf { it.name == ParadoxInlineScriptHandler.inlineScriptKey } ?: return null
        val contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
        val expression = ParadoxInlineScriptHandler.getInlineScriptExpressionFromInlineConfig(contextReferenceElement, inlineConfig) ?: return null
        if(expression.isParameterized()) return null //skip if context name is parameterized
        val name = element.name
        val contextName = expression
        val contextIcon = PlsIcons.Nodes.InlineScript
        val contextKey = "inline_script@$expression"
        val rangeInParent = rangeInElement ?: TextRange.create(0, element.textLength)
        val readWriteAccess = ReadWriteAccessDetector.Access.Write
        val gameType = config.configGroup.gameType ?: return null
        val project = config.configGroup.project
        val result = ParadoxParameterElement(element, name, contextName, contextIcon, contextKey, rangeInParent, readWriteAccess, gameType, project)
        result.inlineScriptExpression = expression
        return result
    }
    
    override fun processContext(parameterElement: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val expression = parameterElement.inlineScriptExpression ?: return false
        if(expression.isParameterized()) return false //skip if context name is parameterized
        val project = parameterElement.project
        ParadoxInlineScriptHandler.processInlineScriptFile(expression, parameterElement, project, onlyMostRelevant, processor)
        return true
    }
    
    override fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val expression = contextReferenceInfo.inlineScriptExpression ?: return false
        if(expression.isParameterized()) return false //skip if context name is parameterized
        val project = contextReferenceInfo.project
        ParadoxInlineScriptHandler.processInlineScriptFile(expression, element, project, onlyMostRelevant, processor)
        return true
    }
    
    override fun getModificationTracker(parameterInfo: ParadoxParameterInfo): ModificationTracker {
        return ParadoxModificationTrackers.InlineScriptsTracker
    }
    
    override fun buildDocumentationDefinition(parameterElement: ParadoxParameterElement, builder: DocumentationBuilder): Boolean = with(builder) {
        val inlineScriptExpression = parameterElement.inlineScriptExpression ?: return false
        if(inlineScriptExpression.isEmpty()) return false
        val filePath = ParadoxInlineScriptHandler.getInlineScriptFilePath(inlineScriptExpression) ?: return false
        
        //不加上文件信息
        
        //加上名字
        val name = parameterElement.name
        append(PlsBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        //加上推断得到的类型信息
        val inferredType = ParadoxParameterHandler.getInferredType(parameterElement)
        if(inferredType != null) {
            append(": ").append(inferredType.escapeXml())
        }
        //加上所属内联脚本信息
        val gameType = parameterElement.gameType
        appendBr().appendIndent()
        append(PlsBundle.message("ofInlineScript")).append(" ")
        appendFilePathLink(gameType, filePath, inlineScriptExpression, parameterElement)
        return true
    }
}