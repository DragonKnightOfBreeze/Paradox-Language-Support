package icu.windea.pls.config.config

import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.impl.CwtPropertyConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType

/**
 * 属性型成员 CWT config。
 *
 * 概述：
 * - 对应 `.cwt` 中形如 `key = value` 的属性条目，承载键、值（及其类型）、分隔符、子成员与选项。
 * - 提供键侧的“规则表达式” [keyExpression]，并作为本 config 的 [configExpression] 参与匹配与校验。
 *
 * 字段：
 * - [key]：属性键的原文。
 * - [separatorType]：分隔符类型（`=`/`:` 等）。
 * - [valueConfig]：当值一侧进一步展开为结构（对象/数组）时，对应的值 config；否则为 null。
 * - [keyExpression]：键侧规则表达式；[configExpression] 等同于该表达式。
 */
interface CwtPropertyConfig : CwtMemberConfig<CwtProperty> {
    /** 属性键的原文。*/
    val key: String
    /** 分隔符类型（`=`/`:` 等）。*/
    val separatorType: CwtSeparatorType

    /** 当值一侧为结构时，解析得到的值 config；否则为 null。*/
    val valueConfig: CwtValueConfig?

    /** 键侧规则表达式。*/
    val keyExpression: CwtDataExpression
    override val configExpression: CwtDataExpression get() = keyExpression

    interface Resolver {
        /**
         * 依据 PSI 指针与原始文本解析出属性型成员 config。
         *
         * 参数：
         * - [pointer]：指向 `CwtProperty` 的智能指针。
         * - [configGroup]：所属规则组。
         * - [key]：属性键原文。
         * - [value]：属性值原文。
         * - [valueType]：属性值类型，默认按字符串处理。
         * - [separatorType]：分隔符类型，默认 `=`。
         * - [configs]：子成员列表，默认为空。
         * - [optionConfigs]：选项列表，默认为空。
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
         * 基于现有 config，快速替换 `key` 与 `value`，用于生成变体。
         */
        fun delegatedWith(
            targetConfig: CwtPropertyConfig,
            key: String,
            value: String
        ): CwtPropertyConfig

        /**
         * 拷贝一个新的属性 config，可选择性修改若干字段。
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

    companion object: Resolver by CwtPropertyConfigResolverImpl()
}
