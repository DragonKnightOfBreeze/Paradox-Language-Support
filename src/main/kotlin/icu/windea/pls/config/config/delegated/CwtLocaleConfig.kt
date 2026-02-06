package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.PlsDocBundle
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.optimized
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.util.ParadoxLocaleManager

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
        /** 按当前 IDE/项目设置自动解析语言环境规则。 */
        fun resolveAuto(): CwtLocaleConfig
        /** 按当前操作系统自动解析语言环境规则。 */
        fun resolveAutoOs(): CwtLocaleConfig
        /** 解析为后备（fallback）语言环境规则。 */
        fun resolveFallback(): CwtLocaleConfig
        /** 由属性规则解析为语言环境规则。 */
        fun resolve(config: CwtPropertyConfig): CwtLocaleConfig
    }

    companion object : Resolver by CwtLocaleConfigResolverImpl()
}

// region Implementations

private class CwtLocaleConfigResolverImpl : CwtLocaleConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    private val autoLocaleConfig = AutoCwtLocaleConfig(ParadoxLocaleManager.ID_AUTO)
    private val autoOsLocaleConfig = AutoCwtLocaleConfig(ParadoxLocaleManager.ID_AUTO_OS)
    private val fallbackLocaleConfig = FallbackCwtLocaleConfig(ParadoxLocaleManager.ID_FALLBACK)

    override fun resolveAuto(): CwtLocaleConfig = autoLocaleConfig
    override fun resolveAutoOs(): CwtLocaleConfig = autoOsLocaleConfig
    override fun resolveFallback(): CwtLocaleConfig = fallbackLocaleConfig
    override fun resolve(config: CwtPropertyConfig): CwtLocaleConfig = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtLocaleConfig {
        val id = config.key
        val codes = config.properties?.find { p -> p.key == "codes" }?.values?.mapNotNull { v -> v.stringValue }?.optimized().orEmpty()
        logger.debug { "Resolved locale config (id: $id).".withLocationPrefix(config) }
        return CwtLocaleConfigImpl(config, id, codes)
    }
}

private class CwtLocaleConfigImpl(
    override val config: CwtPropertyConfig,
    override val id: String,
    override val codes: List<String>
) : UserDataHolderBase(), CwtLocaleConfig {
    override val text: String get() = PlsDocBundle.locale(id)

    override fun equals(other: Any?) = this === other || other is CwtLocaleConfig && id == other.id
    override fun hashCode() = id.hashCode()
    override fun toString() = "CwtLocaleConfig(id='$id')"
}

private class AutoCwtLocaleConfig(
    override val id: String
) : UserDataHolderBase(), CwtLocaleConfig {
    override val config: CwtPropertyConfig get() = throw UnsupportedOperationException()
    override val codes: List<String> get() = emptyList()
    override val text: String get() = PlsDocBundle.locale(id)

    override fun equals(other: Any?) = this === other || other is CwtLocaleConfig && id == other.id
    override fun hashCode() = id.hashCode()
    override fun toString() = "AutoCwtLocaleConfig(id='$id')"
}

private class FallbackCwtLocaleConfig(
    override val id: String
) : UserDataHolderBase(), CwtLocaleConfig {
    override val config: CwtPropertyConfig get() = throw UnsupportedOperationException()
    override val codes: List<String> get() = emptyList()
    override val text: String get() = PlsDocBundle.locale(id)

    override fun equals(other: Any?) = this === other || other is CwtLocaleConfig && id == other.id
    override fun hashCode() = id.hashCode()
    override fun toString() = "FallbackCwtLocaleConfig(id='$id')"
}

// endregion
