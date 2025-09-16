package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtTypeImagesConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 类型图片规则。
 *
 * 概述：
 * - 为某个“定义类型”的不同子类型声明对应的图片位置配置列表，便于在 UI 与文档中展示。
 * - 由 `type_images[...]`（或等效扩展）中的条目解析而来。
 *
 * @property locationConfigs 子类型表达式与位置规则的配对列表（`Pair<subtypeExpression?, CwtLocationConfig>`）。
 */
interface CwtTypeImagesConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val locationConfigs: List<Pair<String?, CwtLocationConfig>> // (subtypeExpression, locationConfig)

    /** 按给定的 [subtypes] 合并与筛选后，返回生效的图片位置规则列表。*/
    fun getConfigs(subtypes: List<String>): List<CwtLocationConfig>

    interface Resolver {
        /** 由成员属性规则解析为类型图片规则。*/
        fun resolve(config: CwtPropertyConfig): CwtTypeImagesConfig?
    }

    companion object : Resolver by CwtTypeImagesConfigResolverImpl()
}
