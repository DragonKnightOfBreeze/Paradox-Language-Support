package icu.windea.pls.config.config.internal

import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.internal.impl.CwtPostfixTemplateSettingsConfigResolverImpl
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * CWT 后缀模板设置规则（扩展规则）。
 *
 * 语义：
 * - 定义可用于编辑器“后缀模板/后缀补全”的条目，包括触发键、示例、变量与模板表达式。
 * - 该规则属于“脱离文件的扩展规则”，解析后按组归档至 `CwtConfigGroup.postfixTemplateSettings`。
 *
 * 字段：
 * - **id**：模板唯一标识（同组内唯一）。
 * - **key**：触发该模板的键（如 `.foo` 中的 `foo`）。
 * - **example**：展示用示例文本（可选）。
 * - **variables**：变量名到默认值的映射（可选）。
 * - **expression**：模板表达式字符串。
 *
 * 解析行为：
 * - 由 `Resolver` 从规则文件中读取 `key`/`example`/`variables`/`expression` 子项并构造。
 * - 仅当 `key` 与 `expression` 同时存在时才会生效、被注册。
 *
 * @property id 模板唯一标识（同组内唯一）
 * @property key 触发该模板的键
 * @property example 展示用示例文本（可选）
 * @property variables 变量名到默认值的映射（可选）
 * @property expression 模板表达式字符串
 */
data class CwtPostfixTemplateSettingsConfig(
    val id: String,
    val key: String,
    val example: String?,
    val variables: Map<String, String>, // variableName - defaultValue
    val expression: String
) : CwtDetachedConfig {
    /**
     * 解析器：从单个规则文件读取后缀模板设置并注册到配置组。
     *
     * 约定：
     * - 顶层以分组属性名为键；组内各子属性的键作为模板的 `id`。
     * - 子属性支持：`key`、`example`、`variables`（键值对）、`expression`。
     */
    interface Resolver {
        /**
         * 将文件中的后缀模板设置规则解析并注册到配置组。
         * @param fileConfig 源规则文件
         * @param configGroup 目标配置组
         */
        fun resolveInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup)
    }

    companion object : Resolver by CwtPostfixTemplateSettingsConfigResolverImpl()
}
