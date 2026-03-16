package icu.windea.pls.config.attributes

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.util.CwtMemberConfigInlinedRecursiveVisitor
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.optimized
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionSubtypeExpression

/**
 * 声明规则的综合属性的评估器。
 *
 * @see CwtDeclarationConfig
 * @see CwtDeclarationConfigAttributes
 */
object CwtDeclarationConfigAttributesEvaluator {
    private data class Context(
        val involvedSubtypes: MutableSet<String> = sortedSetOf(),
        var dynamicValueInvolved: Boolean = false,
        var parameterInvolved: Boolean = false,
        var localisationParameterInvolved: Boolean = false,
    )

    fun evaluate(config: CwtDeclarationConfig): CwtDeclarationConfigAttributes {
        val context = Context()
        config.config.acceptChildren(object : CwtMemberConfigInlinedRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                if (!inlined) processSubtypeExpression(context, config)
                processDataExpression(context, config.keyExpression)
                processDataExpression(context, config.valueExpression)
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                processDataExpression(context, config.valueExpression)
                return super.visitValue(config)
            }

            override fun visitSingleAlias(name: String, config: CwtSingleAliasConfig): Boolean {
                checkSingleAliasName(context, name).let { if (!it) return true }
                return super.visitSingleAlias(name, config)
            }

            override fun visitAliasGroup(name: String, aliasConfigGroup: Collection<List<CwtAliasConfig>>): Boolean {
                checkAliasName(context, name).let { if (!it) return true }
                return super.visitAliasGroup(name, aliasConfigGroup)
            }
        })
        return buildAttributes(context)
    }

    private fun processSubtypeExpression(context: Context, config: CwtPropertyConfig) {
        val subtypeExpression = config.key.removeSurroundingOrNull("subtype[", "]") ?: return
        val resolved = ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression)
        resolved.subtypes.forEachFast { (subtype, _) -> context.involvedSubtypes.add(subtype) }
    }

    private fun processDataExpression(context: Context, dataExpression: CwtDataExpression) {
        val dataType = dataExpression.type
        if (!context.dynamicValueInvolved) {
            val r = dataType in CwtDataTypeSets.DynamicValueInvolved
            if (r) context.dynamicValueInvolved = true
        }
        if (!context.parameterInvolved) {
            val r = dataType in CwtDataTypeSets.ParameterInvolved
            if (r) context.parameterInvolved = true
        }
        if (!context.localisationParameterInvolved) {
            val r = dataType in CwtDataTypeSets.LocalisationParameterInvolved
            if (r) context.localisationParameterInvolved = true
        }
    }

    private fun checkSingleAliasName(context: Context, name: String): Boolean {
        // any trigger or effect clause -> involved (relax check)
        val involved = "trigger_clause" in name || "effect_clause" in name
        if (!involved) return true
        context.dynamicValueInvolved = true
        context.parameterInvolved = true
        context.localisationParameterInvolved = true
        return false
    }

    private fun checkAliasName(context: Context, name: String): Boolean {
        // any trigger or effect alias -> involved (relax check)
        val involved = "trigger" == name || "effect" in name
        if (!involved) return true
        context.dynamicValueInvolved = true
        context.parameterInvolved = true
        context.localisationParameterInvolved = true
        return false
    }

    private fun buildAttributes(context: Context): CwtDeclarationConfigAttributes {
        val result = CwtDeclarationConfigAttributes(
            context.involvedSubtypes.optimized(),
            context.dynamicValueInvolved,
            context.parameterInvolved,
            context.localisationParameterInvolved,
        )
        if (result == CwtDeclarationConfigAttributes.EMPTY) return CwtDeclarationConfigAttributes.EMPTY
        return result
    }
}
