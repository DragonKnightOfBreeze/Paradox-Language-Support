package icu.windea.pls.config.config

import icu.windea.pls.config.config.impl.CwtOptionValueConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.model.CwtType

/**
 * CWT 选项值规则（仅值形式）。
 *
 * 与 [CwtOptionConfig] 相比，此类型只包含“值”，常用于像 `scopes: string[]` 中的数组元素等场景。
 */
interface CwtOptionValueConfig : CwtOptionMemberConfig<CwtOption> {
    /**
     * 解析器接口：用于在构建规则模型时创建“选项值规则”对象。
     */
    interface Resolver {
        fun resolve(
            value: String,
            valueType: CwtType = CwtType.String,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = null
        ): CwtOptionValueConfig
    }

    companion object : Resolver by CwtOptionValueConfigResolverImpl()
}
