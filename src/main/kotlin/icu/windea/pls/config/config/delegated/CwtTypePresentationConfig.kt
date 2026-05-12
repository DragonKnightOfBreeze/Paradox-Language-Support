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
 * @property locationConfigGroup 子类型表达式到一组位置规则的映射。如果子类型表达式为空，则表示适用于所有子类型。
 *
 * @see CwtTypeLocalisationConfig
 * @see CwtTypeImagesConfig
 * @see CwtLocationConfig
 * @see CwtLocationExpression
 */
sealed interface CwtTypePresentationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val locationConfigGroup: Map<String, List<CwtLocationConfig>>

    /** 按给定的 [subtypes] 合并与筛选后，返回生效的位置规则列表。 */
    fun getLocationConfigs(subtypes: List<String>): List<CwtLocationConfig>
}
