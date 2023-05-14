package icu.windea.pls.lang.expression.impl

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScriptParameterExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.Parameter
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.ARGUMENT_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxConfigHandler.highlightScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        //尝试解析为参数名（仅限key）
        if(element !is ParadoxScriptPropertyKey || config !is CwtPropertyConfig || isKey != true) return null
        return ParadoxParameterSupport.resolveArgument(element, rangeInElement, config)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(!context.quoted && context.keyword.isParameterized()) return //排除可能带参数的情况
        
        val config = context.config ?: return
        //提示参数名（仅限key）
        val contextElement = context.contextElement
        val isKey = context.isKey
        if(isKey != true || config !is CwtPropertyConfig) return
        return ParadoxParameterHandler.completeArguments(contextElement, context, result)
    }
}

class ParadoxScriptParameterValueExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.ParameterValue
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(!getSettings().inference.argumentValueConfig) return
        if(element !is ParadoxScriptValue || config !is CwtValueConfig) return
        val propertyKey = element.propertyKey ?: return
        val propertyConfig = config.propertyConfig ?: return
        val parameterElement = ParadoxParameterSupport.resolveArgument(propertyKey, null, propertyConfig) ?: return
        ProgressManager.checkCanceled()
        val inferredConfig = ParadoxParameterHandler.inferEntireConfig(parameterElement) ?: return
        val range = rangeInElement?.shiftRight(element.startOffset) ?: element.textRange
        INSTANCE.annotate(element, rangeInElement, expression, holder, inferredConfig)
        
        //create tooltip
        val inferredConfigExpression = inferredConfig.expression.expressionString
        val tooltip = PlsBundle.message("inferred.config.expression", inferredConfigExpression.escapeXml())
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).tooltip(tooltip).create()
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        if(!getSettings().inference.argumentValueConfig) return null
        if(element !is ParadoxScriptValue || config !is CwtValueConfig || isKey != false) return null
        val propertyKey = element.propertyKey ?: return null
        val propertyConfig = config.propertyConfig ?: return null
        val parameterElement = ParadoxParameterSupport.resolveArgument(propertyKey, null, propertyConfig) ?: return null
        ProgressManager.checkCanceled()
        val inferredConfig = ParadoxParameterHandler.inferEntireConfig(parameterElement) ?: return null
        val inferredConfigExpression = inferredConfig.expression
        val configGroup = inferredConfig.info.configGroup
        ProgressManager.checkCanceled()
        return ParadoxConfigHandler.resolveScriptExpression(element, rangeInElement, inferredConfig, inferredConfigExpression, configGroup, isKey, exact)
    }
    
    override fun multiResolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Collection<PsiElement> {
        if(!getSettings().inference.argumentValueConfig) return emptySet()
        if(element !is ParadoxScriptValue || config !is CwtValueConfig || isKey != false) return emptySet()
        val propertyKey = element.propertyKey ?: return emptySet()
        val propertyConfig = config.propertyConfig ?: return emptySet()
        val parameterElement = ParadoxParameterSupport.resolveArgument(propertyKey, null, propertyConfig) ?: return emptySet()
        val inferredConfig = ParadoxParameterHandler.inferEntireConfig(parameterElement) ?: return emptySet()
        val inferredConfigExpression = inferredConfig.expression
        val configGroup = inferredConfig.info.configGroup
        ProgressManager.checkCanceled()
        return ParadoxConfigHandler.multiResolveScriptExpression(element, rangeInElement, inferredConfig, inferredConfigExpression, configGroup, isKey)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) = with(context) {
        if(!getSettings().inference.argumentValueConfig) return
        val element = contextElement
        val config = config
        val configs = configs
        val key = isKey
        if(element !is ParadoxScriptValue || config !is CwtValueConfig || key != false) return
        val propertyKey = element.propertyKey ?: return
        val propertyConfig = config.propertyConfig ?: return
        val parameterElement = ParadoxParameterSupport.resolveArgument(propertyKey, null, propertyConfig) ?: return
        ProgressManager.checkCanceled()
        val inferredConfig = ParadoxParameterHandler.inferEntireConfig(parameterElement) ?: return
        context.put(PlsCompletionKeys.configKey, inferredConfig)
        context.put(PlsCompletionKeys.configsKey, null)
        ParadoxConfigHandler.completeScriptExpression(context, result)
        context.put(PlsCompletionKeys.configKey, config)
        context.put(PlsCompletionKeys.configsKey, configs)
    }
}

class ParadoxScriptLocalisationParameterExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataType.LocalisationParameter
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.ARGUMENT_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        ParadoxConfigHandler.highlightScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        //尝试解析为本地化参数名（仅限key）
        if(isKey != true || config !is CwtPropertyConfig) return null
        return ParadoxLocalisationParameterSupport.resolveArgument(element, rangeInElement, config)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        //NOTE 不兼容本地化参数（CwtDataType.LocalisationParameter），因为那个引用也可能实际上对应一个缺失的本地化的名字
    }
}