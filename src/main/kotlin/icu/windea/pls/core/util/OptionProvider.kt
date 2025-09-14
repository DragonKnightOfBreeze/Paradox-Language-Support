package icu.windea.pls.core.util

import icu.windea.pls.core.orNull

/**
 * 用于从给定的值、默认值、环境变量、默认环境变量中，获取最终需要的选项值。
 */
@Suppress("unused")
data class OptionProvider<T : String?>(
    val value: T?,
    val defaultValue: T,
    val fromEnv: Boolean = false,
    val env: String? = null,
    val defaultEnv: String? = null,
) {
    @Suppress("UNCHECKED_CAST")
    fun get(): T {
        if (fromEnv) {
            val envKey = env?.orNull() ?: defaultEnv?.orNull()
            envKey?.let { System.getenv(it)?.orNull() }?.let { return it as T }
        }
        value?.orNull()?.let { return it }
        return defaultValue
    }

    fun fromEnv(env: String?, defaultEnv: String? = null): OptionProvider<T> {
        return copy(env = env, defaultEnv = defaultEnv)
    }

    fun fromEnv(fromEnv: Boolean, env: String?, defaultEnv: String? = null): OptionProvider<T> {
        return copy(fromEnv = fromEnv, env = env, defaultEnv = defaultEnv)
    }

    companion object {
        fun <T : String?> from(value: T?, defaultValue: T): OptionProvider<T> {
            return OptionProvider(value, defaultValue)
        }
    }
}
