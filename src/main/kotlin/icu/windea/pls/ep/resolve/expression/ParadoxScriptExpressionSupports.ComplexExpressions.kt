package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxComplexExpressionCompletionManager
import icu.windea.pls.lang.resolve.complexExpression.ParadoxArrayDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDatabaseObjectExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxNameFormatExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScriptValueReferenceExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTagsExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxValueFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxVariableFieldExpression

// Complex Expressions

/**
 * @see CwtDataTypes.TemplateExpression
 * @see ParadoxTemplateExpression
 */
class ParadoxScriptTemplateExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.TemplateExpression
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeTemplateExpression(context, result)
    }
}

/**
 * @see CwtDataTypeSets.DynamicValue
 * @see ParadoxDynamicValueExpression
 */
class ParadoxScriptDynamicValueExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType in CwtDataTypeSets.DynamicValue
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeDynamicValueExpression(context, result)
    }
}

/**
 * @see CwtDataTypeSets.ScopeField
 * @see ParadoxScopeFieldExpression
 */
class ParadoxScriptScopeFieldExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType in CwtDataTypeSets.ScopeField
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val configExpression = context.config?.configExpression ?: return
        val context = when (configExpression.type) {
            CwtDataTypes.Scope -> context.copy(scopeName = configExpression.value)
            CwtDataTypes.ScopeGroup -> context.copy(scopeGroupName = configExpression.value)
            else -> context
        }
        ParadoxComplexExpressionCompletionManager.completeScopeFieldExpression(context, result)
    }
}

/**
 * @see CwtDataTypeSets.ValueField
 * @see ParadoxValueFieldExpression
 */
class ParadoxScriptValueFieldExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType in CwtDataTypeSets.ValueField
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val configExpression = context.config?.configExpression ?: return
        val context = when (configExpression.type) {
            CwtDataTypes.IntValueField -> context.copy(isInt = true)
            else -> context
        }
        ParadoxComplexExpressionCompletionManager.completeValueFieldExpression(context, result)
    }
}

/**
 * @see CwtDataTypeSets.VariableField
 * @see ParadoxVariableFieldExpression
 */
class ParadoxScriptVariableFieldExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType in CwtDataTypeSets.VariableField
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val configExpression = context.config?.configExpression ?: return
        val context = when (configExpression.type) {
            CwtDataTypes.IntVariableField -> context.copy(isInt = true)
            else -> context
        }
        ParadoxComplexExpressionCompletionManager.completeVariableFieldExpression(context, result)
    }
}

/**
 * @see CwtDataTypes.ScriptValueReference
 * @see ParadoxScriptValueReferenceExpression
 */
class ParadoxScriptScriptValueReferenceExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.ScriptValueReference
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeScriptValueReferenceExpression(context, result)
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

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeDefineReferenceExpression(context, result)
    }
}

/**
 * @see CwtDataTypes.ArrayDefineReference
 * @see ParadoxArrayDefineReferenceExpression
 */
class ParadoxScriptArrayDefineReferenceExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.ArrayDefineReference
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeArrayDefineReferenceExpression(context, result)
    }
}

/**
 * @see CwtDataTypes.Tags
 * @see ParadoxTagsExpression
 */
class ParadoxScriptTagsExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Tags
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeTagsExpression(context, result)
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

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeDatabaseObjectExpression(context, result)
    }
}

/**
 * @see CwtDataTypes.NameFormat
 * @see ParadoxNameFormatExpression
 */
class ParadoxScriptNameFormatExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.NameFormat
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeNameFormatExpression(context, result)
    }
}
