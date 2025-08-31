package icu.windea.pls.config.config

import icu.windea.pls.config.config.impl.CwtOptionConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType

/**
 * CWT 选项规则（键值形式）。
 *
 * 通常以 `key <sep> value` 的形式出现，用于修饰/限定某个成员规则（属性/值）的语义。
 *
 * @property key 选项键。
 * @property separatorType 分隔符类型 [CwtSeparatorType]。
 */
interface CwtOptionConfig : CwtOptionMemberConfig<CwtOption> {
    val key: String
    val separatorType: CwtSeparatorType

    /**
     * 解析器接口：用于在构建规则模型时创建选项规则对象。
     */
    interface Resolver {
        fun resolve(
            key: String,
            value: String,
            valueType: CwtType = CwtType.String,
            separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
        ): CwtOptionConfig
    }

    companion object : Resolver by CwtOptionConfigResolverImpl()
}
