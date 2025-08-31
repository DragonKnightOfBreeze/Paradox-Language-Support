package icu.windea.pls.ep.parameter

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtInlineConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.definitionParameterModificationTracker
import icu.windea.pls.config.configGroup.definitionTypesSupportParameters
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.documentation.DocumentationBuilder
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.findTopHostElementOrThis
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.documentation.appendBr
import icu.windea.pls.lang.documentation.appendIndent
import icu.windea.pls.lang.documentation.appendPsiLinkOrUnresolved
import icu.windea.pls.lang.expression.ParadoxValueFieldExpression
import icu.windea.pls.lang.expression.nodes.ParadoxScriptValueArgumentNode
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.processQueryAsync
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.model.ParadoxParameterContextInfo
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.model.ReferenceLinkType
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.model.elementInfo.ParadoxParameterInfo
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.findParentDefinition
import icu.windea.pls.script.psi.findParentProperty
import icu.windea.pls.script.psi.properties

open class ParadoxDefinitionParameterSupport : ParadoxParameterSupport {
    override fun isContext(element: ParadoxScriptDefinitionElement): Boolean {
        if (element !is ParadoxScriptProperty) return false
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
        if (!isContext(element)) return null
        return ParadoxParameterManager.getContextInfo(element)
    }

    override fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        var contextConfig: CwtPropertyConfig? = null
        var contextReferenceElement: ParadoxScriptProperty? = null
        var completionOffset = -1
        when (from) {
            //extraArgs: config, completionOffset?
            ParadoxParameterContextReferenceInfo.From.Argument -> {
                val config = extraArgs.getOrNull(0)?.castOrNull<CwtMemberConfig<*>>() ?: return null
                completionOffset = extraArgs.getOrNull(1)?.castOrNull<Int>() ?: -1
                //infer context config
                contextConfig = config.castOrNull<CwtPropertyConfig>()?.parentConfig?.castOrNull<CwtPropertyConfig>() ?: return null
                if (contextConfig.configExpression.type != CwtDataTypes.Definition) return null
                contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
            }
            //extraArgs: contextConfig
            ParadoxParameterContextReferenceInfo.From.ContextReference -> {
                contextConfig = extraArgs.getOrNull(0)?.castOrNull<CwtPropertyConfig>() ?: return null
                if (contextConfig.configExpression.type != CwtDataTypes.Definition) return null
                contextReferenceElement = element.castOrNull() ?: return null
            }
            //extraArgs: offset?
            ParadoxParameterContextReferenceInfo.From.InContextReference -> {
                val parentBlock = when (element.elementType) {
                    ParadoxScriptElementTypes.LEFT_BRACE -> element.parent.parentOfType<ParadoxScriptBlock>()
                    else -> element.parentOfType<ParadoxScriptBlock>()
                } ?: return null
                val parentProperties = parentBlock.parentsOfType<ParadoxScriptProperty>(withSelf = false)
                for (prop in parentProperties) {
                    //infer context config
                    val propConfig = ParadoxExpressionManager.getConfigs(prop).firstOrNull() as? CwtPropertyConfig ?: continue
                    if (propConfig.configExpression.type != CwtDataTypes.Definition) continue
                    if (propConfig.configs?.any { it is CwtPropertyConfig && it.configExpression.type == CwtDataTypes.Parameter } != true) continue
                    contextConfig = propConfig
                    contextReferenceElement = prop
                    break
                }
            }
        }
        if (contextConfig == null || contextReferenceElement == null) return null
        val configGroup = contextConfig.configGroup
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val definitionName = contextReferenceElement.name.orNull() ?: return null
        if (definitionName.isParameterized()) return null //skip if context name is parameterized
        val definitionTypes = contextConfig.configExpression.value?.split('.') ?: return null
        val contextName = definitionName
        val contextIcon = PlsIcons.Nodes.Definition(definitionTypes[0])
        val contextKey = "${definitionTypes.joinToString(".")}@${definitionName}"
        val contextNameElement = contextReferenceElement.propertyKey
        val arguments = mutableListOf<ParadoxParameterContextReferenceInfo.Argument>()
        contextReferenceElement.block?.properties()?.forEach f@{
            if (completionOffset != -1 && completionOffset in it.textRange) return@f
            val k = it.propertyKey
            val v = it.propertyValue
            val argumentName = k.name
            arguments += ParadoxParameterContextReferenceInfo.Argument(argumentName, k.createPointer(project), k.textRange, v?.createPointer(project), v?.textRange)
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
        val readWriteAccess = ParadoxParameterManager.getReadWriteAccess(element)
        val gameType = definitionInfo.gameType
        val project = definitionInfo.project
        val result = ParadoxParameterElement(element, name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        result.containingContext = context.createPointer(project)
        result.definitionName = definitionName
        result.definitionTypes = definitionTypes
        return result
    }

    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement? {
        if (config !is CwtPropertyConfig || config.configExpression.type != CwtDataTypes.Parameter) return null
        return doResolveArgument(element, config)
    }

    private fun doResolveArgument(element: ParadoxScriptExpressionElement, config: CwtPropertyConfig): ParadoxParameterElement? {
        val contextConfig = config.castOrNull<CwtPropertyConfig>()?.parentConfig?.castOrNull<CwtPropertyConfig>() ?: return null
        if (contextConfig.configExpression.type != CwtDataTypes.Definition) return null
        val contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
        val definitionName = contextReferenceElement.name.orNull() ?: return null
        if (definitionName.isParameterized()) return null //skip if context name is parameterized
        val definitionTypes = contextConfig.configExpression.value?.split('.') ?: return null
        val name = element.name
        val contextName = definitionName
        val contextIcon = PlsIcons.Nodes.Definition(definitionTypes[0])
        val contextKey = "${definitionTypes.joinToString(".")}@${definitionName}"
        val readWriteAccess = ParadoxParameterManager.getReadWriteAccess(element)
        val gameType = config.configGroup.gameType ?: return null
        val project = config.configGroup.project
        val result = ParadoxParameterElement(element, name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        result.containingContextReference = contextReferenceElement.createPointer(project)
        result.definitionName = definitionName
        result.definitionTypes = definitionTypes
        return result
    }

    override fun processContext(parameterElement: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val definitionName = parameterElement.definitionName ?: return false
        val definitionTypes = parameterElement.definitionTypes ?: return false
        if (definitionName.isParameterized()) return false //skip if context name is parameterized
        val definitionType = definitionTypes.joinToString(".")
        val project = parameterElement.project
        val selector = selector(project, parameterElement).definition().contextSensitive()
        ParadoxDefinitionSearch.search(definitionName, definitionType, selector).processQueryAsync(onlyMostRelevant, processor)
        return true
    }

    override fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val definitionName = contextReferenceInfo.definitionName ?: return false
        val definitionTypes = contextReferenceInfo.definitionTypes ?: return false
        if (definitionName.isParameterized()) return false //skip if context name is parameterized
        val definitionType = definitionTypes.joinToString(".")
        val project = contextReferenceInfo.project
        val selector = selector(project, element).definition().contextSensitive()
        ParadoxDefinitionSearch.search(definitionName, definitionType, selector).processQueryAsync(onlyMostRelevant, processor)
        return true
    }

    override fun getModificationTracker(parameterInfo: ParadoxParameterInfo): ModificationTracker? {
        val configGroup = PlsFacade.getConfigGroup(parameterInfo.project, parameterInfo.gameType)
        return configGroup.definitionParameterModificationTracker
    }

    override fun buildDocumentationDefinition(parameterElement: ParadoxParameterElement, builder: DocumentationBuilder): Boolean = with(builder) {
        val definitionName = parameterElement.definitionName ?: return false
        val definitionType = parameterElement.definitionTypes ?: return false
        if (definitionType.isEmpty()) return false

        //不加上文件信息

        //加上名字
        val name = parameterElement.name
        append(PlsStringConstants.parameterPrefix).append(" <b>").append(name.escapeXml().or.anonymous()).append("</b>")
        //加上推断得到的类型信息
        val inferredType = ParadoxParameterManager.getInferredType(parameterElement)
        if (inferredType != null) {
            append(": ").append(inferredType.escapeXml())
        }
        //加上所属定义信息
        val gameType = parameterElement.gameType
        val categories = ReferenceLinkType.CwtConfig.Categories
        appendBr().appendIndent()
        append(PlsBundle.message("ofDefinition")).append(" ")
        val link = ReferenceLinkType.Definition.createLink(definitionName, definitionType.first(), gameType)
        appendPsiLinkOrUnresolved(link.escapeXml(), definitionName.escapeXml(), context = parameterElement)
        append(": ")
        val type = definitionType.first()
        val typeLink = ReferenceLinkType.CwtConfig.createLink(categories.types, type, gameType)
        appendPsiLinkOrUnresolved(typeLink.escapeXml(), type.escapeXml())
        for ((index, t) in definitionType.withIndex()) {
            if (index == 0) continue
            append(", ")
            val subtypeLink = ReferenceLinkType.CwtConfig.createLink(categories.types, "$type/$t", gameType)
            appendPsiLinkOrUnresolved(subtypeLink.escapeXml(), t.escapeXml())
        }
        return true
    }
}

/**
 * @see icu.windea.pls.lang.expression.ParadoxScriptValueExpression
 * @see icu.windea.pls.lang.expression.nodes.ParadoxScriptValueArgumentNode
 * @see icu.windea.pls.lang.expression.nodes.ParadoxScriptValueArgumentValueNode
 */
class ParadoxScriptValueInlineParameterSupport : ParadoxParameterSupport {
    override fun isContext(element: ParadoxScriptDefinitionElement) = false

