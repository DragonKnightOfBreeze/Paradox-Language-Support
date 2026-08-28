package icu.windea.pls.lang.resolve.util

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentsOfType
import com.intellij.psi.util.startOffset
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtMacroConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.select.selectConfigScope
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.resolve.ParadoxInlineScriptService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScriptValueReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.*
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.ParadoxParameterContextInfo
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.containingContext
import icu.windea.pls.model.containingContextReference
import icu.windea.pls.model.definitionName
import icu.windea.pls.model.definitionTypes
import icu.windea.pls.model.expressions.ParadoxConditionalExpression
import icu.windea.pls.model.inlineScriptExpression
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptConditionParameter
import icu.windea.pls.script.psi.ParadoxScriptConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptConditionalExpression
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import java.util.*

object ParadoxParameterSupportFactory {
    fun getReadWriteAccess(element: PsiElement): ReadWriteAccess {
        return when {
            element is ParadoxParameter -> ReadWriteAccess.Read
            element is ParadoxScriptConditionParameter -> ReadWriteAccess.Read
            else -> ReadWriteAccess.Write
        }
    }

    fun resolveParameterForDefinition(element: PsiElement, name: String, context: ParadoxDefinitionElement): ParadoxParameterLightElement? {
        val definitionInfo = context.definitionInfo ?: return null
        val definitionName = context.name
        val definitionTypes = definitionInfo.types
        val contextName = definitionName
        val contextIcon = ChronicleIcons.Nodes.Definition(definitionInfo.type)
        val contextKey = "${definitionTypes.joinToString(".")}@${definitionName}"
        val readWriteAccess = getReadWriteAccess(element)
        val gameType = definitionInfo.gameType
        val project = definitionInfo.project
        val result = ParadoxParameterLightElement(element, name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        result.containingContext = context.createPointer(project)
        result.definitionName = definitionName
        result.definitionTypes = definitionTypes
        return result
    }

    fun resolveParameterForInlineScript(element: PsiElement, name: String, context: ParadoxScriptFile): ParadoxParameterLightElement? {
        val expression = ParadoxInlineScriptManager.getInlineScriptExpression(context) ?: return null
        val contextName = expression
        val contextIcon = ChronicleIcons.Nodes.Macro
        val contextKey = "inline_script@$expression"
        val readWriteAccess = getReadWriteAccess(element)
        val gameType = selectGameType(context) ?: return null
        val project = context.project
        val result = ParadoxParameterLightElement(element, name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        result.containingContext = context.createPointer(project)
        result.inlineScriptExpression = expression
        return result
    }

    @Suppress("UNUSED_PARAMETER")
    fun resolveArgumentForDefinition(element: ParadoxScriptExpressionElement, rangeInExpression: TextRange?, config: CwtConfig<*>): ParadoxParameterLightElement? {
        if (element !is ParadoxScriptPropertyKey) return null
        if (config !is CwtPropertyConfig || config.configExpression.type != CwtDataTypes.Parameter) return null
        val contextConfig = selectConfigScope { config.asProperty()?.parentConfig.asProperty() } ?: return null
        if (contextConfig.configExpression.type != CwtDataTypes.Definition) return null
        val contextReferenceElement = selectScope { element.queryParentBy("*/*").asProperty() } ?: return null
        val definitionName = contextReferenceElement.name.orNull() ?: return null
        if (definitionName.isParameterized()) return null // skip if context name is parameterized
        val definitionTypes = contextConfig.configExpression.metadata.value?.split('.') ?: return null
        val name = element.name.orNull() ?: return null
        val contextName = definitionName
        val contextIcon = ChronicleIcons.Nodes.Definition(definitionTypes[0])
        val contextKey = "${definitionTypes.joinToString(".")}@${definitionName}"
        val readWriteAccess = getReadWriteAccess(element)
        val gameType = config.configGroup.gameType
        val project = config.configGroup.project
        val result = ParadoxParameterLightElement(element, name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        result.containingContextReference = contextReferenceElement.createPointer(project)
        result.definitionName = definitionName
        result.definitionTypes = definitionTypes
        return result
    }

    @Suppress("UNUSED_PARAMETER")
    fun resolveArgumentForInlineScript(element: ParadoxScriptExpressionElement, rangeInExpression: TextRange?, config: CwtConfig<*>): ParadoxParameterLightElement? {
        // NOTE 2.1.0 这里目前不验证游戏类型
        if (element !is ParadoxScriptPropertyKey) return null
        if (config !is CwtPropertyConfig || config.configExpression.type != CwtDataTypes.Parameter) return null
        val contextConfig = selectConfigScope { config.asProperty()?.parentConfig.asProperty() } ?: return null
        val inlineConfig = contextConfig.inlineConfig?.takeIf { ParadoxInlineScriptManager.isMatched(it.name) }
        if (inlineConfig == null) return null
        val contextReferenceElement = selectScope { element.queryParentBy("*/*").asProperty() } ?: return null
        val argumentName = element.name.orNull()?.takeIf { it != "script" } ?: return null
        val inlineScriptExpression = ParadoxInlineScriptService.getInlineScriptExpressionFromUsageElement(contextReferenceElement) ?: return null
        val name = argumentName
        val contextName = inlineScriptExpression.takeIf { !it.isParameterized() } ?: return null
        val contextIcon = ChronicleIcons.Nodes.Macro
        val contextKey = "inline_script@$inlineScriptExpression"
        val readWriteAccess = ReadWriteAccess.Write
        val gameType = config.configGroup.gameType
        val project = config.configGroup.project
        val result = ParadoxParameterLightElement(element, name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        result.containingContextReference = contextReferenceElement.createPointer(project)
        result.inlineScriptExpression = inlineScriptExpression
        return result
    }

    fun resolveArgumentForScriptValueReference(element: ParadoxScriptExpressionElement, rangeInExpression: TextRange?, config: CwtConfig<*>): ParadoxParameterLightElement? {
        if (rangeInExpression == null) return null
        if (config !is CwtMemberConfig<*>) return null
        if (config.configExpression.type !in CwtDataTypeSets.ValueField) return null
        val expressionString = element.value
        if (!expressionString.contains("value:")) return null // 快速判断
        val configGroup = config.configGroup
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(expressionString, null, configGroup) ?: return null
        val scriptValueReferenceExpression = valueFieldExpression.fieldNode.asDynamic()?.valueNode?.dataSourceNode?.castOrNull<ParadoxScriptValueReferenceExpression>() ?: return null
        val scriptValueNode = scriptValueReferenceExpression.scriptValueNode
        val definitionName = scriptValueNode.text
        if (definitionName.isParameterized()) return null // skip if context name is parameterized
        val definitionTypes = listOf(ParadoxDefinitionTypes.scriptValue)
        val argumentNode = scriptValueReferenceExpression.nodes.find f@{
            if (it !is ParadoxScriptValueArgumentNameNode) return@f false
            if (it.rangeInExpression != rangeInExpression) return@f false
            true
        } as? ParadoxScriptValueArgumentNameNode ?: return null
        val name = argumentNode.text.orNull() ?: return null
        val contextName = definitionName
        val contextIcon = ChronicleIcons.Nodes.Definition(definitionTypes[0])
        val contextKey = "script_value@${definitionName}"
        val readWriteAccess = ReadWriteAccess.Write
        val gameType = configGroup.gameType
        val project = configGroup.project
        val result = ParadoxParameterLightElement(element, name, contextName, contextIcon, contextKey, readWriteAccess, gameType, project)
        result.definitionName = definitionName
        result.definitionTypes = definitionTypes
        return result
    }

    fun getContextInfo(element: ParadoxDefinitionElement): ParadoxParameterContextInfo? {
        val file = element.containingFile
        val project = file.project
        val gameType = selectGameType(file) ?: return null
        val parameters = sortedMapOf<String, MutableList<ParadoxParameterContextInfo.Parameter>>() // 按名字进行排序
        val fileConditionExpressions = ArrayDeque<ParadoxConditionalExpression>()
        element.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxParameter) return visitParameter(element)
                if (element is ParadoxScriptConditionParameter) return visitConditionParameter(element)
                if (element is ParadoxScriptConditionalExpression) return visitConditionalExpression(element)
                super.visitElement(element)
            }

            private fun visitParameter(element: ParadoxParameter) {
                val name = element.name ?: return
                val defaultValue = element.defaultValue
                val conditionalExpressions = ArrayDeque(fileConditionExpressions) // not null
                val elementPointer = element.createPointer<PsiElement>(file)
                val info = ParadoxParameterContextInfo.Parameter(elementPointer, name, defaultValue, conditionalExpressions, project, gameType)
                parameters.getOrPut(name) { mutableListOf() }.add(info)
                // 不需要继续向下遍历
            }

            private fun visitConditionParameter(element: ParadoxScriptConditionParameter) {
                val name = element.name ?: return
                val elementPointer = element.createPointer<PsiElement>(file)
                val info = ParadoxParameterContextInfo.Parameter(elementPointer, name, null, null, project, gameType)
                parameters.getOrPut(name) { mutableListOf() }.add(info)
                // 不需要继续向下遍历
            }

            private fun visitConditionalExpression(element: ParadoxScriptConditionalExpression) {
                // value may be empty (invalid condition expression)
                fileConditionExpressions.addLast(ParadoxConditionalExpression.resolve(element.text))
                super.visitElement(element)
            }

            override fun elementFinished(element: PsiElement?) {
                if (element is ParadoxScriptConditionalBlock) {
                    fileConditionExpressions.removeLast()
                }
            }
        })
        return ParadoxParameterContextInfo(parameters, project, gameType)
    }

