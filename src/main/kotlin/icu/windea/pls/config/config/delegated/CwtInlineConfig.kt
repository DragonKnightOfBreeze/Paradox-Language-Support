package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtInlineConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 内联脚本规则：`inline[inline_script] = <expr | { ... }>`。
 *
 * - 用于在脚本中调用事先定义的“内联脚本”，以复用一段可参数化的脚本语句。
 * - CWT 示例见 `common/inline_scripts.cwt`：
 *   - `inline[inline_script] = filepath[common/inline_scripts/,.txt]`
 *   - `inline[inline_script] = { script = filepath[common/inline_scripts/,.txt]; $param = $value; ... }`
 * - 在 PLS 中，[inline] 会被解析为一个可直接替换使用的 [CwtPropertyConfig]（即“展开后的规则”）。
 *
 * 字段说明：
 * - `name`: 从键 `inline[$]` 解析得到的脚本名。
 * - `inline()`: 生成（或返回缓存的）展开后的属性规则，用于在使用处进行规则匹配与校验。
 */
interface CwtInlineConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("inline[$]")
    val name: String

    fun inline(): CwtPropertyConfig

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtInlineConfig?
    }

    companion object : Resolver by CwtInlineConfigResolverImpl()
}
