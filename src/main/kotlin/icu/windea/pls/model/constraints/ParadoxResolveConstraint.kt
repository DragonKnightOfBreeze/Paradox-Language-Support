package icu.windea.pls.model.constraints

import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.references.*
import icu.windea.pls.lang.references.csv.*
import icu.windea.pls.lang.references.localisation.*
import icu.windea.pls.lang.references.script.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

enum class ParadoxResolveConstraint {
    ScriptedVariable {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptedVariableReference -> true
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxScriptedVariablePsiReference -> true
                else -> false
            }
        }
    },
    Definition {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptExpressionElement -> element.isResolvableExpression() && element.isExpression()
                is ParadoxLocalisationExpressionElement -> element.isComplexExpression()
                is ParadoxLocalisationIcon -> true //<sprite>, etc.
                is ParadoxLocalisationConceptCommand -> true //<game_concept>
                is ParadoxLocalisationTextColorAwareElement -> true //<text_color>
                is ParadoxLocalisationTextFormat -> true //<text_format>
                is ParadoxLocalisationTextIcon -> true //<text_icon>
                is ParadoxCsvColumn -> !element.isHeaderColumn()
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType in CwtDataTypeGroups.DefinitionAware || dataType == CwtDataTypes.AliasKeysField
                }
                is ParadoxTemplateSnippetNode.Reference -> {
                    val configExpression = reference.config.configExpression
                    val dataType = configExpression.type
                    dataType in CwtDataTypeGroups.DefinitionAware || dataType == CwtDataTypes.AliasKeysField
                }
                is ParadoxDataSourceNode.Reference -> {
                    reference.node.linkConfigs.any { linkConfig ->
                        val configExpression = linkConfig.configExpression ?: return@any false
                        val dataType = configExpression.type
                        dataType in CwtDataTypeGroups.DefinitionAware || dataType == CwtDataTypes.AliasKeysField
                    }
                }
                is ParadoxDatabaseObjectDataSourceNode.Reference -> {
                    reference.node.config?.type != null
                }
                is ParadoxScriptValueNode.Reference -> true //<script_value>
                is ParadoxLocalisationIconPsiReference -> true //<sprite>, etc.
                is ParadoxLocalisationConceptPsiReference -> true //<game_concept>
                is ParadoxLocalisationTextColorPsiReference -> true //<text_color>
                is ParadoxLocalisationTextFormatPsiReference -> true //<text_format>
                is ParadoxLocalisationTextIconPsiReference -> true //<text_icon>
                is ParadoxCsvExpressionPsiReference -> {
                    val configExpression = reference.columnConfig.valueConfig?.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType == CwtDataTypes.Definition
                }
                else -> false
            }
        }
    },
    Localisation {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                is ParadoxLocalisationExpressionElement -> element.isDatabaseObjectExpression(strict = true)
                is ParadoxLocalisationParameter -> true
                //is ParadoxCsvColumn -> !element.isHeaderColumn()
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType in CwtDataTypeGroups.LocalisationAware || dataType == CwtDataTypes.AliasKeysField
                }
                is ParadoxTemplateSnippetNode.Reference -> {
                    val configExpression = reference.config.configExpression
                    val dataType = configExpression.type
                    dataType in CwtDataTypeGroups.LocalisationAware || dataType == CwtDataTypes.AliasKeysField
                }
                is ParadoxDatabaseObjectDataSourceNode.Reference -> {
                    reference.node.config?.localisation != null
                }
                is ParadoxLocalisationParameterPsiReference -> true
                else -> false
            }
        }
    },
    Parameter {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxParameter -> true
                is ParadoxConditionParameter -> true
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType == CwtDataTypes.Parameter
                }
                is ParadoxScriptValueArgumentNode.Reference -> true
                is ParadoxParameterPsiReference -> true
                is ParadoxConditionParameterPsiReference -> true
                else -> false
            }
        }
    },
    LocalisationParameter {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxLocalisationParameter -> true
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType == CwtDataTypes.LocalisationParameter
                }
                is ParadoxLocalisationParameterPsiReference -> true
                else -> false
            }
        }
    },
    ComplexEnumValue {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                is ParadoxCsvColumn -> !element.isHeaderColumn()
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType == CwtDataTypes.EnumValue || dataType == CwtDataTypes.AliasKeysField
                }
                is ParadoxTemplateSnippetNode.Reference -> {
                    val configExpression = reference.config.configExpression
                    val dataType = configExpression.type
                    dataType == CwtDataTypes.EnumValue || dataType == CwtDataTypes.AliasKeysField
                }
                is ParadoxDataSourceNode.Reference -> {
                    reference.node.linkConfigs.any { linkConfig ->
                        val configExpression = linkConfig.configExpression ?: return@any false
                        val dataType = configExpression.type
                        dataType == CwtDataTypes.EnumValue || dataType == CwtDataTypes.AliasKeysField
                    }
                }
                is ParadoxComplexEnumValuePsiReference -> true
                is ParadoxCsvExpressionPsiReference -> {
                    val configExpression = reference.columnConfig.valueConfig?.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType == CwtDataTypes.EnumValue
                }
                else -> false
            }
        }
    },
    DynamicValue {
        override fun canResolveReference(element: PsiElement): Boolean {
            return when (element) {
                is ParadoxScriptStringExpressionElement -> element.isExpression()
                is ParadoxLocalisationExpressionElement -> element.isCommandExpression()
                else -> false
            }
        }

        override fun canResolve(reference: PsiReference): Boolean {
            return when (reference) {
                is ParadoxScriptExpressionPsiReference -> {
                    val configExpression = reference.config.configExpression ?: return false
                    val dataType = configExpression.type
                    dataType in CwtDataTypeGroups.DynamicValue || dataType == CwtDataTypes.AliasKeysField
                }
                is ParadoxTemplateSnippetNode.Reference -> {
                    val configExpression = reference.config.configExpression
                    val dataType = configExpression.type
                    dataType in CwtDataTypeGroups.DynamicValue || dataType == CwtDataTypes.AliasKeysField
                }
                is ParadoxDataSourceNode.Reference -> {
                    reference.node.linkConfigs.any { linkConfig ->
                        val configExpression = linkConfig.configExpression ?: return@any false
                        val dataType = configExpression.type
                        dataType in CwtDataTypeGroups.DynamicValue || dataType == CwtDataTypes.AliasKeysField
                    }
                }
                is ParadoxDynamicValueNode.Reference -> true
                else -> false
            }
        }
    },
    DynamicValueStrictly {
        override fun canResolveReference(element: PsiElement): Boolean {
            return DynamicValue.canResolveReference(element)
        }

        override fun canResolve(reference: PsiReference): Boolean {
            if (reference is ParadoxTemplateSnippetNode.Reference) return false
            if (reference is ParadoxDataSourceNode.Reference) return false
            return DynamicValue.canResolve(reference)
        }
    },
    ;

    open fun canResolveReference(element: PsiElement): Boolean = true

    open fun canResolve(reference: PsiReference): Boolean = true
}