    override fun findContext(element: PsiElement) = null

    override fun getContextKeyFromContext(context: ParadoxScriptDefinitionElement) = null

    override fun resolveParameter(element: ParadoxParameter) = null

    override fun resolveConditionParameter(element: ParadoxConditionParameter) = null

    override fun getContextInfo(element: ParadoxScriptDefinitionElement) = null

    override fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        var expressionElement: ParadoxScriptStringExpressionElement?
        var expressionString: String?
        var expressionElementConfig: CwtMemberConfig<*>?
        var completionOffset = -1
        when (from) {
            //extraArgs: config, completionOffset?
            ParadoxParameterContextReferenceInfo.From.Argument -> {
                val config = extraArgs.getOrNull(0)?.castOrNull<CwtMemberConfig<*>>() ?: return null
                completionOffset = extraArgs.getOrNull(1)?.castOrNull<Int>() ?: -1
                expressionElement = when {
                    element is ParadoxScriptProperty -> element.propertyKey
                    element is ParadoxScriptStringExpressionElement -> element
                    else -> return null
                }
                expressionString = expressionElement.value
                if (!expressionString.contains("value:")) return null //快速判断
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
                expressionString = expressionElement.value
                if (!expressionString.contains("value:")) return null //快速判断
                expressionElementConfig = contextConfig
            }
            //extraArgs: offset?
            ParadoxParameterContextReferenceInfo.From.InContextReference -> {
                val offset = extraArgs.getOrNull(0)?.castOrNull<Int>() ?: -1
                expressionElement = element.parentOfType<ParadoxScriptStringExpressionElement>(withSelf = true) ?: return null
                expressionString = expressionElement.value
                if (!expressionString.contains("value:")) return null //快速判断
                val pipeIndex = expressionString.indexOf('|', expressionString.indexOf("value:").let { if (it != -1) it + 6 else return null })
                if (pipeIndex == -1) return null
                if (offset != -1 && pipeIndex >= offset - expressionElement.startOffset) return null //要求光标在管道符之后（如果offset不为-1）
                expressionElementConfig = ParadoxExpressionManager.getConfigs(expressionElement).firstOrNull() ?: return null
            }
        }
        if (expressionElementConfig.configExpression.type !in CwtDataTypeGroups.ValueField) return null
        val configGroup = expressionElementConfig.configGroup
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val range = TextRange.create(0, expressionString.length)
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(expressionString, range, configGroup) ?: return null
        val scriptValueExpression = valueFieldExpression.scriptValueExpression ?: return null
        val definitionName = scriptValueExpression.scriptValueNode.text.orNull() ?: return null
        if (definitionName.isParameterized()) return null //skip if context name is parameterized
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
            if (completionOffset != -1 && completionOffset in nameNode.rangeInExpression.shiftRight(offset)) return@f
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
        if (rangeInElement == null) return null
        if (config !is CwtMemberConfig<*>) return null
        if (config.configExpression.type !in CwtDataTypeGroups.ValueField) return null
        val expressionString = element.value
        if (!expressionString.contains("value:")) return null //快速判断
        val range = TextRange.create(0, expressionString.length)
        val configGroup = config.configGroup
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(expressionString, range, configGroup) ?: return null
        val scriptValueExpression = valueFieldExpression.scriptValueExpression ?: return null
        val scriptValueNode = scriptValueExpression.scriptValueNode
        val definitionName = scriptValueNode.text
        if (definitionName.isParameterized()) return null //skip if context name is parameterized
        val definitionTypes = listOf("script_value")
        val argumentNode = scriptValueExpression.nodes.find f@{
            if (it !is ParadoxScriptValueArgumentNode) return@f false
            if (it.rangeInExpression != rangeInElement) return@f false
            true
        } as? ParadoxScriptValueArgumentNode ?: return null
        val name = argumentNode.text
        val contextName = definitionName
        val contextIcon = PlsIcons.Nodes.Definition(definitionTypes[0])
        val contextKey = "script_value@${definitionName}"
        val readWriteAccess = ReadWriteAccessDetector.Access.Write
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val result = ParadoxParameterElement(element, name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        result.definitionName = definitionName
        result.definitionTypes = definitionTypes
        return result
    }

