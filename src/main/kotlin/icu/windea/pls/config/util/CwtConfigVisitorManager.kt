package icu.windea.pls.config.util

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.orNull
import icu.windea.pls.core.withRecursionGuard

object CwtConfigVisitorManager {
    fun visitExpanded(config: CwtPropertyConfig, visitor: CwtMemberConfigVisitor, forUnion: Boolean = true, forSingleAlias: Boolean = true, forAlias: Boolean = true): Boolean {
        val valueExpression = config.valueExpression
        return when (valueExpression.type) {
            CwtDataTypes.UnionValue -> {
                if (!forUnion) return true
                val name = valueExpression.value?.orNull() ?: return true
                visitExpandedForUnion(name, config.configGroup, visitor)
            }
            CwtDataTypes.AliasKeysField -> {
                if (!forAlias) return true
                val name = valueExpression.value?.orNull() ?: return true
                visitExpandedForAliasGroup(name, config.configGroup, visitor)
            }
            CwtDataTypes.AliasMatchLeft -> {
                if (!forAlias) return true
                val name = valueExpression.value?.orNull() ?: return true
                val keyExpression = config.keyExpression
                if (keyExpression.type != CwtDataTypes.AliasName || keyExpression.value != name) return true // invalid
                visitExpandedForAliasGroup(name, config.configGroup, visitor)
            }
            CwtDataTypes.SingleAliasRight -> {
                if (!forSingleAlias) return true
                val name = valueExpression.value?.orNull() ?: return true
                visitExpandedForSingleAlias(name, config.configGroup, visitor)
            }
            else -> true
        }
    }

    fun visitExpanded(config: CwtValueConfig, visitor: CwtMemberConfigVisitor, forUnion: Boolean = true, forSingleAlias: Boolean = true, forAlias: Boolean = true): Boolean {
        val valueExpression = config.valueExpression
        return when (valueExpression.type) {
            CwtDataTypes.UnionValue -> {
                if (!forUnion) return true
                val name = valueExpression.value?.orNull() ?: return true
                visitExpandedForUnion(name, config.configGroup, visitor)
            }
            CwtDataTypes.AliasKeysField -> {
                if (!forAlias) return true
                val name = valueExpression.value?.orNull() ?: return true
                visitExpandedForAliasGroup(name, config.configGroup, visitor)
            }
            CwtDataTypes.AliasMatchLeft -> {
                true // ignored (must be processed on property config level)
            }
            CwtDataTypes.SingleAliasRight -> {
                if (!forSingleAlias) return true
                val name = valueExpression.value?.orNull() ?: return true
                visitExpandedForSingleAlias(name, config.configGroup, visitor)
            }
            else -> true
        }
    }

    private fun visitExpandedForUnion(name: String, configGroup: CwtConfigGroup, visitor: CwtMemberConfigVisitor): Boolean {
        // NOTE 3.0.0 recursion guard is required here
        val unionConfig = configGroup.unions[name] ?: return true
        return withRecursionGuard {
            withRecursionCheck("u:$name") {
                when (visitor) {
                    is CwtMemberConfigExpandedRecursiveVisitor -> visitor.visitUnion(name, unionConfig)
                    else -> visitUnion(name, unionConfig, visitor)
                }
            }
        } ?: true
    }

    private fun visitExpandedForAliasGroup(name: String, configGroup: CwtConfigGroup, visitor: CwtMemberConfigVisitor): Boolean {
        // NOTE 2.1.6 recursion guard is required here
        val aliasConfigGroup = configGroup.aliasGroups[name]?.values?.orNull() ?: return true
        return withRecursionGuard {
            withRecursionCheck("a:$name") check@{
                when (visitor) {
                    is CwtMemberConfigExpandedRecursiveVisitor -> visitor.visitAliasGroup(name, aliasConfigGroup)
                    else -> visitAliasGroup(name, aliasConfigGroup, visitor)
                }
            }
        } ?: true
    }

    private fun visitExpandedForSingleAlias(name: String, configGroup: CwtConfigGroup, visitor: CwtMemberConfigVisitor): Boolean {
        // NOTE 2.1.6 recursion guard is required here
        val singleAliasConfig = configGroup.singleAliases[name] ?: return true
        return withRecursionGuard {
            withRecursionCheck("sa:$name") {
                when (visitor) {
                    is CwtMemberConfigExpandedRecursiveVisitor -> visitor.visitSingleAlias(name, singleAliasConfig)
                    else -> visitSingleAlias(name, singleAliasConfig, visitor)
                }
            }
        } ?: true
    }

    @Suppress("UNUSED_PARAMETER")
    fun visitAliasGroup(name: String, aliasConfigGroup: Collection<List<CwtAliasConfig>>, visitor: CwtMemberConfigVisitor): Boolean {
        aliasConfigGroup.forEach { aliasConfigs ->
            aliasConfigs.forEachFast { aliasConfig ->
                visitAlias(name, aliasConfig, visitor).let { if (!it) return false }
            }
        }
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    fun visitUnion(name: String, config: CwtUnionConfig, visitor: CwtMemberConfigVisitor): Boolean {
        return config.config.accept(visitor)
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
