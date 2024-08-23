package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.lookup.*
import com.intellij.codeInsight.template.*
import com.intellij.icons.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.internal.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import javax.swing.*

sealed class CwtConfigTemplateExpression(
    val context: ProcessingContext,
    val schemaExpression: CwtSchemaExpression,
    val range: TextRange,
    val text: String,
) : Expression() {
    override fun calculateResult(context: ExpressionContext?): Result? {
        return TextResult(text)
    }
    
    override fun requiresCommittedPSI(): Boolean {
        return false
    }
    
    class Parameter(
        context: ProcessingContext,
        schemaExpression: CwtSchemaExpression,
        range: TextRange,
        text: String,
        parameterText: String,
    ) : CwtConfigTemplateExpression(context, schemaExpression, range, text) {
        val parameterName: String = parameterText
        
        val icon: Icon = AllIcons.Nodes.Parameter
        
        override fun calculateLookupItems(context: ExpressionContext): Array<LookupElement>? {
            //currently only calculate from configs
            val configGroup = this.context.configGroup ?: return null
            return when(parameterName) {
                "system_scope" -> configGroup.systemScopes.mapToArray { (n, c) -> createLookupItem(n, c) }
                "localisation_locale" -> configGroup.localisationLocalesById.mapToArray { (n, c) -> createLookupItem(n, c) }
                "localisation_predefined_parameter" -> configGroup.localisationPredefinedParameters.mapToArray { (n, c) -> createLookupItem(n, c) }
                "type" -> configGroup.types.mapToArray { (n, c) -> createLookupItem(n, c) }
                "subtype" -> {
                    val configPath = this.context.contextElement?.parentOfType<CwtMemberElement>(withSelf = true)?.configPath ?: return null
                    val type = when {
                        configPath.subPaths[0] == "types" -> configPath.subPaths.getOrNull(1)?.removeSurroundingOrNull("type[","]") ?: return null
                        else -> return null
                    }
                    configGroup.types[type]?.subtypes?.mapToArray { (n, c) -> createLookupItem(n, c) }
                }
                "enum" -> configGroup.enums.mapToArray { (n, c) -> createLookupItem(n, c) }
                "complex_enum" -> configGroup.complexEnums.mapToArray { (n, c) -> createLookupItem(n, c) }
                "complex_enum_value" -> {
                    val configPath = this.context.contextElement?.parentOfType<CwtMemberElement>(withSelf = true)?.configPath ?: return null
                    val complexEnum = when {
                        configPath.subPaths[0] == "complex_enum_values" -> configPath.subPaths.getOrNull(1) ?: return null
                        else -> return null
                    }
                    configGroup.extendedComplexEnumValues[complexEnum]?.mapToArray { (n, c) -> createLookupItem(n, c) }
                }
                "dynamic_value_type" -> configGroup.dynamicValueTypes.mapToArray { (n, c) -> createLookupItem(n, c) }
                "dynamic_value" -> {
                    val configPath = this.context.contextElement?.parentOfType<CwtMemberElement>(withSelf = true)?.configPath ?: return null
                    val complexEnum = when {
                        configPath.subPaths[0] == "dynamic_values" -> configPath.subPaths.getOrNull(1) ?: return null
                        else -> return null
                    }
                    configGroup.extendedDynamicValues[complexEnum]?.mapToArray { (n, c) -> createLookupItem(n, c) }
                }
                "link" -> configGroup.links.mapToArray { (n, c) -> createLookupItem(n, c) }
                "scope" -> null //no completion yet
                "localisation_link" -> configGroup.localisationLinks.mapToArray { (n, c) -> createLookupItem(n, c) }
                "localisation_command" -> configGroup.localisationCommands.mapToArray { (n, c) -> createLookupItem(n, c) }
                "modifier_category" -> configGroup.modifierCategories.mapToArray { (n, c) -> createLookupItem(n, c) }
                "modifier" -> configGroup.modifiers.mapToArray { (n, c) -> createLookupItem(n, c) }
                "scope_name" -> configGroup.scopes.mapToArray { (n, c) -> createLookupItem(n, c) }
                "scope_group" -> configGroup.scopeGroups.mapToArray { (n, c) -> createLookupItem(n, c) }
                "database_object_type" -> configGroup.databaseObjectTypes.mapToArray { (n, c) -> createLookupItem(n, c) }
                "scripted_variable" -> configGroup.extendedScriptedVariables.mapToArray { (n, c) -> createLookupItem(n, c) }
                "definition" -> configGroup.extendedDefinitions.mapToArray { (n, c) -> createLookupItem(n, c.singleOrNull()) }
                "game_rule" -> configGroup.extendedGameRules.mapToArray { (n, c) -> createLookupItem(n, c) }
                "on_action" -> configGroup.extendedOnActions.mapToArray { (n, c) -> createLookupItem(n, c) }
                "inline_script" -> configGroup.extendedInlineScripts.mapToArray { (n, c) -> createLookupItem(n, c) }
                "parameter" -> configGroup.extendedParameters.mapToArray { (n, c) -> createLookupItem(n, c.singleOrNull()) }
                "single_alias" -> configGroup.singleAliases.mapToArray { (n, c) -> createLookupItem(n, c) }
                "alias_name" -> configGroup.aliasGroups.mapToArray { (n) -> createLookupItem(n) }
                "alias_sub_name" -> {
                    val editor = context.editor ?: return null
                    val currentText = editor.document.charsSequence.substring(context.templateStartOffset, context.startOffset)
                    val aliasName = currentText.removeSurroundingOrNull("alias_name[",":") ?: return null
                    configGroup.aliasGroups[aliasName]?.mapToArray { (n, c) -> createLookupItem(n, c.singleOrNull()) }
                }
                "inline" -> configGroup.inlineConfigGroup.mapToArray { (n, c) -> createLookupItem(n, c.singleOrNull()) }
                else -> null
            }
        }
        
        private fun createLookupItem(name: String, config: CwtConfig<*>? = null): LookupElementBuilder {
            return LookupElementBuilder.create(name).withPsiElement(config?.pointer?.element)
                .withTailText(" by $text", true)
                .withIcon(icon)
        }
    }
    
    companion object Resolver {
        fun resolve(context: ProcessingContext, schemaExpression: CwtSchemaExpression, range: TextRange, text: String): CwtConfigTemplateExpression? {
            val parameterText = text.removeSurroundingOrNull("$", "$") ?: return null
            return Parameter(context, schemaExpression, range, text, parameterText)
        }
    }
}
