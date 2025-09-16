package icu.windea.pls.config.config

import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.impl.CwtPropertyConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType

/**
 * 属性型成员规则。
 *
 * 概述：
 * - 对应 `.cwt` 中形如 `key = value` 的属性条目，承载键、值（及其类型）、分隔符、子成员与选项。
 * - 暴露键侧的规则表达式 [keyExpression]，且本规则的 [configExpression] 等同于该表达式。
 *
 * @property key 属性键原文。
 * @property separatorType 分隔符类型（`=`/`:` 等）。
 * @property valueConfig 当值一侧进一步展开为结构（对象/数组）时，对应的值规则；否则为 null。
 * @property keyExpression 键侧规则表达式；[configExpression] 等同于该表达式。
 */
interface CwtPropertyConfig : CwtMemberConfig<CwtProperty> {
    val key: String
    val separatorType: CwtSeparatorType

    val valueConfig: CwtValueConfig?

    val keyExpression: CwtDataExpression
    override val configExpression: CwtDataExpression get() = keyExpression

    interface Resolver {
        /**
         * 依据 [pointer]/[configGroup]/[key]/[value] 等解析生成规则；[valueType] 默认为字符串，[separatorType] 默认为 `=`，可携带下级 [configs]/[optionConfigs]。
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

        /** 构造一个委托版本（wrapper），共享来源与上下文，仅按需覆盖部分字段。 */
        fun delegated(
            targetConfig: CwtPropertyConfig,
            configs: List<CwtMemberConfig<*>>? = targetConfig.configs,
            parentConfig: CwtMemberConfig<*>? = targetConfig.parentConfig
        ): CwtPropertyConfig

        /** 基于现有规则，快速替换 `key` 与 `value`，用于生成变体。 */
        fun delegatedWith(
            targetConfig: CwtPropertyConfig,
            key: String,
            value: String
        ): CwtPropertyConfig

        /** 拷贝一个新的属性规则，可选择性修改若干字段。 */
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

    companion object: Resolver by CwtPropertyConfigResolverImpl()
}
