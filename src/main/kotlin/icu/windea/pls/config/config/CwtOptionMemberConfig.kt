package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.CwtMemberElement
import icu.windea.pls.cwt.psi.CwtOptionComment
import icu.windea.pls.cwt.psi.CwtOptionMemberElement
import icu.windea.pls.model.CwtType

/**
 * 选项成员规则。
 *
 * 对应 CWT 规则文件中的一个选项（`## k = v` 或 `## k = { ... }`）或选项值（`## v`）。需要位于附加到成员上的选项注释中。
 *
 * 用于提供额外的选项数据，自身也可以嵌套下级选项和选项值，以提供更复杂的数据表述。
 *
 * @property value 选项值（去除首尾的双引号）。
 * @property valueType 选项值类型，用于后续解析与校验。
 * @property optionConfigs 子规则列表（其中嵌套的下级选项和选项值对应的规则）。
 *
 * @see CwtMemberElement
 * @see CwtOptionComment
 * @see CwtOptionMemberElement
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessor
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessors
 */
sealed interface CwtOptionMemberConfig<out T : CwtOptionMemberElement> : CwtDetachedConfig {
    val value: String
    val valueType: CwtType
    val optionConfigs: List<CwtOptionMemberConfig<*>>?

    override fun toString(): String
}
