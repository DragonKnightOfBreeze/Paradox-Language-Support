package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtMacroConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 宏规则。
 *
 * 用于描述宏的表达式格式并提供额外的用于校验的元数据，从而在脚本文件中提供代码高亮、引用解析、代码补全、代码检查等功能。
 * 这些表达式可以在脚本文件中的各种地方使用（不限于定义声明中），但是也存在特定的规则和限制。
 * 不同的宏拥有不同的用处与游戏运行时的处理逻辑。
 *
 * 目前仅适用于**定义注入（definition injections）**。
 *
 * 路径定位：`macro[{name}]`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * macro[definition_injection] = {
 *     modes = {
 *         # ...
 *     }
 *     # ...
 * }
 * ```
 *
 * @property name 名称。
 */
interface CwtMacroConfig: CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("macro[$]")
    val name: String
    @FromProperty("modes: string[]")
    val modeConfigs: Map<@CaseInsensitive String, CwtValueConfig>

    interface Resolver {
        /** 由属性规则解析为宏规则。*/
        fun resolve(config: CwtPropertyConfig): CwtMacroConfig?
    }

    companion object : Resolver by CwtMacroConfigResolverImpl()
}
