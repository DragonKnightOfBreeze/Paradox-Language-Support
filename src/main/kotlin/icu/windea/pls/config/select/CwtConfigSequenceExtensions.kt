@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.config.select

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtMemberContainerConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.collections.orNull

// Builders

inline fun CwtMemberContainerConfig<*>.members(): Sequence<CwtMemberConfig<*>> {
    return configs?.orNull()?.asSequence().orEmpty()
}

inline fun CwtMemberContainerConfig<*>.properties(): Sequence<CwtPropertyConfig> {
    return properties?.orNull()?.asSequence().orEmpty()
}

inline fun CwtMemberContainerConfig<*>.values(): Sequence<CwtValueConfig> {
    return values?.orNull()?.asSequence().orEmpty()
}
