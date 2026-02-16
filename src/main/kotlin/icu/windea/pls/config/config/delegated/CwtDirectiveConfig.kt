package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.caseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 指令规则。
 *
 * 用于描述脚本文件中区别于一般抽象的特殊的表达式和结构，并提供额外的用于提示和验证的元数据。
 * 这些表达式和结构会改变游戏运行时的脚本解析器的行为，从而改变、扩展或复用已有的脚本片段。
 * 不同的指令可以拥有不同的规则结构。
 *
 * 目前涉及的语言特性：
 * - **内联脚本（inline_script）**：（Sellaris）会在解析阶段被替换为目标文件的内容，且可以指定参数。
 * - **定义注入（definition_injection）**：（VIC3 / EU5）会在解析阶段对目标定义的声明进行注入或替换，且可以指定模式以决定具体行为。
 *
 * 路径定位：`directive[{name}]`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * directive[inline_script] = {
 *     # ...
 * }
 * ```
 *
 * @property name 名称。
 */
interface CwtDirectiveConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("directive[$]")
    val name: String
    @FromProperty("modes: string[]")
    val modeConfigs: Map<@CaseInsensitive String, CwtValueConfig>
    @FromProperty("relax_modes: string[]")
    val relaxModes: Set<@CaseInsensitive String>
    @FromProperty("self_subtype_modes: string[]")
    val selfSubtypeModes: Set<@CaseInsensitive String>

    interface Resolver {
        /** 由属性规则解析为声明规则。 */
        fun resolve(config: CwtPropertyConfig): CwtDirectiveConfig?
    }

    companion object : Resolver by CwtDirectiveConfigResolverImpl()
}

// region Implementations

private class CwtDirectiveConfigResolverImpl : CwtDirectiveConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtDirectiveConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtDirectiveConfig? {
        val name = config.key.removeSurroundingOrNull("directive[", "]")?.orNull()?.optimized() ?: return null
        val propElements = config.properties.orEmpty()
        val propGroup = propElements.groupBy { it.key }
        val modeConfigs = propGroup.getOne("modes")?.let { prop ->
            prop.values?.associateByTo(caseInsensitiveStringKeyMap()) { it.stringValue }
        }?.optimized().orEmpty()
        val relaxModes = propGroup.getOne("relax_modes")?.let { prop ->
            prop.values?.mapNotNullTo(caseInsensitiveStringSet()) { it.stringValue }
        }?.optimized().orEmpty()
        val selfSubtypeModes = propGroup.getOne("self_subtype_modes")?.let { prop ->
            prop.values?.mapNotNullTo(caseInsensitiveStringSet()) { it.stringValue }
        }?.optimized().orEmpty()
        logger.debug { "Resolved directive config (name: $name).".withLocationPrefix(config) }
        return CwtDirectiveConfigImpl(config, name, modeConfigs, relaxModes, selfSubtypeModes)
    }
}

private class CwtDirectiveConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val modeConfigs: Map<String, CwtValueConfig>,
    override val relaxModes: Set<String>,
    override val selfSubtypeModes: Set<String>,
) : UserDataHolderBase(), CwtDirectiveConfig {
    override fun toString() = "CwtDirectiveConfigImpl(name='$name')"
}

// endregion
