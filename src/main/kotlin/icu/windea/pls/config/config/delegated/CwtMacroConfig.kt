package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.annotations.FromMember
import icu.windea.pls.config.annotations.FromName
import icu.windea.pls.config.config.CwtConfigResolverScope
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.manipulation.CwtConfigManipulationService
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.caseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 宏规则。
 *
 * 用于描述脚本文件中区别于一般抽象的特殊的语言构造（表达式、语句等），并提供额外的用于提示和验证的元数据。
 * 这些语言构造会改变游戏运行时的脚本解析器的行为，从而改变、扩展或复用已有的脚本片段。
 * 不同的宏可以拥有不同的规则结构。
 *
 * 目前涉及的宏包括：
 * - **内联脚本（inline_script）**：（Stellaris）会在解析阶段被替换为目标文件的内容，且可以指定参数。
 * - **定义注入（definition_injection）**：（VIC3 / EU5）会在解析阶段对目标定义的声明进行注入或替换，且可以指定模式以决定具体行为。
 *
 * 路径定位：
 * - `macro[{name}]`。其中 `{name}` 匹配规则名称。
 *
 * 示例：
 *
 * ```cwt
 * macro[inline_script] = {
 *     # ...
 * }
 * ```
 *
 * > CWTools 兼容性：不兼容。插件作为扩展提供。
 *
 * @property name 规则名称。
 */
interface CwtMacroConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtIdMatchableConfig<CwtProperty> {
    @FromName("macro[$]")
    val name: String

    /**
     * 内联脚本的宏规则。为内联脚本用法提供快速文档和规则上下文。
     *
     * @see icu.windea.pls.config.manipulation.CwtConfigManipulationService.inlineMacro
     */
    interface InlineScript : CwtMacroConfig {
        val configForDeclaration: CwtPropertyConfig
    }

    /**
     * 定义注入的宏规则，为定义注入模式提供快速文档与行为归类。
     */
    interface DefinitionInjection : CwtMacroConfig {
        @FromMember("modes: string[]")
        val modeConfigs: Map<@CaseInsensitive String, CwtValueConfig>
        @FromMember("lenient_modes: string[]")
        val lenientModes: Set<@CaseInsensitive String>
        @FromMember("replace_modes: string[]")
        val replaceModes: Set<@CaseInsensitive String>
        @FromMember("create_modes: string[]")
        val createModes: Set<@CaseInsensitive String>
    }

    companion object {
        /** 由属性规则解析为声明规则。 */
        @JvmStatic
        fun resolve(config: CwtPropertyConfig): CwtMacroConfig? {
            return CwtMacroConfigResolver.resolve(config)
        }
    }
}

// region Implementations

private object CwtMacroConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun resolve(config: CwtPropertyConfig): CwtMacroConfig? {
        val name = config.key.removeSurroundingOrNull("macro[", "]")?.orNull()?.optimized()
            ?: config.key.removeSurroundingOrNull("directive[", "]")?.orNull()?.optimized() // stay compatible
            ?: return null
        return when (name) {
            "inline_script" -> {
                logger.debug { "Resolved macro config for inline scripts (name: $name).".withLocationPrefix(config) }
                CwtInlineScriptMacroConfig(config, name)
            }
            "definition_injection" -> {
                val propConfigs = config.properties.orEmpty()
                val propGroup = propConfigs.groupBy { it.key }
                val modeConfigs = propGroup.getOne("modes")?.let { prop ->
                    prop.values?.associateByTo(caseInsensitiveStringKeyMap()) { it.stringValue }
                }?.optimized().orEmpty()
                val lenientModes = propGroup.getOne("lenient_modes")?.let { prop ->
                    prop.values?.mapNotNullTo(caseInsensitiveStringSet()) { it.stringValue }
                }?.optimized().orEmpty()
                val replaceModes = propGroup.getOne("replace_modes")?.let { prop ->
                    prop.values?.mapNotNullTo(caseInsensitiveStringSet()) { it.stringValue }
                }?.optimized().orEmpty()
                val createModes = propGroup.getOne("create_modes")?.let { prop ->
                    prop.values?.mapNotNullTo(caseInsensitiveStringSet()) { it.stringValue }
                }?.optimized().orEmpty()
                logger.debug { "Resolved macro config for definition injections (name: $name).".withLocationPrefix(config) }
                CwtDefinitionInjectionMacroConfig(config, name, modeConfigs, lenientModes, replaceModes, createModes)
            }
            else -> {
                logger.debug { "Resolved macro config (name: $name).".withLocationPrefix(config) }
                CwtMacroConfigImpl(config, name)
            }
        }
    }
}

private class CwtMacroConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
) : UserDataHolderBase(), CwtMacroConfig {
    override fun toString() = "CwtMacroConfigImpl(name='$name')"
}

private class CwtInlineScriptMacroConfig(
    override val config: CwtPropertyConfig,
    override val name: String,
) : UserDataHolderBase(), CwtMacroConfig.InlineScript {
    override val configForDeclaration: CwtPropertyConfig by lazy { computeConfigForDeclaration() }

    private fun computeConfigForDeclaration(): CwtPropertyConfig {
        return CwtConfigManipulationService.inlineSingleAlias(config) ?: config
    }

    override fun toString() = "CwtInlineScriptMacroConfig(name='$name')"
}

private class CwtDefinitionInjectionMacroConfig(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val modeConfigs: Map<String, CwtValueConfig>,
    override val lenientModes: Set<String>,
    override val replaceModes: Set<String>,
    override val createModes: Set<String>,
) : UserDataHolderBase(), CwtMacroConfig.DefinitionInjection {
    override fun toString() = "CwtDefinitionInjectionMacroConfig(name='$name')"
}

// endregion
