package icu.windea.pls.config.config

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.resolved
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.cast
import icu.windea.pls.core.createPointer
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.deoptimizeValue
import icu.windea.pls.model.optimizeValue

/**
 * CWT 属性规则，形如 `key <sep> value`。
 *
 * - 当值类型为 [CwtType.Block] 时，该属性为块，可能包含子成员规则 [configs] 与选项 [optionConfigs]。
 * - 键/值对应的数据表达式分别为 [keyExpression] 与 [valueExpression]，其中 [configExpression] 等价于 [keyExpression]。
 *
 * @property key 属性名（键）。
 * @property separatorType 分隔符类型 [CwtSeparatorType]（如 `=`、`<` 等）。
 * @property valueConfig 值对应的规则对象（如果存在且可获取）。
 * @property keyExpression 键的数据表达式 [CwtDataExpression]，用于匹配与提示。
 * @property configExpression 规则的数据表达式，等价于 [keyExpression]。
 */
interface CwtPropertyConfig : CwtMemberConfig<CwtProperty> {
    val key: String
    val separatorType: CwtSeparatorType

    val valueConfig: CwtValueConfig?

    val keyExpression: CwtDataExpression
    override val configExpression: CwtDataExpression get() = keyExpression

    companion object Resolver
}

// Resolve Methods

fun CwtPropertyConfig.Resolver.resolve(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
    configs: List<CwtMemberConfig<*>>? = null,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = null
): CwtPropertyConfig {
    return if (configs != null) {
        if (optionConfigs != null) {
            CwtPropertyConfigImpl1(pointer, configGroup, key, value, valueType, separatorType, configs, optionConfigs)
        } else {
            CwtPropertyConfigImpl2(pointer, configGroup, key, value, valueType, separatorType, configs)
        }
    } else {
        if (optionConfigs != null) {
            CwtPropertyConfigImpl3(pointer, configGroup, key, value, valueType, separatorType, optionConfigs)
        } else {
            CwtPropertyConfigImpl4(pointer, configGroup, key, value, valueType, separatorType)
        }
    }
}

fun CwtPropertyConfig.delegated(
    configs: List<CwtMemberConfig<*>>? = this.configs,
    parentConfig: CwtMemberConfig<*>? = this.parentConfig
): CwtPropertyConfig {
    return if (configs != null) {
        CwtPropertyConfigDelegate1(this, configs).apply { this.parentConfig = parentConfig }
    } else {
        CwtPropertyConfigDelegate2(this).apply { this.parentConfig = parentConfig }
    }
}

fun CwtPropertyConfig.delegatedWith(key: String, value: String): CwtPropertyConfig {
    return CwtPropertyConfigDelegateWith(this, key, value)
}

fun CwtPropertyConfig.copy(
    pointer: SmartPsiElementPointer<out CwtProperty> = this.pointer,
    key: String = this.key,
    value: String = this.value,
    valueType: CwtType = this.valueType,
    separatorType: CwtSeparatorType = this.separatorType,
    configs: List<CwtMemberConfig<*>>? = this.configs,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = this.optionConfigs
): CwtPropertyConfig {
    return CwtPropertyConfig.resolve(pointer, this.configGroup, key, value, valueType, separatorType, configs, optionConfigs)
}

// Implementations (interned)

private abstract class CwtPropertyConfigImpl(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL
) : UserDataHolderBase(), CwtPropertyConfig {
    override val key = key.intern() // intern to optimize memory
    override val value = value.intern() // intern to optimize memory

    private val valueTypeId = valueType.optimizeValue() // use enum id as field to optimize memory
    override val valueType get() = valueTypeId.deoptimizeValue<CwtType>()

    private val separatorTypeId = separatorType.optimizeValue() // use enum id as field to optimize memory
    override val separatorType get() = separatorTypeId.deoptimizeValue<CwtSeparatorType>()

    // use memory-optimized lazy property
    @Volatile
    private var _valueConfig: Any? = EMPTY_OBJECT
    override val valueConfig @Synchronized get() = if (_valueConfig !== EMPTY_OBJECT) _valueConfig.cast() else getValueConfig().also { _valueConfig = it }

    override var parentConfig: CwtMemberConfig<*>? = null

    // not cached to optimize memory
    override val keyExpression get() = CwtDataExpression.resolve(key, true)
    override val valueExpression get() = if (isBlock) CwtDataExpression.resolveBlock() else CwtDataExpression.resolve(value, false)

    override fun toString() = "$key ${separatorType} $value"
}

