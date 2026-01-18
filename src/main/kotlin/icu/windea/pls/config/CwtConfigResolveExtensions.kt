package icu.windea.pls.config

import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.model.CwtMemberType

// Config Resolve Extensions

@Suppress("NOTHING_TO_INLINE")
inline fun CwtMemberConfig<*>.isProperty() = this.memberType == CwtMemberType.PROPERTY

@Suppress("NOTHING_TO_INLINE")
inline fun CwtMemberConfig<*>.isValue() = this.memberType == CwtMemberType.VALUE

@Suppress("unused")
inline fun <R> CwtMemberConfig<*>.whenProperty(block: (CwtPropertyConfig) -> R): R? {
    return if (isProperty()) block(this as CwtPropertyConfig) else null
}

@Suppress("unused")
inline fun <R> CwtMemberConfig<*>.whenValue(block: (CwtValueConfig) -> R): R? {
    return if (isValue()) block(this as CwtValueConfig) else null
}

/** 解析为被内联的规则，或者返回自身。 */
@Suppress("UNCHECKED_CAST")
fun <T : CwtConfig<*>> T.resolved(): T {
    return when {
        this is CwtPropertyConfig -> inlineConfig?.config ?: aliasConfig?.config ?: this
        else -> this
    } as T
}

/** 解析为被内联的规则，或者返回 `null`。 */
@Suppress("UNCHECKED_CAST")
fun <T : CwtConfig<*>> T.resolvedOrNull(): T? {
    return when {
        this is CwtPropertyConfig -> inlineConfig?.config ?: aliasConfig?.config
        else -> this
    } as? T
}

// Value Resolve Extensions

fun CwtMemberConfig<*>.selectLiteralValue(): String? {
    return if (configs == null) value else null
}
