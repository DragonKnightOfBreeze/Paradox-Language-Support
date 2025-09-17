package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLocaleConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 语言环境规则。
 *
 * 用于提供语言环境（locale）的相关信息（快速文档、ID、代码等）。
 *
 * PLS 基于这些规则，识别和推断上下文（如本地化文件）中的语言环境，或用户偏好的语言环境，
 * 以提供更恰当的 UI 展示与提示信息。
 *
 * 路径定位：`locales/{id}`，`{name}` 匹配语言环境 ID。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * locales = {
 *     l_english = {
 *         codes = { "en" }
 *     }
 *     # ...
 * }
 * ```
 *
 * @property id 语言环境 ID（例如 `l_english`）。
 * @property codes 该语言环境包含的代码列表（如 `en`, `en-US` 等）。
 * @property text 该语言环境的展示文本（依具体实现）。
 * @property shortId 去除前缀 `l_` 的简短 ID。
 * @property idWithText 带展示文本的 ID。
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
        /** 由属性规则解析为语言环境规则。*/
        fun resolve(config: CwtPropertyConfig): CwtLocaleConfig
    }

    companion object : Resolver by CwtLocaleConfigResolverImpl()
}