    override fun processContext(parameterElement: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean) = false

    override fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean) = false
}

open class ParadoxInlineScriptParameterSupport : ParadoxParameterSupport {
    override fun isContext(element: ParadoxScriptDefinitionElement): Boolean {
        if (element !is ParadoxScriptFile) return false
        return ParadoxInlineScriptManager.getInlineScriptExpression(element) != null
    }

    override fun findContext(element: PsiElement): ParadoxScriptDefinitionElement? {
        //NOTE 这里需要兼容通过语言注入注入到脚本文件中的脚本片段中的参数（此时需要先获取最外面的injectionHost）
        val finalElement = element.findTopHostElementOrThis(element.project)
        val context = finalElement.containingFile?.castOrNull<ParadoxScriptFile>()
        return context?.takeIf { isContext(it) }
    }

    override fun getContextKeyFromContext(context: ParadoxScriptDefinitionElement): String? {
        if (context !is ParadoxScriptFile) return null
        val expression = ParadoxInlineScriptManager.getInlineScriptExpression(context) ?: return null
        return "inline_script@$expression"
    }

    override fun getContextInfo(element: ParadoxScriptDefinitionElement): ParadoxParameterContextInfo? {
        if (!isContext(element)) return null
        return ParadoxParameterManager.getContextInfo(element)
    }

