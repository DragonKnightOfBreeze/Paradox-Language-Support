package icu.windea.pls.config.config

import icu.windea.pls.config.config.impl.CwtOptionConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.cwt.psi.CwtOptionComment
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType

/**
 * 选项规则。
 *
 * 对应 CWT 规则文件中的一个选项（`## k = v` 或 `## k = { ... }`）。需要位于附加到成员上的选项注释中。
 *
 * 用于提供额外的选项数据，自身也可以嵌套下级选项和选项值，以提供更复杂的数据表述。
 *
 * @property key 选项键。
 * @property value 选项值（去除首尾的双引号）。
 * @property valueType 选项值类型，用于后续解析与校验。
 * @property separatorType 分隔符类型。用于为作为条件的选项数据取正或取反。
 *
 * @see CwtMemberElement
 * @see CwtOptionComment
 * @see CwtOption
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessor
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessors
 */
interface CwtOptionConfig : CwtOptionMemberConfig<CwtOption> {
    val key: String
    val separatorType: CwtSeparatorType
    override val value: String
    override val valueType: CwtType

    interface Resolver {
        /**
         * 从 [key]/[value] 等信息解析生成规则；[separatorType] 默认 `=`，可携带下级 [optionConfigs]。
         */
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
