package icu.windea.pls.config

import icu.windea.pls.config.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.model.paths.*

fun CwtMemberConfig<*>.findProperty(key: String, ignoreCase: Boolean = false): CwtPropertyConfig? {
    return configs?.findIsInstance<CwtPropertyConfig> { it.key.equals(key, ignoreCase) }
}

fun CwtMemberConfig<*>.findProperty(predicate: (String) -> Boolean): CwtPropertyConfig? {
    return configs?.findIsInstance<CwtPropertyConfig> { predicate(it.key) }
}

fun CwtMemberConfig<*>.findPropertyByPath(path: CwtConfigPath, ignoreCase: Boolean = false): CwtPropertyConfig? {
    if (path.isEmpty()) return null
    var current: CwtMemberConfig<*>? = this
    for (key in path) {
        current = current?.findProperty(key, ignoreCase) ?: return null
    }
    return current as? CwtPropertyConfig
}
