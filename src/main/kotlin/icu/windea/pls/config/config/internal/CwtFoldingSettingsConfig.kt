package icu.windea.pls.config.config.internal

import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.internal.impl.CwtFoldingSettingsConfigResolverImpl
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * CWT 折叠设置规则（扩展规则）。
 *
 * 语义：
 * - 描述哪些键对应的块在编辑器中支持代码折叠，以及折叠时显示的占位文本。
 * - 该规则属于“脱离文件的扩展规则”，解析后按组归档至 `CwtConfigGroup.foldingSettings`。
 *
 * 字段：
 * - **id**：折叠项唯一标识（同组内唯一）。
 * - **key**：单个键名；与 `keys` 二选一。
 * - **keys**：多个键名；与 `key` 二选一。
 * - **placeholder**：折叠后显示的占位文本。
 *
 * 解析行为：
 * - 由 `Resolver` 从规则文件中读取 `key`/`keys`/`placeholder` 子项并构造。
 * - 实现层允许 `key` 与 `keys` 为空，最终是否生效由使用方判定。
 *
 * @property id 折叠项唯一标识（同组内唯一）
 * @property key 单个键名；与 `keys` 二选一
 * @property keys 多个键名；与 `key` 二选一
 * @property placeholder 折叠后显示的占位文本
 */
data class CwtFoldingSettingsConfig(
    /**
     * 折叠项唯一标识（同组内唯一）
     */
    val id: String,
    /**
     * 单个键名；与 `keys` 二选一
     */
    val key: String?,
    /**
     * 多个键名；与 `key` 二选一
     */
    val keys: List<String>?,
    /**
     * 折叠后显示的占位文本
     */
    val placeholder: String
) : CwtDetachedConfig {
    /**
     * 解析器：从单个规则文件中读取折叠设置并注册到配置组。
     *
     * 约定：
     * - 顶层以分组属性名为键；组内各子属性的键作为折叠项的 `id`。
     * - 子属性支持：`key`、`keys`、`placeholder`。
     */
    interface Resolver {
        /**
         * 将文件中的折叠设置规则解析并注册到配置组。
         * @param fileConfig 源规则文件
         * @param configGroup 目标配置组
         */
        fun resolveInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup)
    }

    companion object : Resolver by CwtFoldingSettingsConfigResolverImpl()
}
