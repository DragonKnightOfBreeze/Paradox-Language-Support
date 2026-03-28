package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.configExpression.CwtLocationExpression
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 类型展示规则。
 *
 * 用于定位对应类型的定义的相关本地化与图片，以便在 UI 与各种提示信息中展示。
 *
 * @see CwtLocationConfig
 * @see CwtLocationExpression
 */
sealed interface CwtTypePresentationConfig: CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val locationConfigs: List<Pair<String?, CwtLocationConfig>> // (subtypeExpression, locationConfig)

    /** 按给定的 [subtypes] 合并与筛选后，返回生效的位置规则列表。 */
    fun getConfigs(subtypes: List<String>): List<CwtLocationConfig>
}
