package icu.windea.pls.core.util

import icu.windea.pls.core.orNull

/**
 * 选项值提供器。
 *
 * 用于从给定的值、默认值、环境变量、默认环境变量中，获取最终需要的选项值。
 *
 * 基于以下优先级返回选项值：
 * 1. 当 [fromEnv] 为 `true` 时，优先读取环境变量 [env]（若为空则读 [defaultEnv]）；
 * 2. 若环境变量不存在或为空，则返回 [value]；
 * 3. 否则返回 [defaultValue]。
 *
 * 读取到的空字符串会被视为缺省（等价于 `null`）。
 */
@Suppress("unused")
data class OptionProvider<T : String?>(
    val value: T?,
    val defaultValue: T,
    val fromEnv: Boolean = false,
    val env: String? = null,
    val defaultEnv: String? = null,
) {
    /** 按优先级计算最终的选项值。*/
    @Suppress("UNCHECKED_CAST")
    fun get(): T {
        if (fromEnv) {
            val envKey = env?.orNull() ?: defaultEnv?.orNull()
            envKey?.let { System.getenv(it)?.orNull() }?.let { return it as T }
        }
        value?.orNull()?.let { return it }
        return defaultValue
    }

    /** 指定环境变量键 [env] 与可选默认键 [defaultEnv]，返回新的提供器（不修改 [fromEnv]）。*/
    fun fromEnv(env: String?, defaultEnv: String? = null): OptionProvider<T> {
        return copy(env = env, defaultEnv = defaultEnv)
    }

    /** 同时设置是否启用环境变量 [fromEnv] 与键名。*/
    fun fromEnv(fromEnv: Boolean, env: String?, defaultEnv: String? = null): OptionProvider<T> {
        return copy(fromEnv = fromEnv, env = env, defaultEnv = defaultEnv)
    }

    companion object {
        /** 基于值与默认值创建提供器。*/
        fun <T : String?> from(value: T?, defaultValue: T): OptionProvider<T> {
            return OptionProvider(value, defaultValue)
        }
    }
}
