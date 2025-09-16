package icu.windea.pls.config.config

import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.cwt.psi.CwtMemberElement
import icu.windea.pls.model.CwtType

/**
 * 规则的“成员型”抽象。
 *
 * 概述：
 * - 对应 `.cwt` 文件中的“成员条目”（属性或值），具备自身的值/类型、子规则与选项（options）。
 * - 由具体的 [CwtPropertyConfig] 与 [CwtValueConfig] 继承；并暴露经解析的规则表达式用于匹配/校验/补全。
 *
 * 参考：
 * - references/cwt/guidance.md
 * - docs/zh/config.md
 *
 * @property value 原始值（字符串），通常来自 `key = value` 或单值条目。
 * @property valueType 值的类型（标量/布尔/数值/数组等），用于驱动解析与校验。
 * @property configs 子规则列表（成员下的嵌套属性/值）；无子项时为 null。
 * @property optionConfigs 附加的选项规则列表（以 `##` 声明）；无选项时为 null。
 * @property parentConfig 指向父级成员规则（若存在），用于溯源与继承/推断。
 * @property valueExpression 成员值对应的“数据表达式”。
 * @property configExpression 当前规则的“规则表达式”（属性：等同于 [CwtPropertyConfig.keyExpression]；值：等同于 [valueExpression]）。
 */
sealed interface CwtMemberConfig<out T : CwtMemberElement> : CwtConfig<T> {
    val value: String
    val valueType: CwtType
    val configs: List<CwtMemberConfig<*>>?
    val optionConfigs: List<CwtOptionMemberConfig<*>>?

    var parentConfig: CwtMemberConfig<*>?

    val valueExpression: CwtDataExpression
    override val configExpression: CwtDataExpression

    override fun toString(): String

    object Keys : KeyRegistry()
}
