package icu.windea.pls.lang.expression.impl

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

//提供对复杂表达式的高级语言功能支持

class ParadoxScriptValueSetExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type?.isValueSetValueType() == true
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        //not key/value or quoted -> only value set value name, no scope info
        if(config !is CwtDataConfig<*> || expression.isLeftQuoted()) {
            val valueSetName = config.expression?.value ?: return
            val attributesKey = when(valueSetName) {
                "variable" -> ParadoxScriptAttributesKeys.VARIABLE_KEY
                else -> ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
            }
            ParadoxConfigHandler.annotateScriptExpression(element, element.textRange.unquote(expression), attributesKey, holder)
            return
        }
        val configGroup = config.info.configGroup
        val isKey = if(rangeInElement != null) null else element is ParadoxScriptPropertyKey
        val textRange = rangeInElement ?: TextRange.create(0, expression.length)
        val valueSetValueExpression = ParadoxValueSetValueExpression.resolve(expression, textRange, config, configGroup, isKey) ?: return
        ParadoxConfigHandler.annotateComplexExpression(element, valueSetValueExpression, holder, config)
    }
    
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        //参见：ParadoxValueSetValueExpression
        val configExpression = config.expression ?: return null
        val configGroup = config.info.configGroup
        val name = expression
        val predefinedResolved = ParadoxConfigHandler.resolvePredefinedValueSetValue(name, configExpression, configGroup)
        if(predefinedResolved != null) return predefinedResolved
        return ParadoxValueSetValueHandler.resolveValueSetValue(element, name, configExpression, configGroup)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        //not key/value or quoted -> only value set value name, no scope info
        if(context.config !is CwtDataConfig<*> || context.quoted) {
            ParadoxConfigHandler.completeValueSetValue(context, result)
            return
        }
        ParadoxConfigHandler.completeValueSetValueExpression(context, result)
    }
}

class ParadoxScriptScopeFieldExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type?.isScopeFieldType() == true
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(expression.isLeftQuoted()) return
        val configGroup = config.info.configGroup
        val isKey = if(rangeInElement != null) null else element is ParadoxScriptPropertyKey
        val textRange = rangeInElement ?: TextRange.create(0, expression.length)
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expression, textRange, configGroup, isKey) ?: return
        ParadoxConfigHandler.annotateComplexExpression(element, scopeFieldExpression, holder, config)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        //不在这里处理，参见：ParadoxScopeFieldExpression
        return null
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val configExpression = context.config?.expression ?: return
        when(configExpression.type) {
            CwtDataType.Scope -> {
                context.put(PlsCompletionKeys.scopeNameKey, configExpression.value)
            }
            CwtDataType.ScopeGroup -> {
                context.put(PlsCompletionKeys.scopeGroupNameKey, configExpression.value)
            }
            else -> {}
        }
        ParadoxConfigHandler.completeScopeFieldExpression(context, result)
        when(configExpression.type) {
            CwtDataType.Scope -> {
                context.put(PlsCompletionKeys.scopeNameKey, null)
            }
            CwtDataType.ScopeGroup -> {
                context.put(PlsCompletionKeys.scopeGroupNameKey, null)
            }
            else -> {}
        }
    }
}

class ParadoxScriptValueFieldExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type?.isValueFieldType() == true
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(expression.isLeftQuoted()) return
        val configGroup = config.info.configGroup
        val isKey = if(rangeInElement != null) null else element is ParadoxScriptPropertyKey
        val textRange = rangeInElement ?: TextRange.create(0, expression.length)
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression, textRange, configGroup, isKey) ?: return
        ParadoxConfigHandler.annotateComplexExpression(element, valueFieldExpression, holder, config)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        //不在这里处理，参见：ParadoxValueFieldExpression
        return null
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val configExpression = context.config?.expression ?: return
        when(configExpression.type) {
            CwtDataType.IntValueField -> {
                context.put(PlsCompletionKeys.isIntKey, true)
            }
            else -> {}
        }
        ParadoxConfigHandler.completeValueFieldExpression(context, result)
        when(configExpression.type) {
            CwtDataType.IntValueField -> {
                context.put(PlsCompletionKeys.isIntKey, null)
            }
            else -> {}
        }
    }
}

class ParadoxScriptVariableFieldExpressionSupport : ParadoxScriptExpressionSupport() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type?.isVariableFieldType() == true
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(expression.isLeftQuoted()) return
        val configGroup = config.info.configGroup
        val isKey = if(rangeInElement != null) null else element is ParadoxScriptPropertyKey
        val textRange = rangeInElement ?: TextRange.create(0, expression.length)
        val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression, textRange, configGroup, isKey) ?: return
        ParadoxConfigHandler.annotateComplexExpression(element, variableFieldExpression, holder, config)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        //不在这里处理，参见：ParadoxVariableFieldExpression
        return null
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val configExpression = context.config?.expression ?: return
        when(configExpression.type) {
            CwtDataType.IntVariableField -> {
                context.put(PlsCompletionKeys.isIntKey, true)
            }
            else -> {}
        }
        ParadoxConfigHandler.completeVariableFieldExpression(context, result)
        when(configExpression.type) {
            CwtDataType.IntVariableField -> {
                context.put(PlsCompletionKeys.isIntKey, null)
            }
            else -> {}
        }
    }
}