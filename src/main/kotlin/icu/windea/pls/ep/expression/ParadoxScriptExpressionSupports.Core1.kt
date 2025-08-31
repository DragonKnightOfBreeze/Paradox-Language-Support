package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.config
import icu.windea.pls.lang.codeInsight.completion.isInt
import icu.windea.pls.lang.codeInsight.completion.scopeGroupName
import icu.windea.pls.lang.codeInsight.completion.scopeName
import icu.windea.pls.lang.expression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.expression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.expression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.expression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.expression.ParadoxTemplateExpression
import icu.windea.pls.lang.expression.ParadoxValueFieldExpression
import icu.windea.pls.lang.expression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.expression.getAllReferences
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

class ParadoxScriptTemplateExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type == CwtDataTypes.TemplateExpression
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val templateExpression = ParadoxTemplateExpression.resolve(expressionText, range, configGroup, config) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, templateExpression, holder, config)
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if (element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val templateExpression = ParadoxTemplateExpression.resolve(expressionText, range, configGroup, config)
        if (templateExpression == null) return PsiReference.EMPTY_ARRAY
        return templateExpression.getAllReferences(element).toTypedArray()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeTemplateExpression(context, result)
    }
}

class ParadoxScriptDynamicValueExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type in CwtDataTypeGroups.DynamicValue
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val dynamicValueExpression = ParadoxDynamicValueExpression.resolve(expressionText, range, configGroup, config) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, dynamicValueExpression, holder, config)
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if (element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val dynamicValueExpression = ParadoxDynamicValueExpression.resolve(expressionText, range, configGroup, config)
        if (dynamicValueExpression == null) return PsiReference.EMPTY_ARRAY
        return dynamicValueExpression.getAllReferences(element).toTypedArray()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeDynamicValueExpression(context, result)
    }
}

class ParadoxScriptScopeFieldExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type in CwtDataTypeGroups.ScopeField
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expressionText, range, configGroup) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, scopeFieldExpression, holder, config)
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if (element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expressionText, range, configGroup)
        if (scopeFieldExpression == null) return PsiReference.EMPTY_ARRAY
        return scopeFieldExpression.getAllReferences(element).toTypedArray()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val configExpression = context.config?.configExpression ?: return
        when (configExpression.type) {
            CwtDataTypes.Scope -> {
                context.scopeName = configExpression.value
            }
            CwtDataTypes.ScopeGroup -> {
                context.scopeGroupName = configExpression.value
            }
            else -> {}
        }
        ParadoxCompletionManager.completeScopeFieldExpression(context, result)
        when (configExpression.type) {
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
        return config.configExpression?.type in CwtDataTypeGroups.ValueField
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(expressionText, range, configGroup) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, valueFieldExpression, holder, config)
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if (element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(expressionText, range, configGroup)
        if (valueFieldExpression == null) return PsiReference.EMPTY_ARRAY
        return valueFieldExpression.getAllReferences(element).toTypedArray()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val configExpression = context.config?.configExpression ?: return
        when (configExpression.type) {
            CwtDataTypes.IntValueField -> {
                context.isInt = true
            }
            else -> {}
        }
        ParadoxCompletionManager.completeValueFieldExpression(context, result)
        when (configExpression.type) {
            CwtDataTypes.IntValueField -> {
                context.isInt = null
            }
            else -> {}
        }
    }
}

class ParadoxScriptVariableFieldExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type in CwtDataTypeGroups.VariableField
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expressionText, range, configGroup) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, variableFieldExpression, holder, config)
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if (element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expressionText, range, configGroup)
        if (variableFieldExpression == null) return PsiReference.EMPTY_ARRAY
        return variableFieldExpression.getAllReferences(element).toTypedArray()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val configExpression = context.config?.configExpression ?: return
        when (configExpression.type) {
            CwtDataTypes.IntVariableField -> {
                context.isInt = true
            }
            else -> {}
        }
        ParadoxCompletionManager.completeVariableFieldExpression(context, result)
        when (configExpression.type) {
            CwtDataTypes.IntVariableField -> {
                context.isInt = null
            }
            else -> {}
        }
    }
}

class ParadoxScriptDatabaseObjectExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type == CwtDataTypes.DatabaseObject
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val databaseObjectExpression = ParadoxDatabaseObjectExpression.resolve(expressionText, range, configGroup) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, databaseObjectExpression, holder, config)
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if (element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val databaseObjectExpression = ParadoxDatabaseObjectExpression.resolve(expressionText, range, configGroup)
        if (databaseObjectExpression == null) return PsiReference.EMPTY_ARRAY
        return databaseObjectExpression.getAllReferences(element).toTypedArray()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeDatabaseObjectExpression(context, result)
    }
}

class ParadoxScriptDefineReferenceExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type == CwtDataTypes.DefineReference
    }

    override fun annotate(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        if (element !is ParadoxScriptStringExpressionElement) return
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val defineReferenceExpression = ParadoxDefineReferenceExpression.resolve(expressionText, range, configGroup) ?: return
        ParadoxExpressionManager.annotateComplexExpression(element, defineReferenceExpression, holder, config)
    }

    override fun getReferences(element: ParadoxExpressionElement, rangeInElement: TextRange?, expressionText: String, config: CwtConfig<*>, isKey: Boolean?): Array<out PsiReference>? {
        if (element !is ParadoxScriptStringExpressionElement) return PsiReference.EMPTY_ARRAY
        val configGroup = config.configGroup
        val range = TextRange.create(0, expressionText.length)
        val defineReferenceExpression = ParadoxDefineReferenceExpression.resolve(expressionText, range, configGroup)
        if (defineReferenceExpression == null) return PsiReference.EMPTY_ARRAY
        return defineReferenceExpression.getAllReferences(element).toTypedArray()
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxCompletionManager.completeDefineReferenceExpression(context, result)
    }
}
