package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLocalisationCommandConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 本地化命令规则：描述可在本地化文本中调用的命令及其可接受的输入作用域。
 *
 * - 示例来源：`cwtools-stellaris-config/config/localisation.cwt` 的 `localisation_commands`。
 * - 使用方式：如 `[Root.GetName]`、`[This.GetAdj]` 等，其中 `GetName`/`GetAdj` 即命令名。
 *
 * 字段：
 * - `name`: 命令名。
 * - `supportedScopes`: 允许作为调用上下文的脚本作用域集合（如 `country`、`planet` 等）。
 */
interface CwtLocalisationCommandConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromOption(": string | string[]")
    val supportedScopes: Set<String>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtLocalisationCommandConfig
    }

    companion object : Resolver by CwtLocalisationCommandConfigResolverImpl()
}