    override fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        var inlineConfig: CwtInlineConfig? = null
        var contextReferenceElement: ParadoxScriptProperty? = null
        var completionOffset = -1
        when (from) {
            //extraArgs: config, completionOffset?
            ParadoxParameterContextReferenceInfo.From.Argument -> {
                val config = extraArgs.getOrNull(0)?.castOrNull<CwtMemberConfig<*>>() ?: return null
                completionOffset = extraArgs.getOrNull(1)?.castOrNull<Int>() ?: -1
                if (config !is CwtPropertyConfig || config.configExpression.type != CwtDataTypes.Parameter) return null
                //infer inline config
                val contextConfig = config.castOrNull<CwtPropertyConfig>()?.parentConfig?.castOrNull<CwtPropertyConfig>() ?: return null
                inlineConfig = contextConfig.inlineConfig?.takeIf { it.name == ParadoxInlineScriptManager.inlineScriptKey } ?: return null
                contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
            }
            //extraArgs: contextConfig
            ParadoxParameterContextReferenceInfo.From.ContextReference -> {
                val contextConfig = extraArgs.getOrNull(0)?.castOrNull<CwtPropertyConfig>() ?: return null
                inlineConfig = contextConfig.inlineConfig?.takeIf { it.name == ParadoxInlineScriptManager.inlineScriptKey } ?: return null
                contextReferenceElement = element.castOrNull() ?: return null
            }
            //extraArgs: offset?
            ParadoxParameterContextReferenceInfo.From.InContextReference -> {
                val parentBlock = when (element.elementType) {
                    ParadoxScriptElementTypes.LEFT_BRACE -> element.parent.parentOfType<ParadoxScriptBlock>()
                    else -> element.parentOfType<ParadoxScriptBlock>()
                } ?: return null
                val parentProperties = parentBlock.parentsOfType<ParadoxScriptProperty>(withSelf = false)
                for (prop in parentProperties) {
                    //infer context config
                    val propConfig = ParadoxExpressionManager.getConfigs(prop).findIsInstance<CwtPropertyConfig>() ?: continue
                    val propInlineConfig = propConfig.inlineConfig?.takeIf { it.name == ParadoxInlineScriptManager.inlineScriptKey } ?: continue
                    if (propInlineConfig.config.configs?.any { it is CwtPropertyConfig && it.configExpression.type == CwtDataTypes.Parameter } != true) continue
                    inlineConfig = propInlineConfig
                    contextReferenceElement = prop
                    break
                }
            }
        }
        if (inlineConfig == null || contextReferenceElement == null) return null
        val configGroup = inlineConfig.configGroup
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val expression = ParadoxInlineScriptManager.getInlineScriptExpressionFromInlineConfig(contextReferenceElement, inlineConfig) ?: return null
        if (expression.isParameterized()) return null //skip if context name is parameterized
        val contextName = expression
        val contextIcon = PlsIcons.Nodes.InlineScript
        val contextKey = "inline_script@$expression"
        val contextNameElement = contextReferenceElement.propertyKey
        val arguments = mutableListOf<ParadoxParameterContextReferenceInfo.Argument>()
        contextReferenceElement.block?.properties()?.forEach f@{
            if (completionOffset != -1 && completionOffset in it.textRange) return@f
            val k = it.propertyKey
            val v = it.propertyValue
            val argumentName = k.name
            if (argumentName == "script") return@f //hardcoded
            arguments += ParadoxParameterContextReferenceInfo.Argument(argumentName, k.createPointer(project), k.textRange, v?.createPointer(project), v?.textRange)
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
        val expression = ParadoxInlineScriptManager.getInlineScriptExpression(context) ?: return null
        val contextName = expression
        val contextIcon = PlsIcons.Nodes.InlineScript
        val contextKey = "inline_script@$expression"
        val readWriteAccess = ParadoxParameterManager.getReadWriteAccess(element)
        val gameType = selectGameType(context) ?: return null
        val project = context.project
        val result = ParadoxParameterElement(element, name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        result.containingContext = context.createPointer(project)
        result.inlineScriptExpression = expression
        return result
    }

    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement? {
        if (config !is CwtPropertyConfig || config.configExpression.type != CwtDataTypes.Parameter) return null
        return doResolveArgument(element, config)
    }

    private fun doResolveArgument(element: ParadoxScriptExpressionElement, config: CwtPropertyConfig): ParadoxParameterElement? {
        val contextConfig = config.castOrNull<CwtPropertyConfig>()?.parentConfig?.castOrNull<CwtPropertyConfig>() ?: return null
        val inlineConfig = contextConfig.inlineConfig?.takeIf { it.name == ParadoxInlineScriptManager.inlineScriptKey } ?: return null
        val contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
        val expression = ParadoxInlineScriptManager.getInlineScriptExpressionFromInlineConfig(contextReferenceElement, inlineConfig) ?: return null
        if (expression.isParameterized()) return null //skip if context name is parameterized
        val name = element.name
        val contextName = expression
        val contextIcon = PlsIcons.Nodes.InlineScript
        val contextKey = "inline_script@$expression"
        val readWriteAccess = ReadWriteAccessDetector.Access.Write
        val gameType = config.configGroup.gameType ?: return null
        val project = config.configGroup.project
        val result = ParadoxParameterElement(element, name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        result.containingContextReference = contextReferenceElement.createPointer(project)
        result.inlineScriptExpression = expression
        return result
    }

    override fun processContext(parameterElement: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val expression = parameterElement.inlineScriptExpression ?: return false
        if (expression.isParameterized()) return false //skip if context name is parameterized
        val project = parameterElement.project
        ParadoxInlineScriptManager.processInlineScriptFile(expression, project, parameterElement, onlyMostRelevant, processor)
        return true
    }

    override fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val expression = contextReferenceInfo.inlineScriptExpression ?: return false
        if (expression.isParameterized()) return false //skip if context name is parameterized
        val project = contextReferenceInfo.project
        ParadoxInlineScriptManager.processInlineScriptFile(expression, project, element, onlyMostRelevant, processor)
        return true
    }

    override fun getModificationTracker(parameterInfo: ParadoxParameterInfo): ModificationTracker {
        return ParadoxModificationTrackers.InlineScriptsTracker
    }

    override fun buildDocumentationDefinition(parameterElement: ParadoxParameterElement, builder: DocumentationBuilder): Boolean = with(builder) {
        val inlineScriptExpression = parameterElement.inlineScriptExpression ?: return false
        if (inlineScriptExpression.isEmpty()) return false
        val filePath = ParadoxInlineScriptManager.getInlineScriptFilePath(inlineScriptExpression) ?: return false

        //不加上文件信息

        //加上名字
        val name = parameterElement.name
        append(PlsStringConstants.parameterPrefix).append(" <b>").append(name.escapeXml().or.anonymous()).append("</b>")
        //加上推断得到的类型信息
        val inferredType = ParadoxParameterManager.getInferredType(parameterElement)
        if (inferredType != null) {
            append(": ").append(inferredType.escapeXml())
        }
        //加上所属内联脚本信息
        val gameType = parameterElement.gameType
        appendBr().appendIndent()
        append(PlsBundle.message("ofInlineScript")).append(" ")
        val link = ReferenceLinkType.FilePath.createLink(filePath, gameType)
        appendPsiLinkOrUnresolved(link.escapeXml(), inlineScriptExpression.escapeXml(), context = parameterElement)
        return true
    }
}
