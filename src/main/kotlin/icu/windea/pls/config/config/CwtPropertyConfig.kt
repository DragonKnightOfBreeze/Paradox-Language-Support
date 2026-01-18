package icu.windea.pls.config.config

import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.impl.CwtPropertyConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtMemberConfigVisitor
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.CwtMemberType
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType

/**
 * 属性规则（属性型成员规则）。
 *
 * 对应 CWT 规则文件中的一个属性（`k = v` 或 `k = {...}`）。
 *
 * @property key 属性键（去除首尾的双引号）。
 * @property value 属性值（去除首尾的双引号）。
 * @property valueType 属性值的类型，用于驱动解析与校验。
 * @property separatorType 分隔符类型。
 * @property valueConfig 属性值对应的值规则。懒加载，且在属性值无法解析时返回 null。
 * @property keyExpression 属性键对应的数据表达式，用于驱动解析与校验。
 * @property valueExpression 属性值对应的数据表达式，用于驱动解析与校验。
 * @property configExpression 绑定到该规则的数据表达式（等同于 [keyExpression]）。
 *
 * @see CwtProperty
 */
interface CwtPropertyConfig : CwtMemberConfig<CwtProperty> {
    val key: String
    override val value: String
    override val valueType: CwtType
    val separatorType: CwtSeparatorType

    val valueConfig: CwtValueConfig?

    val keyExpression: CwtDataExpression
    override val valueExpression: CwtDataExpression
    override val configExpression: CwtDataExpression

    override val memberType: CwtMemberType get() = CwtMemberType.PROPERTY

    override fun accept(visitor: CwtMemberConfigVisitor): Boolean {
        return visitor.visitProperty(this)
    }

    /** 创建基于当前规则的委托规则，并指定要替换的子规则列表。父规则会被重置为 `null`。 */
    override fun delegated(configs: List<CwtMemberConfig<*>>?): CwtPropertyConfig {
        throw UnsupportedOperationException()
    }

    /** 创建基于当前规则的委托规则，并指定要替换的值。父规则会被重置为 `null`。 */
    fun delegatedWith(key: String, value: String): CwtPropertyConfig {
        throw UnsupportedOperationException()
    }

    interface Resolver {
        /** 由 [CwtProperty] 解析为属性规则。 */
        fun resolve(element: CwtProperty, file: CwtFile, configGroup: CwtConfigGroup): CwtPropertyConfig?

        /** 创建属性规则。其中的选项数据仍然需要手动初始化。 */
        fun create(
            pointer: SmartPsiElementPointer<out CwtProperty>,
            configGroup: CwtConfigGroup,
            keyExpression: CwtDataExpression,
            valueExpression: CwtDataExpression,
            valueType: CwtType = CwtType.String,
            separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
            configs: List<CwtMemberConfig<*>>? = null,
            injectable: Boolean = false,
        ): CwtPropertyConfig

        /** 创建基于源规则 [sourceConfig] 的复制规则。其中的规则数据仍然需要手动合并。 */
        fun copy(
            sourceConfig: CwtPropertyConfig,
            pointer: SmartPsiElementPointer<out CwtProperty> = sourceConfig.pointer,
            keyExpression: CwtDataExpression = sourceConfig.keyExpression,
            valueExpression: CwtDataExpression = sourceConfig.valueExpression,
            valueType: CwtType = sourceConfig.valueType,
            separatorType: CwtSeparatorType = sourceConfig.separatorType,
            configs: List<CwtMemberConfig<*>>? = sourceConfig.configs,
        ): CwtPropertyConfig
    }

    companion object : Resolver by CwtPropertyConfigResolverImpl()
}
