package icu.windea.pls.config.util.data

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig

/**
 * 用于从附加到规则（[CwtMemberConfig]）上的一组选项（[CwtOptionMemberConfig]）中获取需要的规则数据。
 *
 * @see CwtMemberConfig
 * @see CwtOptionMemberConfig
 */
fun interface CwtOptionDataAccessor<T> {
    fun get(config: CwtMemberConfig<*>): T
}
