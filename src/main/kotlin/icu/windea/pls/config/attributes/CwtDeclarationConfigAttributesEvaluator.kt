package icu.windea.pls.config.attributes

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.util.CwtMemberConfigRecursiveVisitor
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.optimized
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionSubtypeExpression

object CwtDeclarationConfigAttributesEvaluator {
    private data class Context(
        val involvedSubtypes: MutableSet<String> = sortedSetOf(),
        var dynamicValueInvolved: Boolean = false,
        var parameterInvolved: Boolean = false,
        var localisationParameterInvolved: Boolean = false,
    )

    fun evaluate(config: CwtDeclarationConfig): CwtDeclarationConfigAttributes {
        val context = Context()
        config.config.acceptChildren(object : CwtMemberConfigRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                processSubtypeExpression(context, config)
                visitForProperty(config).let { if (!it) return false }
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                visitForValue(config).let { if (!it) return false }
                return super.visitValue(config)
            }

            private fun visitForProperty(config: CwtPropertyConfig): Boolean {
                processDataExpression(context, config.keyExpression)
                processDataExpression(context, config.valueExpression)
                return visitInlined(config)
            }

            private fun visitForValue(config: CwtValueConfig): Boolean {
                processDataExpression(context, config.valueExpression)
                return visitInlined(config)
            }

            private fun visitInlined(config: CwtPropertyConfig): Boolean {
                return CwtConfigManipulator.visitInlined(config, visitor = this)
            }

            private fun visitInlined(config: CwtValueConfig): Boolean {
                return CwtConfigManipulator.visitInlined(config, visitor = this)
            }
        })
        return buildAttributes(context)
    }

    private fun processSubtypeExpression(context: Context, config: CwtPropertyConfig) {
        val subtypeExpression = config.key.removeSurroundingOrNull("subtype[", "]")
        if (subtypeExpression != null) {
            val resolved = ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression)
            resolved.subtypes.forEachFast { (subtype, _) -> context.involvedSubtypes.add(subtype) }
        }
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
