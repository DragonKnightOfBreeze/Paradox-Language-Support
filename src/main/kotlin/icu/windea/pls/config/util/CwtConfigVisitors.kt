@file:Suppress("unused")

package icu.windea.pls.config.util

import icu.windea.pls.config.config.CwtExpandableConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtUnionConfig

/**
 * 成员规则的访问者。
 */
abstract class CwtMemberConfigVisitor {
    open fun visit(config: CwtMemberConfig<*>): Boolean {
        return true
    }

    open fun visitProperty(config: CwtPropertyConfig): Boolean {
        return visit(config)
    }

    open fun visitValue(config: CwtValueConfig): Boolean {
        return visit(config)
    }
}

/**
 * 递归向下遍历的成员规则的访问器。
 */
abstract class CwtMemberConfigRecursiveVisitor : CwtMemberConfigVisitor() {
    override fun visit(config: CwtMemberConfig<*>): Boolean {
        val r = config.acceptChildren(this)
        if (!r) return false
        return visitFinished(config)
    }

    open fun visitFinished(config: CwtMemberConfig<*>): Boolean {
        return true
    }
}

/**
 * 可以按需展开规则（并集规则、别名规则、单别名规则）的，递归向下遍历的成员规则的访问者。
 *
 * @see CwtExpandableConfig
 */
abstract class CwtMemberConfigExpandedRecursiveVisitor(
    val forUnion: Boolean = true,
    val forSingleAlias: Boolean = true,
    val forAlias: Boolean = true,
) : CwtMemberConfigRecursiveVisitor() {
    private var _depth: Int = 0

    val depth: Int get() = _depth.coerceAtLeast(0)
    val expanded: Boolean get() = _depth > 0

    override fun visitProperty(config: CwtPropertyConfig): Boolean {
        visitExpanded(config).let { if (!it) return false }
        return super.visitProperty(config)
    }

    override fun visitValue(config: CwtValueConfig): Boolean {
        visitExpanded(config).let { if (!it) return false }
        return super.visitValue(config)
    }

    private fun visitExpanded(config: CwtPropertyConfig): Boolean {
        return CwtConfigVisitorManager.visitExpanded(config, this, forUnion, forSingleAlias, forAlias)
    }

    private fun visitExpanded(config: CwtValueConfig): Boolean {
        return CwtConfigVisitorManager.visitExpanded(config, this, forUnion, forSingleAlias, forAlias)
    }

    open fun visitAliasGroup(name: String, aliasConfigGroup: Collection<List<CwtAliasConfig>>): Boolean {
        return CwtConfigVisitorManager.visitAliasGroup(name, aliasConfigGroup, this)
    }

    open fun visitUnion(name: String, config: CwtUnionConfig): Boolean {
        return withDepthIncrement { CwtConfigVisitorManager.visitUnion(name, config, this) }
    }

    open fun visitAlias(name: String, config: CwtAliasConfig): Boolean {
        return withDepthIncrement { CwtConfigVisitorManager.visitAlias(name, config, this) }
    }

    open fun visitSingleAlias(name: String, config: CwtSingleAliasConfig): Boolean {
        return withDepthIncrement { CwtConfigVisitorManager.visitSingleAlias(name, config, this) }
    }

    private inline fun <T> withDepthIncrement(action: () -> T): T {
        return try {
            _depth++
            action()
        } finally {
            _depth--
        }
    }
}