// 12 + 9 * 4 + 2 * 1 = 50 -> 56
private class CwtPropertyConfigImpl1(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
    configs: List<CwtMemberConfig<*>>? = null,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
) : CwtPropertyConfigImpl(pointer, configGroup, key, value, valueType, separatorType) {
    override val configs = configs
    override val optionConfigs = optionConfigs
}

// 12 + 8 * 4 + 2 * 1 = 46 -> 48
private class CwtPropertyConfigImpl2(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
    configs: List<CwtMemberConfig<*>>? = null,
) : CwtPropertyConfigImpl(pointer, configGroup, key, value, valueType, separatorType) {
    override val configs = configs
    override val optionConfigs get() = null
}

// 12 + 8 * 4 + 2 * 1 = 46 -> 48
private class CwtPropertyConfigImpl3(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
    optionConfigs: List<CwtOptionMemberConfig<*>>? = null
) : CwtPropertyConfigImpl(pointer, configGroup, key, value, valueType, separatorType) {
    override val configs get() = if (valueType == CwtType.Block) emptyList<CwtMemberConfig<*>>() else null
    override val optionConfigs = optionConfigs
}

// 12 + 7 * 4 + 2 * 1 = 42 -> 48
private class CwtPropertyConfigImpl4(
    pointer: SmartPsiElementPointer<out CwtProperty>,
    configGroup: CwtConfigGroup,
    key: String,
    value: String,
    valueType: CwtType = CwtType.String,
    separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
) : CwtPropertyConfigImpl(pointer, configGroup, key, value, valueType, separatorType) {
    override val configs get() = if (valueType == CwtType.Block) emptyList<CwtMemberConfig<*>>() else null
    override val optionConfigs get() = null
}

private abstract class CwtPropertyConfigDelegate(
    private val delegate: CwtPropertyConfig,
) : UserDataHolderBase(), CwtPropertyConfig by delegate {
    // use memory-optimized lazy property
    @Volatile
    private var _valueConfig: Any? = EMPTY_OBJECT
    override val valueConfig @Synchronized get() = if (_valueConfig !== EMPTY_OBJECT) _valueConfig.cast() else getValueConfig().also { _valueConfig = it }

    override var parentConfig: CwtMemberConfig<*>? = null

    // not cached to optimize memory
    override val keyExpression get() = CwtDataExpression.resolve(key, true)
    override val valueExpression get() = if (isBlock) CwtDataExpression.resolveBlock() else CwtDataExpression.resolve(value, false)

    override fun <T : Any?> getUserData(key: Key<T>) = delegate.getUserData(key) ?: super.getUserData(key)
    override fun <T : Any?> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)

    override fun toString() = "$key ${separatorType.id} $value"
}

// 12 + 5 * 4 = 32 -> 32
private class CwtPropertyConfigDelegate1(
    delegate: CwtPropertyConfig,
    configs: List<CwtMemberConfig<*>>? = null,
) : CwtPropertyConfigDelegate(delegate) {
    override val configs = configs
}

// 12 + 4 * 4 = 28 -> 32
private class CwtPropertyConfigDelegate2(
    delegate: CwtPropertyConfig,
) : CwtPropertyConfigDelegate(delegate) {
    override val configs get() = if (valueType == CwtType.Block) emptyList<CwtMemberConfig<*>>() else null
}

// 12 + 6 * 4 = 36 -> 40
private class CwtPropertyConfigDelegateWith(
    delegate: CwtPropertyConfig,
    key: String,
    value: String,
    // configs should be always null here
) : CwtPropertyConfigDelegate(delegate) {
    override val key = key.intern() // intern to optimize memory
    override val value = value.intern() // intern to optimize memory
}

private fun CwtPropertyConfig.getValueConfig(): CwtValueConfig? {
    // this function should be enough fast because there are no pointers to be created
    val resolvedPointer = this.resolved().pointer
    val valuePointer = when {
        resolvedPointer is CwtPropertyPointer -> resolvedPointer.valuePointer
        else -> resolvedPointer.element?.propertyValue?.createPointer()
    } ?: return null
    return CwtValueConfig.resolveFromPropertyConfig(valuePointer, this)
}
