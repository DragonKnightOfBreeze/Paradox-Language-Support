package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLocaleConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 语言环境规则（locale）。
 *
 * 概述：
 * - 描述一种语言环境 ID 与其变体代码列表，并提供便捷的派生字段用于 UI 展示。
 * - 由 `locale[id] = { codes = [...] }` 或相关扩展写法解析而来。
 *
 * 定位：
 * - 在 `FileBasedCwtConfigGroupDataProvider.processFile` 中，读取顶层键 `locales` 下的每个成员属性。
 * - 规则名取自成员属性键，即 `id`，如 `l_english`。
 *
 * 例：
 * ```cwt
 * # 来自 cwt/core/locales.core.cwt
 * locales = {
 *     l_english = {
 *         codes = { "en" }
 *     }
 * }
 * ```
 *
 * @property id 语言环境 ID（例如 `l_english`）。
 * @property codes 该语言环境包含的代码列表（如 `en`, `en-US` 等）。
 * @property text 该语言环境的展示文本（依具体实现）。
 * @property shortId 去除前缀 `l_` 的简短 ID。
 * @property idWithText 带展示文本的 ID（若存在）。
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
        /** 按当前 IDE/项目设置自动解析语言环境规则。*/
        fun resolveAuto(): CwtLocaleConfig
        /** 按当前操作系统自动解析语言环境规则。*/
        fun resolveAutoOs(): CwtLocaleConfig
        /** 解析为后备（fallback）语言环境规则。*/
        fun resolveFallback(): CwtLocaleConfig
        /** 由成员属性规则解析为语言环境规则。*/
        fun resolve(config: CwtPropertyConfig): CwtLocaleConfig
    }

    companion object : Resolver by CwtLocaleConfigResolverImpl()
}
