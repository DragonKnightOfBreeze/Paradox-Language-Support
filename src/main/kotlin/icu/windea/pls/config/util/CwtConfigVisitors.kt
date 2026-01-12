package icu.windea.pls.config.util

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.collections.process

abstract class CwtMemberConfigVisitor {
    open fun visit(config: CwtMemberConfig<*>): Boolean {
        for (child in config.configs.orEmpty()) {
            if (!visit(child)) return false
        }
        return visitFinished(config)
    }

    open fun visitFinished(config: CwtMemberConfig<*>): Boolean {
        return true
    }
}

abstract class CwtPropertyConfigVisitor {
    open fun visit(config: CwtPropertyConfig): Boolean {
        for (child in config.properties.orEmpty()) {
            if (!visit(child)) return false
        }
        return visitFinished(config)
    }

    open fun visitFinished(config: CwtPropertyConfig): Boolean {
        return true
    }
}

abstract class CwtValueConfigVisitor {
    open fun visit(config: CwtValueConfig): Boolean {
        for (child in config.values.orEmpty()) {
            if (!visit(child)) return false
        }
        return visitFinished(config)
    }

    open fun visitFinished(config: CwtValueConfig): Boolean {
        return true
    }
}

fun CwtMemberConfig<*>.accept(visitor: CwtMemberConfigVisitor): Boolean {
    return visitor.visit(this)
}

fun CwtPropertyConfig.accept(visitor: CwtPropertyConfigVisitor): Boolean {
    return visitor.visit(this)
}

fun CwtValueConfig.accept(visitor: CwtValueConfigVisitor): Boolean {
    return visitor.visit(this)
}

fun CwtMemberConfig<*>.acceptChildren(visitor: CwtMemberConfigVisitor): Boolean {
    return configs.orEmpty().process { visitor.visit(it) }
}

fun CwtMemberConfig<*>.acceptChildren(visitor: CwtPropertyConfigVisitor): Boolean {
    return properties.orEmpty().process { visitor.visit(it) }
}

fun CwtMemberConfig<*>.acceptChildren(visitor: CwtValueConfigVisitor): Boolean {
    return values.orEmpty().process { visitor.visit(it) }
}
