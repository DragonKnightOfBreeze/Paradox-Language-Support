@file:Suppress("unused")

package icu.windea.pls.config.util

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig

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
