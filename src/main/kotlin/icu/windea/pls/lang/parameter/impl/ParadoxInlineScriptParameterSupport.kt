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
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.psi.*

open class ParadoxInlineScriptParameterSupport : ParadoxParameterSupport {
    companion object {
        @JvmField val containingContextKey = Key.create<SmartPsiElementPointer<ParadoxScriptDefinitionElement>>("paradox.parameterElement.containingContext")
        @JvmField val containingContextReferenceKey = Key.create<SmartPsiElementPointer<ParadoxScriptDefinitionElement>>("paradox.parameterElement.contextReference")
        @JvmField val inlineScriptExpressionKey = Key.create<String>("paradox.parameterElement.inlineScriptExpression")
    }
    
    override fun isContext(element: ParadoxScriptDefinitionElement): Boolean {
        if(element !is ParadoxScriptFile) return false
        return ParadoxInlineScriptHandler.getInlineScriptExpression(element) != null
    }
    
    override fun findContext(element: PsiElement): ParadoxScriptDefinitionElement? {
        val context = element.containingFile?.castOrNull<ParadoxScriptFile>()
        return context?.takeIf { isContext(it) }
    }
    
    override fun getContextReferenceInfo(element: PsiElement, from: ParadoxParameterContextReferenceInfo.From, vararg extraArgs: Any?): ParadoxParameterContextReferenceInfo? {
        var inlineConfig: CwtInlineConfig? = null
        var contextReferenceElement: ParadoxScriptProperty? = null
        var completionOffset = -1
        when(from) {
            //extraArgs: config, completionOffset
            ParadoxParameterContextReferenceInfo.From.Argument -> {
                val config = extraArgs.getOrNull(0)?.castOrNull<CwtMemberConfig<*>>() ?: return null
                completionOffset = extraArgs.getOrNull(1)?.castOrNull<Int>() ?: -1
                if(config !is CwtPropertyConfig || config.expression.type != CwtDataType.Parameter) return null
                //infer inline config
                val contextConfig = config.castOrNull<CwtPropertyConfig>()?.parent?.castOrNull<CwtPropertyConfig>() ?: return null
                inlineConfig = contextConfig.inlineableConfig?.castOrNull<CwtInlineConfig>()?.takeIf { it.name == ParadoxInlineScriptHandler.inlineScriptName } ?: return null
                contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
            }
            //extraArgs: contextConfig
            ParadoxParameterContextReferenceInfo.From.ContextReference -> {
                val contextConfig = extraArgs.getOrNull(0)?.castOrNull<CwtPropertyConfig>() ?: return null
                inlineConfig = contextConfig.inlineableConfig?.castOrNull<CwtInlineConfig>()?.takeIf { it.name == ParadoxInlineScriptHandler.inlineScriptName } ?: return null
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
                    val propConfig = ParadoxConfigResolver.getPropertyConfigs(prop, false, true, ParadoxConfigMatcher.Options.Default).firstOrNull() ?: continue
                    val propInlineConfig = propConfig.inlineableConfig?.castOrNull<CwtInlineConfig>()?.takeIf { it.name == ParadoxInlineScriptHandler.inlineScriptName } ?: continue
                    if(propInlineConfig.config.configs?.any { it is CwtPropertyConfig && it.expression.type == CwtDataType.Parameter } != true) continue
                    inlineConfig = propInlineConfig
                    contextReferenceElement = prop
                    break
                }
            }
        }
        if(inlineConfig == null || contextReferenceElement == null) return null
        val rangeInElement = contextReferenceElement.propertyKey.textRangeInParent
        val propertyValue = contextReferenceElement.propertyValue ?: return null
        val expression = ParadoxInlineScriptHandler.getExpressionFromInlineConfig(propertyValue, inlineConfig) ?: return null
        if(expression.isParameterized()) return null //skip if context name is parameterized
        val argumentNames = mutableSetOf<String>()
        contextReferenceElement.block?.processProperty p@{
            if(completionOffset != -1 && completionOffset in it.textRange) return@p true
            val argumentName = it.propertyKey.name
            if(argumentName == "script") return@p true //hardcoded
            argumentNames.add(argumentName)
            true
        }
        val gameType = inlineConfig.info.configGroup.gameType ?: return null
        val project = inlineConfig.info.configGroup.project
        val info = ParadoxParameterContextReferenceInfo(contextReferenceElement.createPointer(), rangeInElement, expression, argumentNames, gameType, project)
        info.putUserData(inlineScriptExpressionKey, expression)
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
        val context = findContext(element) as? ParadoxScriptFile ?: return null
        val expression = ParadoxInlineScriptHandler.getInlineScriptExpression(context) ?: return null
        val readWriteAccess = getReadWriteAccess(element)
        val contextKey = "inline_script@$expression"
        val gameType = selectGameType(context) ?: return null
        val project = context.project
        val result = ParadoxParameterElement(element, name, context.name, contextKey, readWriteAccess, gameType, project)
        result.putUserData(containingContextKey, context.createPointer())
        result.putUserData(inlineScriptExpressionKey, expression)
        result.putUserData(ParadoxParameterHandler.supportKey, this)
        return result
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, vararg extraArgs: Any?): ParadoxParameterElement? {
        //extraArgs: config
        val config = extraArgs.getOrNull(0)?.castOrNull<CwtMemberConfig<*>>() ?: return null
        if(config !is CwtPropertyConfig || config.expression.type != CwtDataType.Parameter) return null
        //infer inline config
        val contextConfig = config.castOrNull<CwtPropertyConfig>()?.parent?.castOrNull<CwtPropertyConfig>() ?: return null
        val inlineConfig = contextConfig.inlineableConfig?.castOrNull<CwtInlineConfig>()?.takeIf { it.name == ParadoxInlineScriptHandler.inlineScriptName } ?: return null
        val contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
        val propertyValue = contextReferenceElement.propertyValue ?: return null
        val expression = ParadoxInlineScriptHandler.getExpressionFromInlineConfig(propertyValue, inlineConfig) ?: return null
        if(expression.isParameterized()) return null //skip if context name is parameterized
        val name = element.name
        val readWriteAccess = ReadWriteAccessDetector.Access.Write
        val contextKey = "inline_script@$expression"
        val gameType = config.info.configGroup.gameType ?: return null
        val project = config.info.configGroup.project
        val result = ParadoxParameterElement(element, name, expression, contextKey, readWriteAccess, gameType, project)
        result.putUserData(inlineScriptExpressionKey, expression)
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
        val expression = element.getUserData(inlineScriptExpressionKey) ?: return false
        if(expression.isParameterized()) return false //skip if context name is parameterized
        val project = element.project
        ParadoxInlineScriptHandler.processInlineScript(expression, element, project, onlyMostRelevant, processor)
        return true
    }
    
    override fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val expression = contextReferenceInfo.getUserData(inlineScriptExpressionKey) ?: return false
        if(expression.isParameterized()) return false //skip if context name is parameterized
        val project = contextReferenceInfo.project
        ParadoxInlineScriptHandler.processInlineScript(expression, element, project, onlyMostRelevant, processor)
        return true
    }
    
    override fun getModificationTracker(parameterElement: ParadoxParameterElement): ModificationTracker {
        return ParadoxPsiModificationTracker.getInstance(parameterElement.project).InlineScriptsTracker
    }
    
    override fun buildDocumentationDefinition(element: ParadoxParameterElement, builder: StringBuilder): Boolean = with(builder) {
        val inlineScriptExpression = element.getUserData(inlineScriptExpressionKey) ?: return false
        if(inlineScriptExpression.isEmpty()) return false
        val filePath = ParadoxInlineScriptHandler.getInlineScriptFilePath(inlineScriptExpression) ?: return false
        
        //不加上文件信息
        
        //加上名字
        val name = element.name
        append(PlsBundle.message("prefix.parameter")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        //加上推断得到的规则信息
        val inferredConfig = ParadoxParameterHandler.inferConfig(element)
        if(inferredConfig != null) {
            append(": ")
            append(inferredConfig.expression.expressionString.escapeXml())
        }
        //加上所属内联脚本信息
        val gameType = element.gameType
        appendBr().appendIndent()
        append(PlsBundle.message("ofInlineScript")).append(" ")
        appendFilePathLink(gameType, filePath, inlineScriptExpression, element)
        return true
    }
}