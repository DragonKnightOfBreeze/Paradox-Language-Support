package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.lang.codeInsight.completion.ParadoxComplexExpressionCompletionManager
import icu.windea.pls.lang.codeInsight.completion.config
import icu.windea.pls.lang.codeInsight.completion.isInt
import icu.windea.pls.lang.codeInsight.completion.scopeGroupName
import icu.windea.pls.lang.codeInsight.completion.scopeName

// Complex Expression

class ParadoxScriptTemplateExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type == CwtDataTypes.TemplateExpression
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeTemplateExpression(context, result)
    }
}

class ParadoxScriptDynamicValueExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type in CwtDataTypeGroups.DynamicValue
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeDynamicValueExpression(context, result)
    }
}

class ParadoxScriptScopeFieldExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type in CwtDataTypeGroups.ScopeField
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

class ParadoxScriptValueFieldExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type in CwtDataTypeGroups.ValueField
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

class ParadoxScriptVariableFieldExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type in CwtDataTypeGroups.VariableField
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

class ParadoxScriptDatabaseObjectExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type == CwtDataTypes.DatabaseObject
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeDatabaseObjectExpression(context, result)
    }
}

class ParadoxScriptDefineReferenceExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type == CwtDataTypes.DefineReference
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeDefineReferenceExpression(context, result)
    }
}

class ParadoxScriptStellarisNameFormatExpressionSupport : ParadoxScriptComplexExpressionSupportBase() {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.configExpression?.type == CwtDataTypes.StellarisNameFormat
    }

    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        ParadoxComplexExpressionCompletionManager.completeStellarisNameFormatExpression(context, result)
    }
}
