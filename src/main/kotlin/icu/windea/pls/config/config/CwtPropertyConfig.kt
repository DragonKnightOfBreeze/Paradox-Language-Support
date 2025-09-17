package icu.windea.pls.config.config

import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.impl.CwtPropertyConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType

/**
 * 属性规则（属性型成员规则）。
 *
 * 对应 CWT 规则文件中的一个属性（`k = v` 或 `k = { ... }`）。
 *
 * @property key 属性键（去除首尾的双引号）。
 * @property separatorType 分隔符类型。
 * @property value 属性值（去除首尾的双引号）。
 * @property valueType 属性值的类型，用于驱动解析与校验。
 * @property valueConfig 属性值对应的值规则。懒加载，且在属性值无法解析时返回 null。
 * @property keyExpression 属性键对应的数据表达式，用于驱动解析与校验。
 * @property valueExpression 属性值对应的数据表达式，用于驱动解析与校验。
 * @property configExpression 绑定到该规则的数据表达式（等同于 [keyExpression]）。
 *
 * @see CwtProperty
 */
interface CwtPropertyConfig : CwtMemberConfig<CwtProperty> {
    val key: String
    val separatorType: CwtSeparatorType
    override val value: String
    override val valueType: CwtType

    val valueConfig: CwtValueConfig?

    val keyExpression: CwtDataExpression
    override val valueExpression: CwtDataExpression
    override val configExpression: CwtDataExpression get() = keyExpression

    interface Resolver {
        /**
         * 依据 [pointer]/[configGroup]/[key]/[value] 等解析生成规则；
         * [valueType] 默认为字符串，[separatorType] 默认为 `=`，可携带下级 [configs]/[optionConfigs]。
         */
        fun resolve(
            pointer: SmartPsiElementPointer<out CwtProperty>,
            configGroup: CwtConfigGroup,
            key: String,
            value: String,
            valueType: CwtType = CwtType.String,
            separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
            configs: List<CwtMemberConfig<*>>? = null,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = null
        ): CwtPropertyConfig

        /**
         * 构造一个委托版本（wrapper），共享来源与上下文，仅按需覆盖部分字段。
         */
        fun delegated(
            targetConfig: CwtPropertyConfig,
            configs: List<CwtMemberConfig<*>>? = targetConfig.configs,
            parentConfig: CwtMemberConfig<*>? = targetConfig.parentConfig
        ): CwtPropertyConfig

        /**
         * 基于现有规则，快速替换 `key` 与 `value`，用于生成变体。
         */
        fun delegatedWith(
            targetConfig: CwtPropertyConfig,
            key: String,
            value: String
        ): CwtPropertyConfig

        /**
         * 拷贝一个新的属性规则，可选择性修改若干字段。
         */
        fun copy(
            targetConfig: CwtPropertyConfig,
            pointer: SmartPsiElementPointer<out CwtProperty> = targetConfig.pointer,
            key: String = targetConfig.key,
            value: String = targetConfig.value,
            valueType: CwtType = targetConfig.valueType,
            separatorType: CwtSeparatorType = targetConfig.separatorType,
            configs: List<CwtMemberConfig<*>>? = targetConfig.configs,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = targetConfig.optionConfigs
        ): CwtPropertyConfig
    }

    companion object : Resolver by CwtPropertyConfigResolverImpl()
}
