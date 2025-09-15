package icu.windea.pls.config.util.data

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig

/**
 * 选项数据访问器。
 *
 * 用于以统一而限定的方式，从附加到成员规则（[CwtMemberConfig]）上的一组选项（[CwtOptionMemberConfig]）中获取需要的选项数据。
 *
 * @see CwtMemberConfig
 * @see CwtOptionMemberConfig
 * @see CwtOptionDataAccessors
 */
fun interface CwtOptionDataAccessor<T> {
    fun get(config: CwtMemberConfig<*>): T
}
