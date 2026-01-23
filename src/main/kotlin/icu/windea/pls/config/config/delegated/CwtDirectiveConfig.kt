package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtDirectiveConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
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
interface CwtDirectiveConfig: CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("directive[$]")
    val name: String
    @FromProperty("modes: string[]")
    val modeConfigs: Map<@CaseInsensitive String, CwtValueConfig>
    @FromProperty("relax_modes: string[]")
    val relaxModes: Set<@CaseInsensitive String>

    interface Resolver {
        /** 由属性规则解析为声明规则。 */
        fun resolve(config: CwtPropertyConfig): CwtDirectiveConfig?
    }

    companion object : Resolver by CwtDirectiveConfigResolverImpl()
}
