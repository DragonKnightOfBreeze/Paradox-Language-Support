package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtTypeImagesConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 类型图像规则：为某定义（definition）类型按子类型列举“图像位置”键的集合。
 *
 * - 每个位置由 `CwtLocationConfig` 表示，典型键如 `icon`、`portrait` 等。
 * - `locationConfigs` 可按子类型表达式进行覆盖/扩展，用于适配不同子类型的图像。
 *
 * 字段：
 * - `locationConfigs`: `(subtypeExpression, locationConfig)` 列表；当 `subtypeExpression` 为 `null` 时表示通用项。
 *
 * 行为：
 * - `getConfigs(subtypes)`: 根据给定子类型列表合并并返回最终位置配置。
 */
interface CwtTypeImagesConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val locationConfigs: List<Pair<String?, CwtLocationConfig>> // (subtypeExpression, locationConfig)

    /**
     * 得到根据子类型列表进行合并后的配置。
     */
    fun getConfigs(subtypes: List<String>): List<CwtLocationConfig>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtTypeImagesConfig?
    }

    companion object : Resolver by CwtTypeImagesConfigResolverImpl()
}
