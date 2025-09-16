package icu.windea.pls.config.config

import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.impl.CwtValueConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.model.CwtType

/**
 * 值型成员 CWT config。
 *
 * 概述：
 * - 对应 `.cwt` 中的“单独值条目”（非 `key = value` 形式），如：`some_value` 或在属性值一侧展开的嵌套值。
 * - 若该值来自某个属性的值一侧，允许通过 [propertyConfig] 反向关联其来源属性。
 * - [configExpression] 等同于值侧的“规则表达式” [valueExpression]。
 *
 * 字段：
 * - [propertyConfig]：当该值是由属性值一侧“展开/提升”为独立值 config 时，指向其来源 [CwtPropertyConfig]；否则为 null。
 */
interface CwtValueConfig : CwtMemberConfig<CwtValue> {
    /** 来源属性 config（若存在）。*/
    val propertyConfig: CwtPropertyConfig?

    /** 值侧规则表达式。*/
    override val configExpression: CwtDataExpression get() = valueExpression

    interface Resolver {
        /**
         * 依据 PSI 指针与原始文本解析出值型成员 config。
         *
         * 参数：
         * - [pointer]：指向 `CwtValue` 的智能指针。
         * - [configGroup]：所属规则组。
         * - [value]：值原文。
         * - [valueType]：值类型，默认按字符串处理。
         * - [configs]：子成员列表，默认为空。
         * - [optionConfigs]：选项列表，默认为空。
         * - [propertyConfig]：来源属性（若该值由属性值侧提升而来）。
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

        /**
         * 基于属性型成员 config，解析出其值侧对应的值型成员 config。
         */
        fun resolveFromPropertyConfig(
            pointer: SmartPsiElementPointer<out CwtValue>,
            propertyConfig: CwtPropertyConfig
        ): CwtValueConfig

        /**
         * 构造一个委托版本（wrapper），共享来源与上下文，仅按需覆盖部分字段。
         */
        fun delegated(
            targetConfig: CwtValueConfig,
            configs: List<CwtMemberConfig<*>>? = targetConfig.configs,
            parentConfig: CwtMemberConfig<*>? = targetConfig.parentConfig,
        ): CwtValueConfig

        /**
         * 基于现有 config，快速替换 `value`，用于生成变体。
         */
        fun delegatedWith(
            targetConfig: CwtValueConfig,
            value: String
        ): CwtValueConfig

        /**
         * 拷贝一个新的值型成员 config，可选择性修改若干字段。
         */
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
