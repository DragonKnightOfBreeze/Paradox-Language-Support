package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.lang.codeInsight.completion.ParadoxComplexExpressionCompletionManager
import icu.windea.pls.lang.codeInsight.completion.config
import icu.windea.pls.lang.codeInsight.completion.isInt
import icu.windea.pls.lang.codeInsight.completion.scopeGroupName
import icu.windea.pls.lang.codeInsight.completion.scopeName
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.StellarisNameFormatExpression

// Complex Expression

/**
 * @see CwtDataTypes.TemplateExpression
 * @see ParadoxTemplateExpression
 */
class ParadoxScriptTemplateExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.TemplateExpression
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeTemplateExpression(context, result)
    }
}

/**
 * @see CwtDataTypeGroups.DynamicValue
 * @see ParadoxDynamicValueExpression
 */
class ParadoxScriptDynamicValueExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType in CwtDataTypeGroups.DynamicValue
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeDynamicValueExpression(context, result)
    }
}

/**
 * @see CwtDataTypeGroups.ScopeField
 * @see ParadoxScopeFieldExpression
 */
class ParadoxScriptScopeFieldExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType in CwtDataTypeGroups.ScopeField
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
        }
        ParadoxComplexExpressionCompletionManager.completeScopeFieldExpression(context, result)
        when (configExpression.type) {
            CwtDataTypes.Scope -> {
                context.scopeName = null
            }
            CwtDataTypes.ScopeGroup -> {
                context.scopeGroupName = null
            }
        }
    }
}

/**
 * @see CwtDataTypeGroups.ValueField
 * @see ParadoxValueFieldExpression
 */
class ParadoxScriptValueFieldExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType in CwtDataTypeGroups.ValueField
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val configExpression = context.config?.configExpression ?: return
        when (configExpression.type) {
            CwtDataTypes.IntValueField -> {
                context.isInt = true
            }
        }
        ParadoxComplexExpressionCompletionManager.completeValueFieldExpression(context, result)
        when (configExpression.type) {
            CwtDataTypes.IntValueField -> {
                context.isInt = null
            }
        }
    }
}

/**
 * @see CwtDataTypeGroups.VariableField
 * @see ParadoxVariableFieldExpression
 */
class ParadoxScriptVariableFieldExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType in CwtDataTypeGroups.VariableField
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        val configExpression = context.config?.configExpression ?: return
        when (configExpression.type) {
            CwtDataTypes.IntVariableField -> {
                context.isInt = true
            }
        }
        ParadoxComplexExpressionCompletionManager.completeVariableFieldExpression(context, result)
        when (configExpression.type) {
            CwtDataTypes.IntVariableField -> {
                context.isInt = null
            }
        }
    }
}

/**
 * @see CwtDataTypes.DatabaseObject
 * @see ParadoxDatabaseObjectExpression
 */
class ParadoxScriptDatabaseObjectExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.DatabaseObject
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeDatabaseObjectExpression(context, result)
    }
}

/**
 * @see CwtDataTypes.DefineReference
 * @see ParadoxDefineReferenceExpression
 */
class ParadoxScriptDefineReferenceExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.DefineReference
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeDefineReferenceExpression(context, result)
    }
}

/**
 * @see CwtDataTypes.StellarisNameFormat
 * @see StellarisNameFormatExpression
 */
class ParadoxScriptStellarisNameFormatExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.StellarisNameFormat
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeStellarisNameFormatExpression(context, result)
    }
}
