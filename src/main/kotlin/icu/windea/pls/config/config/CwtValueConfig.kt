package icu.windea.pls.config.config

import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.impl.CwtValueConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.model.CwtType

/**
 * 值型成员规则。
 *
 * 概述：
 * - 对应 `.cwt` 中的“单独值条目”（非 `key = value` 形式），如：`some_value` 或在属性值一侧展开的嵌套值。
 * - 若该值来自某个属性的值一侧，允许通过 [propertyConfig] 反向关联其来源属性。
 * - 本规则的 [configExpression] 等同于值侧的规则表达式 [valueExpression]。
 *
 * @property propertyConfig 当该值由属性值一侧“展开/提升”为独立值规则时，指向其来源 [CwtPropertyConfig]；否则为 null。
 */
interface CwtValueConfig : CwtMemberConfig<CwtValue> {
    val propertyConfig: CwtPropertyConfig?

    override val configExpression: CwtDataExpression get() = valueExpression

    interface Resolver {
        /**
         * 依据 [pointer]/[configGroup]/[value] 等解析生成规则；[valueType] 默认为字符串，可携带下级 [configs]/[optionConfigs]，若来源为属性值侧可指定 [propertyConfig]。
         */
        fun resolve(
            pointer: SmartPsiElementPointer<out CwtValue>,
            configGroup: CwtConfigGroup,
            value: String,
            valueType: CwtType = CwtType.String,
            configs: List<CwtMemberConfig<*>>? = null,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
            propertyConfig: CwtPropertyConfig? = null
        ): CwtValueConfig

        /** 基于属性型成员规则，解析出其值侧对应的值型成员规则。 */
        fun resolveFromPropertyConfig(
            pointer: SmartPsiElementPointer<out CwtValue>,
            propertyConfig: CwtPropertyConfig
        ): CwtValueConfig

        /** 构造一个委托版本（wrapper），共享来源与上下文，仅按需覆盖部分字段。 */
        fun delegated(
            targetConfig: CwtValueConfig,
            configs: List<CwtMemberConfig<*>>? = targetConfig.configs,
            parentConfig: CwtMemberConfig<*>? = targetConfig.parentConfig,
        ): CwtValueConfig

        /** 基于现有规则，快速替换 `value`，用于生成变体。 */
        fun delegatedWith(
            targetConfig: CwtValueConfig,
            value: String
        ): CwtValueConfig

        /** 拷贝一个新的值型成员规则，可选择性修改若干字段。 */
        fun copy(
            targetConfig: CwtValueConfig,
            pointer: SmartPsiElementPointer<out CwtValue> = targetConfig.pointer,
            value: String = targetConfig.value,
            valueType: CwtType = targetConfig.valueType,
            configs: List<CwtMemberConfig<*>>? = targetConfig.configs,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = targetConfig.optionConfigs,
            propertyConfig: CwtPropertyConfig? = targetConfig.propertyConfig,
        ): CwtValueConfig
    }

    companion object: Resolver by CwtValueConfigResolverImpl()
}
