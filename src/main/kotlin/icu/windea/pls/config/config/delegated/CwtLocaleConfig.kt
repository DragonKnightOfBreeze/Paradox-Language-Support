package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.ChronicleDocBundle
import icu.windea.pls.config.annotations.FromMember
import icu.windea.pls.config.annotations.FromName
import icu.windea.pls.config.config.CwtConfigResolverScope
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.optimized
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.util.ParadoxLocaleManager

/**
 * 语言环境规则。
 *
 * 用于提供语言环境（locale）的相关信息（快速文档、ID、语言代码等）。
 *
 * 插件基于这些规则，识别和推断可用的语言环境、偏好的语言环境以及上下文（如本地化文件）中的语言环境，从而改进 UI 展示、提示信息以及本地化校验逻辑。
 * 通用的规则分组中应声明所有全局的语言环境，其中部分可能不受当前游戏类型支持。
 *
 * 路径定位：
 * - `locales/{id}`。其中 `{id}` 匹配语言环境 ID。
 *
 * 示例：
 *
 * ```cwt
 * locales = {
 *     l_english = { codes = { "en" } }
 *     l_simp_chinese = { codes = { "zh-CN" } }
 * }
 * ```
 *
 * > CWTools 兼容性：不兼容。插件作为扩展提供。
 *
 * @property id 语言环境 ID（如 `l_english`）。
 * @property codes 此语言环境包含的语言代码列表（如 `en`、`zh-CN`）。
 * @property supports 此语言环境是否受当前游戏类型支持。
 * @property text 此语言环境的显示文本（依具体实现）。
 * @property shortId 去除前缀 `l_` 的简短 ID。
 * @property idWithText 带显示文本的 ID。
 */
interface CwtLocaleConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtIdMatchableConfig<CwtProperty> {
    @FromName
    val id: String
    @FromMember("codes: string[]")
    val codes: List<String>
    @FromMember("supports: boolean", defaultValue = "yes")
    val supports: Boolean

    val text: String
    val shortId: String get() = id.removePrefix("l_")
    val idWithText: String get() = if (text.isEmpty()) id else "$id ($text)"

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        /** 按当前 IDE/项目设置自动解析语言环境规则。 */
        @JvmStatic
        fun resolveAuto(): CwtLocaleConfig {
            return CwtLocaleConfigResolver.resolveAuto()
        }

        /** 按当前操作系统自动解析语言环境规则。 */
        @JvmStatic
        fun resolveAutoOs(): CwtLocaleConfig {
            return CwtLocaleConfigResolver.resolveAutoOs()
        }

        /** 解析为后备（fallback）语言环境规则。 */
        @JvmStatic
        fun resolveFallback(): CwtLocaleConfig {
            return CwtLocaleConfigResolver.resolveFallback()
        }

        /** 由属性规则解析为语言环境规则。 */
        @JvmStatic
        fun resolve(config: CwtPropertyConfig): CwtLocaleConfig? {
            return CwtLocaleConfigResolver.resolve(config)
        }
    }
}

// region Implementations

private object CwtLocaleConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    private val autoLocaleConfig = AutoCwtLocaleConfig(ParadoxLocaleManager.ID_AUTO)
    private val autoOsLocaleConfig = AutoCwtLocaleConfig(ParadoxLocaleManager.ID_AUTO_OS)
    private val fallbackLocaleConfig = FallbackCwtLocaleConfig(ParadoxLocaleManager.ID_FALLBACK)

    fun resolveAuto(): CwtLocaleConfig = autoLocaleConfig
    fun resolveAutoOs(): CwtLocaleConfig = autoOsLocaleConfig
    fun resolveFallback(): CwtLocaleConfig = fallbackLocaleConfig
    fun resolve(config: CwtPropertyConfig): CwtLocaleConfig? {
        val id = config.key
        val propConfigs = config.properties
        if (propConfigs == null) {
            logger.warn("Skipped invalid locale config (id: $id): Null properties.".withLocationPrefix(config))
            return null
        }

        val propGroup = propConfigs.groupBy { it.key }
        val codes = propGroup.getOne("codes")?.values?.mapNotNull { v -> v.stringValue }?.optimized().orEmpty()
        val supports = propGroup.getOne("supports")?.booleanValue ?: true
        logger.debug { "Resolved locale config (id: $id).".withLocationPrefix(config) }
        return CwtLocaleConfigImpl(config, id, codes, supports)
    }
}

private class CwtLocaleConfigImpl(
    override val config: CwtPropertyConfig,
    override val id: String,
    override val codes: List<String>,
    override val supports: Boolean,
) : UserDataHolderBase(), CwtLocaleConfig {
    override val text: String get() = ChronicleDocBundle.locale(id)

    override fun equals(other: Any?) = this === other || other is CwtLocaleConfig && id == other.id
    override fun hashCode() = id.hashCode()
    override fun toString() = "CwtLocaleConfig(id='$id')"
}

private class AutoCwtLocaleConfig(
    override val id: String
) : UserDataHolderBase(), CwtLocaleConfig {
    override val config: CwtPropertyConfig get() = throw UnsupportedOperationException() // as placeholder
    override val codes: List<String> get() = emptyList() // as placeholder
    override val supports: Boolean get() = false // as placeholder
    override val text: String get() = ChronicleDocBundle.locale(id)

    override fun equals(other: Any?) = this === other || other is CwtLocaleConfig && id == other.id
    override fun hashCode() = id.hashCode()
    override fun toString() = "AutoCwtLocaleConfig(id='$id')"
}

private class FallbackCwtLocaleConfig(
    override val id: String
) : UserDataHolderBase(), CwtLocaleConfig {
    override val config: CwtPropertyConfig get() = throw UnsupportedOperationException() // as placeholder
    override val codes: List<String> get() = emptyList() // as placeholder
    override val supports: Boolean get() = false // as placeholder
    override val text: String get() = ChronicleDocBundle.locale(id)

    override fun equals(other: Any?) = this === other || other is CwtLocaleConfig && id == other.id
    override fun hashCode() = id.hashCode()
    override fun toString() = "FallbackCwtLocaleConfig(id='$id')"
}

// endregion
