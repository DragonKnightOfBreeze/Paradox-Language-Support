package icu.windea.pls.config.config

import icu.windea.pls.core.collections.process

// region CwtMemberConfig Extensions

// val CwtMemberConfig<*>.isRoot: Boolean
//    get() = when (this) {
//        is CwtPropertyConfig -> this.parentConfig == null
//        is CwtValueConfig -> this.parentConfig == null && this.propertyConfig == null
//    }

/**
 * 如果当前成员规则对应属性的值，则返回所属的属性规则。否则返回自身。
 */
val CwtMemberConfig<*>.memberConfig: CwtMemberConfig<*>
    get() = when (this) {
        is CwtPropertyConfig -> this
        is CwtValueConfig -> propertyConfig ?: this
    }

inline fun CwtMemberConfig<*>.processParent(processor: (CwtMemberConfig<*>) -> Boolean): Boolean {
    var parent = this.parentConfig
    while (parent != null) {
        val result = processor(parent)
        if (!result) return false
        parent = parent.parentConfig
    }
    return true
}

fun CwtMemberConfig<*>.processDescendants(processor: (CwtMemberConfig<*>) -> Boolean): Boolean {
    return doProcessDescendants(processor)
}

private fun CwtMemberConfig<*>.doProcessDescendants(processor: (CwtMemberConfig<*>) -> Boolean): Boolean {
    processor(this).also { if (!it) return false }
    this.configs?.process { it.doProcessDescendants(processor) }?.also { if (!it) return false }
    return true
}

// endregion

