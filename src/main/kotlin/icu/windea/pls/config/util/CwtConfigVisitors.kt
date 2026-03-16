@file:Suppress("unused")

package icu.windea.pls.config.util

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator

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
    @PublishedApi internal var _inlineDepth: Int = 0

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

    open fun visitInlinedProperty(config: CwtPropertyConfig): Boolean {
        return CwtConfigManipulator.visitInlined(config, forSingleAlias, forAlias, this)
    }

    open fun visitInlinedValue(config: CwtValueConfig): Boolean {
        return CwtConfigManipulator.visitInlined(config, forSingleAlias, forAlias, this)
    }

    inline fun <T> withInlineDepthIncrement(action: () -> T): T {
        return try {
            _inlineDepth++
            action()
        } finally {
            _inlineDepth--
        }
    }
}
