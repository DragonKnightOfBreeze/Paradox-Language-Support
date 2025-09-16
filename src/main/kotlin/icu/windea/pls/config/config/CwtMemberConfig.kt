package icu.windea.pls.config.config

import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.cwt.psi.CwtMemberElement
import icu.windea.pls.model.CwtType

/**
 * CWT config 的“成员型”抽象。
 *
 * 概述：
 * - 对应 `.cwt` 文件中的“成员条目”（属性或值），具备自身的值/类型、子 config 及选项（options）。
 * - 该抽象被具体的 [CwtPropertyConfig] 与 [CwtValueConfig] 继承。
 * - 同时提供经解析后的“规则表达式”：[valueExpression] 与 [configExpression]，用于匹配/校验/补全。
 *
 * 术语：
 * - 本项目中统一将“CWT 规则”翻译为“CWT config”。
 *
 * 参考：
 * - references/cwt/guidance.md
 * - docs/zh/config.md
 */
sealed interface CwtMemberConfig<out T : CwtMemberElement> : CwtConfig<T> {
    /** 原始值（字符串形式）。通常来自 `key = value` 或单值条目。 */
    val value: String
    /** 值的类型（标量/布尔/数值/数组等），用于驱动后续解析与校验。 */
    val valueType: CwtType
    /** 子 config 列表（成员下的嵌套属性/值）。无子项时为 null。 */
    val configs: List<CwtMemberConfig<*>>?
    /** 附加的选项 config 列表（以 `##` 形式声明）。无选项时为 null。 */
    val optionConfigs: List<CwtOptionMemberConfig<*>>?

    /** 指向父级成员 config（若存在）。用于向上溯源及继承/推断。 */
    var parentConfig: CwtMemberConfig<*>?

    /**
     * 成员值对应的“数据表达式”。
     *
     * - 对属性：对应于 `value` 所代表的表达式。
     * - 对值：对应于该值条目的表达式。
     */
    val valueExpression: CwtDataExpression
    /**
     * 当前 config 的“规则表达式”。
     *
     * - 对属性：等同于 [CwtPropertyConfig.keyExpression]。
     * - 对值：等同于 [valueExpression]。
     */
    override val configExpression: CwtDataExpression

    override fun toString(): String

    /** 成员 config 相关的用户数据键集合。 */
    object Keys : KeyRegistry()
}
