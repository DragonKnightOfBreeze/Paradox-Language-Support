package icu.windea.pls.config.util

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.orNull
import icu.windea.pls.core.withRecursionGuard

object CwtConfigVisitorManager {
    fun visitInlined(config: CwtPropertyConfig, forSingleAlias: Boolean = true, forAlias: Boolean = true, visitor: CwtMemberConfigVisitor): Boolean {
        val valueExpression = config.valueExpression
        return when (valueExpression.type) {
            CwtDataTypes.AliasKeysField -> {
                if (!forAlias) return true
                val name = valueExpression.value?.orNull() ?: return true
                visitInlinedForAliasGroup(name, config.configGroup, visitor)
            }
            CwtDataTypes.AliasMatchLeft -> {
                if (!forAlias) return true
                val name = valueExpression.value?.orNull() ?: return true
                val keyExpression = config.keyExpression
                if (keyExpression.type != CwtDataTypes.AliasName || keyExpression.value != name) return true // invalid
                visitInlinedForAliasGroup(name, config.configGroup, visitor)
            }
            CwtDataTypes.SingleAliasRight -> {
                if (!forSingleAlias) return true
                val name = valueExpression.value?.orNull() ?: return true
                visitInlinedForSingleAlias(name, config.configGroup, visitor)
            }
            else -> true
        }
    }

    fun visitInlined(config: CwtValueConfig, forSingleAlias: Boolean = true, forAlias: Boolean = true, visitor: CwtMemberConfigVisitor): Boolean {
        val valueExpression = config.valueExpression
        return when (valueExpression.type) {
            CwtDataTypes.AliasKeysField -> {
                if (!forAlias) return true
                val name = valueExpression.value?.orNull() ?: return true
                visitInlinedForAliasGroup(name, config.configGroup, visitor)
            }
            CwtDataTypes.AliasMatchLeft -> {
                true // ignored (must be processed on property config level)
            }
            CwtDataTypes.SingleAliasRight -> {
                if (!forSingleAlias) return true
                val name = valueExpression.value?.orNull() ?: return true
                visitInlinedForSingleAlias(name, config.configGroup, visitor)
            }
            else -> true
        }
    }

    private fun visitInlinedForAliasGroup(name: String, configGroup: CwtConfigGroup, visitor: CwtMemberConfigVisitor): Boolean {
        // NOTE 2.1.6 recursion guard is required here
        val aliasConfigGroup = configGroup.aliasGroups[name]?.values?.orNull() ?: return true
        return withRecursionGuard {
            withRecursionCheck("a:$name") check@{
                when (visitor) {
                    is CwtMemberConfigInlinedRecursiveVisitor -> visitor.visitAliasGroup(name, aliasConfigGroup)
                    else -> visitAliasGroup(name, aliasConfigGroup, visitor)
                }
            }
        } ?: true
    }

    private fun visitInlinedForSingleAlias(name: String, configGroup: CwtConfigGroup, visitor: CwtMemberConfigVisitor): Boolean {
        // NOTE 2.1.6 recursion guard is required here
        val singleAliasConfig = configGroup.singleAliases[name] ?: return true
        return withRecursionGuard {
            withRecursionCheck("sa:$name") {
                when (visitor) {
                    is CwtMemberConfigInlinedRecursiveVisitor -> visitor.visitSingleAlias(name, singleAliasConfig)
                    else -> visitSingleAlias(name, singleAliasConfig, visitor)
                }
            }
        } ?: true
    }

    fun visitAliasGroup(name: String, aliasConfigGroup: Collection<List<CwtAliasConfig>>, visitor: CwtMemberConfigVisitor): Boolean {
        aliasConfigGroup.forEach { aliasConfigs ->
            aliasConfigs.forEachFast { aliasConfig ->
                val r = visitAlias(name, aliasConfig, visitor)
                if (!r) return false
            }
        }
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    fun visitAlias(name: String, config: CwtAliasConfig, visitor: CwtMemberConfigVisitor): Boolean {
        return config.config.accept(visitor)
    }

    @Suppress("UNUSED_PARAMETER")
    fun visitSingleAlias(name: String, config: CwtSingleAliasConfig, visitor: CwtMemberConfigVisitor): Boolean {
        return config.config.accept(visitor)
    }
}
