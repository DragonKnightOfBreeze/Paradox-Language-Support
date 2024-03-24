package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*

class ParadoxDynamicValueExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type in CwtDataTypeGroups.DynamicValue
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        //not key/value or quoted -> only dynamic value name, no scope info
        if(config !is CwtMemberConfig<*> || expression.isLeftQuoted()) {
            val dynamicValueType = config.expression?.value ?: return
            val attributesKey = when(dynamicValueType) {
                "variable" -> ParadoxScriptAttributesKeys.VARIABLE_KEY
                else -> ParadoxScriptAttributesKeys.DYNAMIC_VALUE_KEY
            }
            CwtConfigHandler.annotateScriptExpression(element, element.textRange.unquote(expression), attributesKey, holder)
            return
        }
        val configGroup = config.info.configGroup
        val range = rangeInElement ?: TextRange.create(0, expression.length)
        val dynamicValueExpression = ParadoxDynamicValueExpression.resolve(expression, range, configGroup, config) ?: return
        CwtConfigHandler.annotateComplexExpression(element, dynamicValueExpression, holder, config)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        val configExpression = config.expression ?: return null
        val configGroup = config.info.configGroup
        val name = expression
        return ParadoxDynamicValueHandler.resolveDynamicValue(element, name, configExpression, configGroup)
    }
    
    override fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.info.configGroup
        val range = TextRange.create(0, expression.length).unquote(expression)
        if(expression.isLeftQuoted()) {
            //quoted -> only dynamic value name, no scope info
            val reference = ParadoxScriptExpressionPsiReference(element, range, config, isKey)
            return arrayOf(reference)
        }
        val dynamicValueExpression = ParadoxDynamicValueExpression.resolve(expression, range, configGroup, config)
        if(dynamicValueExpression == null) return PsiReference.EMPTY_ARRAY
        return dynamicValueExpression.getReferences(element)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        //not key/value or quoted -> only dynamic value name, no scope info
        if(context.config !is CwtMemberConfig<*> || context.quoted) {
            CwtConfigHandler.completeDynamicValue(context, result)
            return
        }
        CwtConfigHandler.completeDynamicValueExpression(context, result)
    }
}

class ParadoxScopeFieldExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type in CwtDataTypeGroups.ScopeField
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(element !is ParadoxScriptStringExpressionElement) return
        if(expression.isLeftQuoted()) return
        val configGroup = config.info.configGroup
        val range = rangeInElement ?: TextRange.create(0, expression.length)
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expression, range, configGroup) ?: return
        CwtConfigHandler.annotateComplexExpression(element, scopeFieldExpression, holder, config)
    }
    
    override fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        if(expression.isLeftQuoted()) return PsiReference.EMPTY_ARRAY
        val configGroup = config.info.configGroup
        val range = TextRange.create(0, expression.length).unquote(expression)
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expression, range, configGroup)
        if(scopeFieldExpression == null) return PsiReference.EMPTY_ARRAY
        return scopeFieldExpression.getReferences(element)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val configExpression = context.config?.expression ?: return
        when(configExpression.type) {
            CwtDataTypes.Scope -> {
                context.scopeName = configExpression.value
            }
            CwtDataTypes.ScopeGroup -> {
                context.scopeGroupName = configExpression.value
            }
            else -> {}
        }
        CwtConfigHandler.completeScopeFieldExpression(context, result)
        when(configExpression.type) {
            CwtDataTypes.Scope -> {
                context.scopeName = null
            }
            CwtDataTypes.ScopeGroup -> {
                context.scopeGroupName = null
            }
            else -> {}
        }
    }
}

class ParadoxValueFieldExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type in CwtDataTypeGroups.ValueField
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(expression.isLeftQuoted()) return
        val configGroup = config.info.configGroup
        val textRange = rangeInElement ?: TextRange.create(0, expression.length)
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression, textRange, configGroup) ?: return
        CwtConfigHandler.annotateComplexExpression(element, valueFieldExpression, holder, config)
    }
    
    override fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        if(expression.isLeftQuoted()) return PsiReference.EMPTY_ARRAY
        val configGroup = config.info.configGroup
        val range = TextRange.create(0, expression.length).unquote(expression)
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression, range, configGroup)
        if(valueFieldExpression == null) return PsiReference.EMPTY_ARRAY
        return valueFieldExpression.getReferences(element)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val configExpression = context.config?.expression ?: return
        when(configExpression.type) {
            CwtDataTypes.IntValueField -> {
                context.isInt = true
            }
            else -> {}
        }
        CwtConfigHandler.completeValueFieldExpression(context, result)
        when(configExpression.type) {
            CwtDataTypes.IntValueField -> {
                context.isInt = null
            }
            else -> {}
        }
    }
}

class ParadoxVariableFieldExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type in CwtDataTypeGroups.VariableField
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(element !is ParadoxScriptStringExpressionElement) return
        if(expression.isLeftQuoted()) return
        val configGroup = config.info.configGroup
        val range = rangeInElement ?: TextRange.create(0, expression.length)
        val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression, range, configGroup) ?: return
        CwtConfigHandler.annotateComplexExpression(element, variableFieldExpression, holder, config)
    }
    
    override fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        if(expression.isLeftQuoted()) return PsiReference.EMPTY_ARRAY
        val configGroup = config.info.configGroup
        val range = TextRange.create(0, expression.length).unquote(expression)
        val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression, range, configGroup)
        if(variableFieldExpression == null) return PsiReference.EMPTY_ARRAY
        return variableFieldExpression.getReferences(element)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val configExpression = context.config?.expression ?: return
        when(configExpression.type) {
            CwtDataTypes.IntVariableField -> {
                context.isInt = true
            }
            else -> {}
        }
        CwtConfigHandler.completeVariableFieldExpression(context, result)
        when(configExpression.type) {
            CwtDataTypes.IntVariableField -> {
                context.isInt = null
            }
            else -> {}
        }
    }
}
