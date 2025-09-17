package icu.windea.pls.config.config

import icu.windea.pls.config.config.impl.CwtOptionValueConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement
import icu.windea.pls.cwt.psi.CwtOptionComment
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.model.CwtType

/**
 * 选项值规则。
 *
 * 对应 CWT 规则文件中的一个没有键的选项值（`## v`）。需要位于附加到成员上的选项注释中。
 *
 * 用于提供额外的选项数据，自身也可以嵌套下级选项和选项值，以提供更复杂的数据表述。
 * 在选项注释中单独使用时，常用来提供布尔标志或较短的语义标签。
 *
 * @see CwtMemberElement
 * @see CwtOptionComment
 * @see CwtValue
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessor
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessors
 */
interface CwtOptionValueConfig : CwtOptionMemberConfig<CwtValue> {
    interface Resolver {
        /**
         * 从 [value] 等信息解析生成规则；[valueType] 默认按字符串处理，可携带下级 [optionConfigs]。
         */
        fun resolve(
            value: String,
            valueType: CwtType = CwtType.String,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
        ): CwtOptionValueConfig
    }

    companion object : Resolver by CwtOptionValueConfigResolverImpl()
}
