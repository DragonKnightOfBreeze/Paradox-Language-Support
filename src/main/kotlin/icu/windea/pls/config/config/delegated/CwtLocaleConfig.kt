package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLocaleConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 本地化语言规则：描述可用的语言标识及其代码集。
 *
 * - 用于选择 IDE/工具的当前语言环境，以及解析本地化文件后缀（如 `l_english`）。
 * - 解析器提供多种自动选择策略：`resolveAuto()`（按照项目/设置）、`resolveAutoOs()`（根据操作系统）、`resolveFallback()`（回退）。
 *
 * 字段：
 * - `id`: 语言 ID（如 `l_english`）。
 * - `codes`: 语言代码列表（如 `en`、`en-US`）。
 *
 * 扩展属性：
 * - `text`: 显示名称（本地化文本）。
 * - `shortId`: 去除前缀 `l_` 后的短 ID。
 * - `idWithText`: 组合显示字符串，如 `l_english (English)`。
 */
interface CwtLocaleConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val id: String
    @FromProperty("codes: string[]")
    val codes: List<String>

    val text: String
    val shortId: String get() = id.removePrefix("l_")
    val idWithText: String get() = if (text.isEmpty()) id else "$id ($text)"

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolveAuto(): CwtLocaleConfig
        fun resolveAutoOs(): CwtLocaleConfig
        fun resolveFallback(): CwtLocaleConfig
        fun resolve(config: CwtPropertyConfig): CwtLocaleConfig
    }

    companion object : Resolver by CwtLocaleConfigResolverImpl()
}
