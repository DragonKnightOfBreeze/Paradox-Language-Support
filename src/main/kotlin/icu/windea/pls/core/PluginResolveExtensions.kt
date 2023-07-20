package icu.windea.pls.core

import com.intellij.psi.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.references.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.localisation.references.*
import icu.windea.pls.script.references.*

fun PsiReference.canResolveScriptedVariable(): Boolean {
    return when(this) {
        is ParadoxScriptedVariablePsiReference -> true
        else -> false
    }
}

fun PsiReference.canResolveDefinition(): Boolean {
    return when(this) {
        is ParadoxScriptExpressionPsiReference -> {
            val configExpression = this.config.expression ?: return false
            configExpression.type == CwtDataType.Definition
        }
        is ParadoxTemplateSnippetExpressionReference -> {
            val configExpression = this.configExpression
            configExpression.type == CwtDataType.Definition
        }
        is ParadoxDataExpressionNode.Reference -> {
            this.linkConfigs.any { linkConfig ->
                val configExpression = linkConfig.expression ?: return@any false
                configExpression.type == CwtDataType.Definition
            }
        }
        is ParadoxLocalisationCommandFieldPsiReference -> true //<scripted_loc>
        else -> false
    }
}

fun PsiReference.canResolveLocalisation(): Boolean {
    return when(this) {
        is ParadoxScriptExpressionPsiReference -> {
            val configExpression = this.config.expression ?: return false
            configExpression.type == CwtDataType.Localisation || configExpression.type == CwtDataType.InlineLocalisation
        }
        is ParadoxLocalisationPropertyPsiReference -> true
        else -> false
    }
}

fun PsiReference.canResolveParameter(): Boolean {
    return when(this) {
        is ParadoxScriptExpressionPsiReference -> {
            val configExpression = this.config.expression ?: return false
            configExpression.type == CwtDataType.Parameter
        }
        is ParadoxScriptValueArgumentExpressionNode.Reference -> true
        is ParadoxParameterPsiReference -> true
        is ParadoxConditionParameterPsiReference -> true
        else -> false
    }
}

fun PsiReference.canResolveLocalisationParameter(): Boolean {
    return when(this) {
        is ParadoxScriptExpressionPsiReference -> {
            val configExpression = this.config.expression ?: return false
            configExpression.type == CwtDataType.LocalisationParameter
        }
        is ParadoxLocalisationPropertyPsiReference -> true
        else -> false
    }
}

fun PsiReference.canResolveValueSetValue(): Boolean {
    return when(this) {
        is ParadoxScriptExpressionPsiReference -> {
            val configExpression = this.config.expression ?: return false
            configExpression.type.isValueSetValueType()
        }
        is ParadoxTemplateSnippetExpressionReference -> {
            val configExpression = this.configExpression
            configExpression.type.isValueSetValueType()
        }
        is ParadoxDataExpressionNode.Reference -> {
            this.linkConfigs.any { linkConfig ->
                val configExpression = linkConfig.expression ?: return@any false
                configExpression.type.isValueSetValueType()
            }
        }
        is ParadoxValueSetValueExpressionNode.Reference -> true
        is ParadoxLocalisationCommandScopePsiReference -> true //value[event_target], value[global_event_target]
        is ParadoxLocalisationCommandFieldPsiReference -> true //value[variable]
        else -> false
    }
}

fun PsiReference.canResolveComplexEnumValue(): Boolean {
    return when(this) {
        is ParadoxScriptExpressionPsiReference -> {
            val configExpression = this.config.expression ?: return false
            configExpression.type == CwtDataType.EnumValue
        }
        is ParadoxTemplateSnippetExpressionReference -> {
            val configExpression = this.configExpression
            configExpression.type == CwtDataType.EnumValue
        }
        is ParadoxDataExpressionNode.Reference -> {
            this.linkConfigs.any { linkConfig ->
                val configExpression = linkConfig.expression ?: return@any false
                configExpression.type == CwtDataType.EnumValue
            }
        }
        is ParadoxComplexEnumValuePsiReference -> true
        else -> false
    }
}