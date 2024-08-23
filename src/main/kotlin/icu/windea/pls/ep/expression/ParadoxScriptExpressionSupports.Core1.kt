package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

class ParadoxScriptDynamicValueExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type in CwtDataTypeGroups.DynamicValue
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val dynamicValueExpression = ParadoxDynamicValueExpression.resolve(expressionText, range, configGroup, config) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, dynamicValueExpression, holder, config)
    }
    
    override fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val dynamicValueExpression = ParadoxDynamicValueExpression.resolve(expressionText, range, configGroup, config)
        if(dynamicValueExpression == null) return PsiReference.EMPTY_ARRAY
        return dynamicValueExpression.getReferences(element)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeDynamicValueExpression(context, result)
    }
}

class ParadoxScriptScopeFieldExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type in CwtDataTypeGroups.ScopeField
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expressionText, range, configGroup) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, scopeFieldExpression, holder, config)
    }
    
    override fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expressionText, range, configGroup)
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

class ParadoxScriptValueFieldExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type in CwtDataTypeGroups.ValueField
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(expressionText, range, configGroup) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, valueFieldExpression, holder, config)
    }
    
    override fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(expressionText, range, configGroup)
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

class ParadoxScriptVariableFieldExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type in CwtDataTypeGroups.VariableField
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expressionText, range, configGroup) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, variableFieldExpression, holder, config)
    }
    
    override fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expressionText, range, configGroup)
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

class ParadoxScriptDatabaseObjectExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type in CwtDataTypeGroups.DatabaseObject
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val databaseObjectExpression = ParadoxDatabaseObjectExpression.resolve(expressionText, range, configGroup) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, databaseObjectExpression, holder, config)
    }
    
    override fun getReferences(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if(element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val databaseObjectExpression = ParadoxDatabaseObjectExpression.resolve(expressionText, range, configGroup)
        if(databaseObjectExpression == null) return PsiReference.EMPTY_ARRAY
        return databaseObjectExpression.getReferences(element)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeDatabaseObjectExpression(context, result)
    }
}
