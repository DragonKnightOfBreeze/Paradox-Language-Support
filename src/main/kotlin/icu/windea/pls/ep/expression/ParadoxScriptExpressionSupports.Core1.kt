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
        if(element !is ParadoxScriptStringExpressionElement) return
        //not key/value or quoted -> only dynamic value name, no scope info
        if(config !is CwtMemberConfig<*> || element.text.isLeftQuoted()) {
            val dynamicValueType = config.expression?.value ?: return
            val attributesKey = when(dynamicValueType) {
                "variable" -> ParadoxScriptAttributesKeys.VARIABLE_KEY
                else -> ParadoxScriptAttributesKeys.DYNAMIC_VALUE_KEY
            }
            ParadoxExpressionHandler.annotateExpression(element, element.textRange.unquote(element.text), attributesKey, holder)
            return
        }
        val configGroup = config.configGroup
        val range = TextRange.create(0, expression.length)
        val dynamicValueExpression = ParadoxDynamicValueExpression.resolve(expression, range, configGroup, config) ?: return
        ParadoxExpressionHandler.annotateComplexExpression(element, dynamicValueExpression, holder, config)
    }
    
    override fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        if(element.text.isLeftQuoted()) {
            //quoted -> only dynamic value name, no scope info
            val rangeInElement0 = rangeInElement ?: ParadoxExpressionHandler.getExpressionTextRange(element)
            val reference = ParadoxScriptExpressionPsiReference(element, rangeInElement0, config, isKey)
            return arrayOf(reference)
        }
        val range = TextRange.create(0, expression.length)
        val dynamicValueExpression = ParadoxDynamicValueExpression.resolve(expression, range, configGroup, config)
        if(dynamicValueExpression == null) return PsiReference.EMPTY_ARRAY
        return dynamicValueExpression.getReferences(element)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        //not key/value or quoted -> only dynamic value name, no scope info
        if(context.config !is CwtMemberConfig<*> || context.quoted) {
            ParadoxCompletionManager.completeDynamicValue(context, result)
            return
        }
        ParadoxCompletionManager.completeDynamicValueExpression(context, result)
    }
}

class ParadoxScopeFieldExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type in CwtDataTypeGroups.ScopeField
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expression.length)
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expression, range, configGroup) ?: return
        ParadoxExpressionHandler.annotateComplexExpression(element, scopeFieldExpression, holder, config)
    }
    
    override fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expression.length)
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
        ParadoxCompletionManager.completeScopeFieldExpression(context, result)
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
        if(element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expression.length)
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression, range, configGroup) ?: return
        ParadoxExpressionHandler.annotateComplexExpression(element, valueFieldExpression, holder, config)
    }
    
    override fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expression.length)
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
        ParadoxCompletionManager.completeValueFieldExpression(context, result)
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
        val configGroup = config.configGroup
        val range = TextRange.create(0, expression.length)
        val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression, range, configGroup) ?: return
        ParadoxExpressionHandler.annotateComplexExpression(element, variableFieldExpression, holder, config)
    }
    
    override fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expression.length)
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
        ParadoxCompletionManager.completeVariableFieldExpression(context, result)
        when(configExpression.type) {
            CwtDataTypes.IntVariableField -> {
                context.isInt = null
            }
            else -> {}
        }
    }
}

class ParadoxDatabaseObjectExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type in CwtDataTypeGroups.DatabaseObject
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expression.length)
        val databaseObjectExpression = ParadoxDatabaseObjectExpression.resolve(expression, range, configGroup) ?: return
        ParadoxExpressionHandler.annotateComplexExpression(element, databaseObjectExpression, holder, config)
    }
    
    override fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expression.length)
        val databaseObjectExpression = ParadoxDatabaseObjectExpression.resolve(expression, range, configGroup)
        if(databaseObjectExpression == null) return PsiReference.EMPTY_ARRAY
        return databaseObjectExpression.getReferences(element)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeDatabaseObjectExpression(context, result)
    }
}
