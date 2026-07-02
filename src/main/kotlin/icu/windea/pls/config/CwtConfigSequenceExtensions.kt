@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.config

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtMemberContainerConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.collections.orNull

// region Builders

fun CwtMemberContainerConfig<*>.members(): Sequence<CwtMemberConfig<*>> {
    return configs?.orNull()?.asSequence().orEmpty()
}

fun CwtMemberContainerConfig<*>.properties(): Sequence<CwtPropertyConfig> {
    return properties?.orNull()?.asSequence().orEmpty()
}

fun CwtMemberContainerConfig<*>.values(): Sequence<CwtValueConfig> {
    return values?.orNull()?.asSequence().orEmpty()
}

fun CwtMemberConfig<*>.parents(withSelf: Boolean = false): Sequence<CwtMemberConfig<*>> {
    val current = if(withSelf) this else this.parentConfig
    return generateSequence(current) { it.parentConfig }
}

// endregion
