@file:Suppress("unused")

package icu.windea.pls.config

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.model.paths.CwtConfigPath

object CwtConfigSearchScope

@Suppress("UnusedReceiverParameter")
inline fun <T : CwtMemberConfig<*>, R> T.search(block: context(CwtConfigSearchScope) T.() -> R): R {
    return with(CwtConfigSearchScope) { block() }
}

context(_: CwtConfigSearchScope)
fun CwtMemberConfig<*>.property(key: String, ignoreCase: Boolean = false): CwtPropertyConfig? {
    return properties?.find { it.key.equals(key, ignoreCase) }
}

context(_: CwtConfigSearchScope)
fun CwtMemberConfig<*>.property(predicate: (String) -> Boolean): CwtPropertyConfig? {
    return properties?.find { predicate(it.key) }
}

/**
 * @see CwtConfigPath
 */
context(_: CwtConfigSearchScope)
fun CwtMemberConfig<*>.propertyByPath(path: CwtConfigPath, ignoreCase: Boolean = false): CwtPropertyConfig? {
    if (path.isEmpty()) return null
    var current = this
    for (key in path) {
        current = current.property(key, ignoreCase) ?: return null
    }
    return current as? CwtPropertyConfig
}

/**
 * @see CwtConfigPath
 */
context(_: CwtConfigSearchScope)
fun CwtMemberConfig<*>.propertyByPath(path: String, ignoreCase: Boolean = false): CwtPropertyConfig? {
    return this.propertyByPath(CwtConfigPath.resolve(path), ignoreCase)
}
