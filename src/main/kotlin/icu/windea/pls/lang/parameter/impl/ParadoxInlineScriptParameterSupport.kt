package icu.windea.pls.lang.parameter.impl

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

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
                if(config !is CwtPropertyConfig || config.expression.type != CwtDataType.Parameter) return null
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
                    val propConfig = ParadoxConfigHandler.getConfigs(prop).firstOrNull() ?: continue
                    val propInlineConfig = propConfig.inlineableConfig?.castOrNull<CwtInlineConfig>()?.takeIf { it.name == ParadoxInlineScriptHandler.inlineScriptKey } ?: continue
                    if(propInlineConfig.config.configs?.any { it is CwtPropertyConfig && it.expression.type == CwtDataType.Parameter } != true) continue
                    inlineConfig = propInlineConfig
                    contextReferenceElement = prop
                    break
                }
            }
        }
        if(inlineConfig == null || contextReferenceElement == null) return null
        val configGroup = inlineConfig.info.configGroup
        val gameType = configGroup.gameType ?: return null
        val project = configGroup.project
        val propertyValue = contextReferenceElement.propertyValue ?: return null
        val expression = ParadoxInlineScriptHandler.getExpressionFromInlineConfig(propertyValue, inlineConfig) ?: return null
        if(expression.isParameterized()) return null //skip if context name is parameterized
        val contextName = expression
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
        val info = ParadoxParameterContextReferenceInfo(contextReferenceElement.createPointer(project), contextName, contextNameElement.createPointer(project), contextNameElement.textRange, arguments, gameType, project)
        info.putUserData(ParadoxParameterSupport.Keys.inlineScriptExpression, expression)
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
        val contextIcon = PlsIcons.InlineScript
        val contextKey = "inline_script@$expression"
        val rangeInParent = TextRange.create(0, element.textLength)
        val readWriteAccess = ParadoxParameterHandler.getReadWriteAccess(element)
        val gameType = selectGameType(context) ?: return null
        val project = context.project
        val result = ParadoxParameterElement(element, name, contextName, contextIcon, contextKey, rangeInParent, readWriteAccess, gameType, project)
        result.putUserData(ParadoxParameterSupport.Keys.containingContext, context.createPointer(project))
        result.putUserData(ParadoxParameterSupport.Keys.inlineScriptExpression, expression)
        result.putUserData(ParadoxParameterSupport.Keys.support, this)
        return result
    }
    
    override fun resolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>): ParadoxParameterElement? {
        if(config !is CwtPropertyConfig || config.expression.type != CwtDataType.Parameter) return null
        return doResolveArgument(element, rangeInElement, config)
    }
    
    private fun doResolveArgument(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtPropertyConfig): ParadoxParameterElement? {
        val contextConfig = config.castOrNull<CwtPropertyConfig>()?.parentConfig?.castOrNull<CwtPropertyConfig>() ?: return null
        val inlineConfig = contextConfig.inlineableConfig?.castOrNull<CwtInlineConfig>()?.takeIf { it.name == ParadoxInlineScriptHandler.inlineScriptKey } ?: return null
        val contextReferenceElement = element.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return null
        val propertyValue = contextReferenceElement.propertyValue ?: return null
        val expression = ParadoxInlineScriptHandler.getExpressionFromInlineConfig(propertyValue, inlineConfig) ?: return null
        if(expression.isParameterized()) return null //skip if context name is parameterized
        val name = element.name
        val contextName = expression
        val contextIcon = PlsIcons.InlineScript
        val contextKey = "inline_script@$expression"
        val rangeInParent = rangeInElement ?: TextRange.create(0, element.textLength)
        val readWriteAccess = ReadWriteAccessDetector.Access.Write
        val gameType = config.info.configGroup.gameType ?: return null
        val project = config.info.configGroup.project
        val result = ParadoxParameterElement(element, name, contextName, contextIcon, contextKey, rangeInParent, readWriteAccess, gameType, project)
        result.putUserData(ParadoxParameterSupport.Keys.inlineScriptExpression, expression)
        result.putUserData(ParadoxParameterSupport.Keys.support, this)
        return result
    }
    
    override fun processContext(parameterElement: ParadoxParameterElement, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val expression = parameterElement.getUserData(ParadoxParameterSupport.Keys.inlineScriptExpression) ?: return false
        if(expression.isParameterized()) return false //skip if context name is parameterized
        val project = parameterElement.project
        ParadoxInlineScriptHandler.processInlineScriptFile(expression, parameterElement, project, onlyMostRelevant, processor)
        return true
    }
    
    override fun processContext(element: PsiElement, contextReferenceInfo: ParadoxParameterContextReferenceInfo, onlyMostRelevant: Boolean, processor: (ParadoxScriptDefinitionElement) -> Boolean): Boolean {
        val expression = contextReferenceInfo.getUserData(ParadoxParameterSupport.Keys.inlineScriptExpression) ?: return false
        if(expression.isParameterized()) return false //skip if context name is parameterized
        val project = contextReferenceInfo.project
        ParadoxInlineScriptHandler.processInlineScriptFile(expression, element, project, onlyMostRelevant, processor)
        return true
    }
    
    override fun buildDocumentationDefinition(parameterElement: ParadoxParameterElement, builder: StringBuilder): Boolean = with(builder) {
        val inlineScriptExpression = parameterElement.getUserData(ParadoxParameterSupport.Keys.inlineScriptExpression) ?: return false
        if(inlineScriptExpression.isEmpty()) return false
        val filePath = ParadoxInlineScriptHandler.getInlineScriptFilePath(inlineScriptExpression) ?: return false
        
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
        //加上所属内联脚本信息
        val gameType = parameterElement.gameType
        appendBr().appendIndent()
        append(PlsBundle.message("ofInlineScript")).append(" ")
        appendFilePathLink(gameType, filePath, inlineScriptExpression, parameterElement)
        return true
    }
    
    override fun getModificationTracker(parameterElement: ParadoxParameterElement): ModificationTracker {
        return ParadoxPsiModificationTracker.getInstance(parameterElement.project).InlineScriptsTracker
    }
}