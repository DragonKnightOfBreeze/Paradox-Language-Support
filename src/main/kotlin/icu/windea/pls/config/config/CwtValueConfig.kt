package icu.windea.pls.config.config

import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.config.impl.CwtValueConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtMemberConfigVisitor
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.model.CwtType

/**
 * 值规则（值型成员规则）。
 *
 * 对应 CWT 规则文件中的一个值（`v`）。可以是属性的值，也可以是单独的值。
 *
 * @property propertyConfig 对应属性的值时，所属的属性规则。
 * @property configExpression 绑定到该规则的数据表达式（等同于 [valueExpression]）。
 *
 * @see CwtValue
 */
interface CwtValueConfig : CwtMemberConfig<CwtValue> {
    val propertyConfig: CwtPropertyConfig?

    override val configExpression: CwtDataExpression

    override fun accept(visitor: CwtMemberConfigVisitor): Boolean {
        return visitor.visitValue(this)
    }

    /** 创建基于当前规则的委托规则，并指定要替换的子规则列表。父规则会被重置为 `null`。 */
    override fun delegated(configs: List<CwtMemberConfig<*>>?): CwtValueConfig {
        throw UnsupportedOperationException()
    }

    /** 创建基于当前规则的委托规则，并指定要替换的值。父规则会被重置为 `null`。 */
    fun delegatedWith(value: String): CwtValueConfig {
        throw UnsupportedOperationException()
    }

    interface Resolver {
        /** 由 [CwtValue] 解析为值规则。 */
        fun resolve(element: CwtValue, file: CwtFile, configGroup: CwtConfigGroup): CwtValueConfig

        /** 基于属性型成员规则，解析出其值侧对应的值型成员规则。 */
        fun resolveFromPropertyConfig(
            pointer: SmartPsiElementPointer<out CwtValue>,
            propertyConfig: CwtPropertyConfig,
        ): CwtValueConfig

        /** 创建值规则。其中的选项数据仍然需要手动初始化。 */
        fun create(
            pointer: SmartPsiElementPointer<out CwtValue>,
            configGroup: CwtConfigGroup,
            valueExpresssion: CwtDataExpression,
            valueType: CwtType = CwtType.String,
            configs: List<CwtMemberConfig<*>>? = null,
            propertyConfig: CwtPropertyConfig? = null,
            injectable: Boolean = false,
        ): CwtValueConfig

        /** 创建基于指定的字符串字面量 [value] 的模拟的值规则。使用空指针。 */
        fun createMock(configGroup: CwtConfigGroup, value: String): CwtValueConfig

        /** 创建基于源规则 [sourceConfig] 的复制规则。其中的选项数据仍然需要手动合并。 */
        fun copy(
            sourceConfig: CwtValueConfig,
            pointer: SmartPsiElementPointer<out CwtValue> = sourceConfig.pointer,
            valueExpression: CwtDataExpression = sourceConfig.valueExpression,
            valueType: CwtType = sourceConfig.valueType,
            configs: List<CwtMemberConfig<*>>? = sourceConfig.configs,
            propertyConfig: CwtPropertyConfig? = sourceConfig.propertyConfig,
        ): CwtValueConfig
    }

    companion object : Resolver by CwtValueConfigResolverImpl()
}
