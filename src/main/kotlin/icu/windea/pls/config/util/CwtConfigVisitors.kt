@file:Suppress("unused")

package icu.windea.pls.config.util

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.manipulators.CwtConfigManipulator
import icu.windea.pls.core.collections.forEachFast

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
 * 可以按需展开要内联的规则（单别名规则、别名规则）的，递归向下遍历的成员规则的访问者。
 *
 * @see CwtSingleAliasConfig
 * @see CwtAliasConfig
 */
abstract class CwtMemberConfigInlinedRecursiveVisitor(
    val forSingleAlias: Boolean = true,
    val forAlias: Boolean = true,
) : CwtMemberConfigRecursiveVisitor() {
    private var _inlineDepth: Int = 0

    val inlineDepth: Int get() = _inlineDepth.coerceAtLeast(0)
    val inlined: Boolean get() = _inlineDepth > 0

    override fun visitProperty(config: CwtPropertyConfig): Boolean {
        visitInlinedProperty(config).let { if (!it) return false }
        return super.visitProperty(config)
    }

    override fun visitValue(config: CwtValueConfig): Boolean {
        visitInlinedValue(config).let { if (!it) return false }
        return super.visitValue(config)
    }

    private fun visitInlinedProperty(config: CwtPropertyConfig): Boolean {
        return CwtConfigManipulator.visitInlined(config, forSingleAlias, forAlias, this)
    }

    private fun visitInlinedValue(config: CwtValueConfig): Boolean {
        return CwtConfigManipulator.visitInlined(config, forSingleAlias, forAlias, this)
    }

    open fun visitSingleAlias(name: String, config: CwtSingleAliasConfig): Boolean {
        return withInlineDepthIncrement { config.config.accept(this) }
    }

    open fun visitAliasGroup(name: String, aliasConfigGroup: Collection<List<CwtAliasConfig>>): Boolean {
        aliasConfigGroup.forEach { aliasConfigs ->
            aliasConfigs.forEachFast { aliasConfig ->
                visitAlias(aliasConfig).let { if (!it) return false }
            }
        }
        return true
    }

    open fun visitAlias(config: CwtAliasConfig): Boolean {
        return withInlineDepthIncrement { config.config.accept(this) }
    }

    private inline fun <T> withInlineDepthIncrement(action: () -> T): T {
        return try {
            _inlineDepth++
            action()
        } finally {
            _inlineDepth--
        }
    }
}