    fun getContextReferenceInfoForDefinition(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        var contextConfig: CwtPropertyConfig? = null
        var contextReferenceElement: ParadoxScriptProperty? = null
        var completionOffset = -1
        when (from) {
            // extraArgs: config, completionOffset?
            ParadoxParameterContextReferenceInfo.From.Argument -> {
                val config = extraArgs.getOrNull(0)?.castOrNull<CwtMemberConfig<*>>() ?: return null
                completionOffset = extraArgs.getOrNull(1)?.castOrNull<Int>() ?: -1
                // infer context config
                contextConfig = selectConfigScope { config.asProperty()?.parentConfig.asProperty() } ?: return null
                if (contextConfig.configExpression.type != CwtDataTypes.Definition) return null
                contextReferenceElement = selectScope { element.queryParentBy("*/*").asProperty() } ?: return null
            }
            // extraArgs: contextConfig
            ParadoxParameterContextReferenceInfo.From.ContextReference -> {
                contextConfig = extraArgs.getOrNull(0)?.castOrNull<CwtPropertyConfig>() ?: return null
                if (contextConfig.configExpression.type != CwtDataTypes.Definition) return null
                contextReferenceElement = element.castOrNull() ?: return null
            }
            // extraArgs: offset?
            ParadoxParameterContextReferenceInfo.From.InContextReference -> {
                val parentBlock = when (element.elementType) {
                    ParadoxScriptElementTypes.LEFT_BRACE -> element.parent.parentOfType<ParadoxScriptBlock>()
                    else -> element.parentOfType<ParadoxScriptBlock>()
                } ?: return null
                val parentProperties = parentBlock.parentsOfType<ParadoxScriptProperty>(withSelf = false)
                for (prop in parentProperties) {
                    // infer context config
                    val propConfig = ParadoxConfigManager.getConfigs(prop).firstOrNull() as? CwtPropertyConfig ?: continue
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
        val gameType = configGroup.gameType
        val project = configGroup.project
        val definitionName = contextReferenceElement.name.orNull() ?: return null
        if (definitionName.isParameterized()) return null // skip if context name is parameterized
        val definitionTypes = contextConfig.configExpression.metadata.value?.split('.') ?: return null
        val contextName = definitionName
        val contextIcon = ChronicleIcons.Nodes.Definition(definitionTypes[0])
        val contextKey = "${definitionTypes.joinToString(".")}@${definitionName}"
        val contextNameElement = contextReferenceElement.propertyKey
        val arguments = mutableListOf<ParadoxParameterContextReferenceInfo.Argument>()
        contextReferenceElement.block?.properties()?.forEach f@{
            if (completionOffset != -1 && completionOffset in it.textRange) return@f
            val k = it.propertyKey
            val v = it.propertyValue
            val argument = ParadoxParameterContextReferenceInfo.Argument(k.value,
                argumentValue = v?.value,
                argumentNameElementPointer = k.createPointer(project),
                argumentNameRange = k.textRange,
                argumentValueElementPointer = v?.createPointer(project),
                argumentValueRange = v?.textRange,
                project = project,
                gameType = gameType
            )
            arguments += argument
        }
        val info = ParadoxParameterContextReferenceInfo(
            elementPointer = contextReferenceElement.createPointer(project),
            contextName = contextName,
            contextIcon = contextIcon,
            contextKey = contextKey,
            contextNameElementPointer = contextNameElement.createPointer(project),
            contextNameRange = contextNameElement.textRange,
            arguments = arguments,
            project = project,
            gameType = gameType,
        )
        info.definitionName = definitionName
        info.definitionTypes = definitionTypes
        return info
    }

    fun getContextReferenceInfoForInlineScript(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        // NOTE 2.1.0 这里目前不验证游戏类型
        var inlineConfig: CwtMacroConfig? = null
        var contextReferenceElement: ParadoxScriptProperty? = null
        var completionOffset = -1
        when (from) {
            // extraArgs: config, completionOffset?
            ParadoxParameterContextReferenceInfo.From.Argument -> {
                val config = extraArgs.getOrNull(0)?.castOrNull<CwtMemberConfig<*>>() ?: return null
                completionOffset = extraArgs.getOrNull(1)?.castOrNull<Int>() ?: -1
                if (config !is CwtPropertyConfig || config.configExpression.type != CwtDataTypes.Parameter) return null
                // infer inline config
                val contextConfig = selectConfigScope { config.asProperty()?.parentConfig.asProperty() } ?: return null
                inlineConfig = contextConfig.inlineConfig?.takeIf { ParadoxInlineScriptManager.isMatched(it.name) } ?: return null
                contextReferenceElement = selectScope { element.queryParentBy("*/*").asProperty() } ?: return null
            }
            // extraArgs: contextConfig
            ParadoxParameterContextReferenceInfo.From.ContextReference -> {
                val contextConfig = extraArgs.getOrNull(0)?.castOrNull<CwtPropertyConfig>() ?: return null
                inlineConfig = contextConfig.inlineConfig?.takeIf { ParadoxInlineScriptManager.isMatched(it.name) } ?: return null
                contextReferenceElement = element.castOrNull() ?: return null
            }
            // extraArgs: offset?
            ParadoxParameterContextReferenceInfo.From.InContextReference -> {
                val parentBlock = when (element.elementType) {
                    ParadoxScriptElementTypes.LEFT_BRACE -> element.parent.parentOfType<ParadoxScriptBlock>()
                    else -> element.parentOfType<ParadoxScriptBlock>()
                } ?: return null
                val parentProperties = parentBlock.parentsOfType<ParadoxScriptProperty>(withSelf = false)
                for (prop in parentProperties) {
                    // infer context config
                    val propConfig = ParadoxConfigManager.getConfigs(prop).findIsInstance<CwtPropertyConfig>() ?: continue
                    val propInlineConfig = propConfig.inlineConfig?.takeIf { ParadoxInlineScriptManager.isMatched(it.name) } ?: continue
                    if (propInlineConfig.config.configs?.any { it is CwtPropertyConfig && it.configExpression.type == CwtDataTypes.Parameter } != true) continue
                    inlineConfig = propInlineConfig
                    contextReferenceElement = prop
                    break
                }
            }
        }
        if (inlineConfig == null || contextReferenceElement == null) return null
        val configGroup = inlineConfig.configGroup
        val gameType = configGroup.gameType
        val project = configGroup.project
        val inlineScriptExpression = ParadoxInlineScriptService.getInlineScriptExpressionFromUsageElement(contextReferenceElement) ?: return null
        val contextName = inlineScriptExpression.takeIf { !it.isParameterized() } ?: return null
        val contextIcon = ChronicleIcons.Nodes.Macro
        val contextKey = "inline_script@$inlineScriptExpression"
        val contextNameElement = contextReferenceElement.propertyKey
        val arguments = mutableListOf<ParadoxParameterContextReferenceInfo.Argument>()
        contextReferenceElement.block?.properties()?.forEach f@{ p ->
            if (completionOffset != -1 && completionOffset in p.textRange) return@f
            val k = p.propertyKey
            val v = p.propertyValue
            val argument = ParadoxParameterContextReferenceInfo.Argument(
                argumentName = k.value.orNull()?.takeIf { it != "script" } ?: return@f,
                argumentValue = v?.value,
                argumentNameElementPointer = k.createPointer(project),
                argumentNameRange = k.textRange,
                argumentValueElementPointer = v?.createPointer(project),
                argumentValueRange = v?.textRange,
                project = project,
                gameType = gameType,
            )
            arguments += argument
        }
        val info = ParadoxParameterContextReferenceInfo(
            elementPointer = contextReferenceElement.createPointer(project),
            contextName = contextName,
            contextIcon = contextIcon,
            contextKey = contextKey,
            contextNameElementPointer = contextNameElement.createPointer(project),
            contextNameRange = contextNameElement.textRange,
            arguments = arguments,
            project = project,
            gameType = gameType,
        )
        info.inlineScriptExpression = inlineScriptExpression
        return info
    }

    fun getContextReferenceInfoForScriptValueReference(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        var expressionElement: ParadoxScriptStringExpressionElement?
        var expressionString: String?
        var expressionElementConfig: CwtMemberConfig<*>?
        var completionOffset = -1
        when (from) {
            // extraArgs: config, completionOffset?
            ParadoxParameterContextReferenceInfo.From.Argument -> {
                val config = extraArgs.getOrNull(0)?.castOrNull<CwtMemberConfig<*>>() ?: return null
                completionOffset = extraArgs.getOrNull(1)?.castOrNull<Int>() ?: -1
                expressionElement = when (element) {
                    is ParadoxScriptProperty -> element.propertyKey
                    is ParadoxScriptStringExpressionElement -> element
                    else -> return null
                }
                expressionString = expressionElement.value
                if (!expressionString.contains("value:")) return null // 快速判断
                expressionElementConfig = config
            }
            // extraArgs: contextConfig
            ParadoxParameterContextReferenceInfo.From.ContextReference -> {
                val contextConfig = extraArgs.getOrNull(0)?.castOrNull<CwtMemberConfig<*>>() ?: return null
                expressionElement = when (element) {
                    is ParadoxScriptProperty -> element.propertyKey
                    is ParadoxScriptStringExpressionElement -> element
                    else -> return null
                }
                expressionString = expressionElement.value
                if (!expressionString.contains("value:")) return null // 快速判断
                expressionElementConfig = contextConfig
            }
            // extraArgs: offset?
            ParadoxParameterContextReferenceInfo.From.InContextReference -> {
                val offset = extraArgs.getOrNull(0)?.castOrNull<Int>() ?: -1
                expressionElement = element.parentOfType<ParadoxScriptStringExpressionElement>(withSelf = true) ?: return null
                expressionString = expressionElement.value
                if (!expressionString.contains("value:")) return null // 快速判断
                val pipeIndex = expressionString.indexOf('|', expressionString.indexOf("value:").let { if (it != -1) it + 6 else return null })
                if (pipeIndex == -1) return null
                if (offset != -1 && pipeIndex >= offset - expressionElement.startOffset) return null // 要求光标在管道符之后（如果offset不为-1）
                expressionElementConfig = ParadoxConfigManager.getConfigs(expressionElement).firstOrNull() ?: return null
            }
        }
        if (expressionElementConfig.configExpression.type !in CwtDataTypeSets.ValueField) return null
        val configGroup = expressionElementConfig.configGroup
        val gameType = configGroup.gameType
        val project = configGroup.project
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(expressionString, null, configGroup) ?: return null
        val scriptValueReferenceExpression = valueFieldExpression.fieldNode.asDynamic()?.valueNode?.dataSourceNode?.castOrNull<ParadoxScriptValueReferenceExpression>() ?: return null
        val scriptValueNode = scriptValueReferenceExpression.scriptValueNode
        val definitionName = scriptValueNode.text
        if (definitionName.isParameterized()) return null // skip if context name is parameterized
        val definitionTypes = listOf(ParadoxDefinitionTypes.scriptValue)
        val contextName = definitionName
        val contextIcon = ChronicleIcons.Nodes.Definition(definitionTypes[0])
        val contextKey = "script_value@${definitionName}"
        val offset = ParadoxExpressionService.getExpressionOffset(expressionElement)
        val startOffset = element.startOffset + offset
        val contextNameRange = scriptValueReferenceExpression.scriptValueNode.rangeInExpression.shiftRight(startOffset) // text range of script value name
        val arguments = mutableListOf<ParadoxParameterContextReferenceInfo.Argument>()
        val pointer = expressionElement.createPointer(project)
        val expressionStartOffset = expressionElement.startOffset + offset
        scriptValueReferenceExpression.argumentNodes.forEach f@{ (nameNode, valueNode) ->
            if (completionOffset != -1 && completionOffset in nameNode.rangeInExpression.shiftRight(expressionStartOffset)) return@f
            val argument = ParadoxParameterContextReferenceInfo.Argument(
                argumentName = nameNode.text,
                argumentValue = valueNode?.text,
                argumentNameElementPointer = pointer,
                argumentNameRange = nameNode.rangeInExpression.shiftRight(startOffset),
                argumentValueElementPointer = pointer,
                argumentValueRange = valueNode?.rangeInExpression?.shiftRight(startOffset),
                project = project,
                gameType = gameType,
            )
            arguments += argument
        }
        val info = ParadoxParameterContextReferenceInfo(
            elementPointer = pointer,
            contextName = contextName,
            contextIcon = contextIcon,
            contextKey = contextKey,
            contextNameElementPointer = pointer,
            contextNameRange = contextNameRange,
            arguments = arguments,
            project = project,
            gameType = gameType,
        )
        info.definitionName = definitionName
        info.definitionTypes = definitionTypes
        return info
    }
}
