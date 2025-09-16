package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtTypeLocalisationConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 类型本地化规则。
 *
 * 概述：
 * - 为某个“定义类型”的不同子类型声明对应的本地化位置配置列表，便于在 UI 与文档中展示。
 * - 由 `type_localisation[...]`（或等效扩展）中的条目解析而来。
 *
 * @property locationConfigs 子类型表达式与位置规则的配对列表（`Pair<subtypeExpression?, CwtLocationConfig>`）。
 */
interface CwtTypeLocalisationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val locationConfigs: List<Pair<String?, CwtLocationConfig>> // (subtypeExpression, locationConfig)

    /** 按给定的 [subtypes] 合并与筛选后，返回生效的本地化位置规则列表。*/
    fun getConfigs(subtypes: List<String>): List<CwtLocationConfig>

    interface Resolver {
        /** 由成员属性规则解析为类型本地化规则。*/
        fun resolve(config: CwtPropertyConfig): CwtTypeLocalisationConfig?
    }

    companion object : Resolver by CwtTypeLocalisationConfigResolverImpl()
}
