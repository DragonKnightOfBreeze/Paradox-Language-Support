package icu.windea.pls.config.config

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.util.Key
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.option.CwtOptionDataHolder
import icu.windea.pls.config.option.CwtOptionDataHolderBase
import icu.windea.pls.config.option.CwtOptionDataProvider
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.CwtMemberConfigVisitor
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.createPointer
import icu.windea.pls.core.deoptimized
import icu.windea.pls.core.emptyPointer
import icu.windea.pls.core.optimized
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.lang.codeInsight.type
import icu.windea.pls.model.CwtMemberType
import icu.windea.pls.model.CwtMembersType
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.model.forCwtType

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

    override val memberType: CwtMemberType get() = CwtMemberType.VALUE

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

// region Implementations

private class CwtValueConfigResolverImpl : CwtValueConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(element: CwtValue, file: CwtFile, configGroup: CwtConfigGroup): CwtValueConfig {
        // - use `EmptyPointer` for default project to optimize memory
        // - 2.1.1 use `valueExpression` as constructor argument and field directly to optimize performance

        val pointer = if (configGroup.project.isDefault) emptyPointer() else element.createPointer(file)
        val configs = CwtConfigResolverManager.getConfigs(element, file, configGroup)
        val valueExpression = if (configs == null) CwtDataExpression.resolveValue(element.value) else CwtDataExpression.resolveBlock()
        val valueType = element.type
        val config = create(pointer, configGroup, valueExpression, valueType, configs, injectable = true)
        val optionConfigs = CwtConfigResolverManager.getOptionConfigs(element)
        CwtOptionDataProvider.process(config.optionData, optionConfigs) // initialize option data
        logger.trace { "Resolved value config (value: ${config.value}).".withLocationPrefix(element) }
        return config
    }

    override fun resolveFromPropertyConfig(
        pointer: SmartPsiElementPointer<out CwtValue>,
        propertyConfig: CwtPropertyConfig,
    ): CwtValueConfig {
        val config = CwtValueConfigFromPropertyConfig(pointer, propertyConfig)
        propertyConfig.optionData.copyTo(config) // inherit option data from property config
        return config
    }

    override fun create(
        pointer: SmartPsiElementPointer<out CwtValue>,
        configGroup: CwtConfigGroup,
        valueExpresssion: CwtDataExpression,
        valueType: CwtType,
        configs: List<CwtMemberConfig<*>>?,
        propertyConfig: CwtPropertyConfig?,
        injectable: Boolean,
    ): CwtValueConfig {
        val withConfigs = configs != null && (injectable || configs.isNotEmpty()) // 2.0.6 NOTE configs may be injectable
        val config = when (withConfigs) {
            true -> CwtValueConfigImplWithConfigs(pointer, configGroup, propertyConfig)
                .also { it.configs = configs.optimized() } // optimized to optimize memory
            else -> CwtValueConfigImpl(pointer, configGroup, valueExpresssion, valueType, propertyConfig)
        }
        return config
    }

    override fun createMock(configGroup: CwtConfigGroup, value: String): CwtValueConfig {
        return CwtValueConfigMock(configGroup, CwtDataExpression.resolveValue(value))
    }

    override fun copy(
        sourceConfig: CwtValueConfig,
        pointer: SmartPsiElementPointer<out CwtValue>,
        valueExpression: CwtDataExpression,
        valueType: CwtType,
        configs: List<CwtMemberConfig<*>>?,
        propertyConfig: CwtPropertyConfig?,
    ): CwtValueConfig {
        val config = create(pointer, sourceConfig.configGroup, valueExpression, valueType, configs, propertyConfig, injectable = true)
        return config
    }
}

private const val blockValue = PlsStrings.blockFolder
private val blockValueTypeId = CwtType.Block.optimized(OptimizerRegistry.forCwtType())

// 12 + 2 * 4 = 20 -> 24
private sealed class CwtValueConfigBase : CwtOptionDataHolderBase(), CwtValueConfig {
    override val optionData: CwtOptionDataHolder get() = this

    @Volatile override var parentConfig: CwtMemberConfig<*>? = null

    override val configExpression: CwtDataExpression get() = valueExpression

    override fun postProcess() {
        // bind parent config
        this.configs?.forEachFast { it.parentConfig = this }
        // run post processors
        CwtConfigService.postProcess(this)
        // collect information
        CwtConfigResolverManager.collectFromConfigExpression(this, valueExpression)
    }

    override fun postOptimize() {
        // bind parent config
        this.configs?.forEachFast { it.parentConfig = this }
    }

    override fun delegated(configs: List<CwtMemberConfig<*>>?): CwtValueConfig {
        val withConfigs = configs != null  // 2.0.6 NOTE configs may be injectable
        val config = when (withConfigs) {
            true -> CwtValueConfigDelegateWithConfigs(this)
                .also { it.configs = configs } // do not do optimization here
            else -> CwtValueConfigDelegate(this)
        }
        return config
    }

    override fun delegatedWith(value: String): CwtValueConfig {
        return CwtValueConfigDelegateWithValue(this, value)
    }

    override fun toString() = "(value) $value"
}

// 12 + 5 * 4 = 32 -> 32
private sealed class CwtValueConfigImplBase(
    override val pointer: SmartPsiElementPointer<out CwtValue>,
    override val configGroup: CwtConfigGroup,
    override val propertyConfig: CwtPropertyConfig?,
) : CwtValueConfigBase()

// 12 + 1 * 1 + 6 * 4 = 37 -> 40
private open class CwtValueConfigImpl(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    override val valueExpression: CwtDataExpression, // as constructor argument and field directly
    valueType: CwtType,
    propertyConfig: CwtPropertyConfig?,
) : CwtValueConfigImplBase(pointer, configGroup, propertyConfig) {
    private val valueTypeId = valueType.optimized(OptimizerRegistry.forCwtType()) // optimized to optimize memory

    override val value: String get() = valueExpression.expressionString
    override val valueType: CwtType get() = valueTypeId.deoptimized(OptimizerRegistry.forCwtType())
    override val configs: List<CwtMemberConfig<*>>? get() = if (valueTypeId == blockValueTypeId) emptyList() else null
}

// 12 + 7 * 4 = 40 -> 40
private open class CwtValueConfigImplWithConfigs(
    pointer: SmartPsiElementPointer<out CwtValue>,
    configGroup: CwtConfigGroup,
    propertyConfig: CwtPropertyConfig?,
) : CwtValueConfigImplBase(pointer, configGroup, propertyConfig) {
    override val value: String get() = blockValue
    override val valueType: CwtType get() = CwtType.Block

    @Volatile override var configs: List<CwtMemberConfig<*>> = emptyList()
    @Volatile private var membersType: CwtMembersType = CwtMembersType.MIXED

    override val properties: List<CwtPropertyConfig>
        get() {
            if (membersType == CwtMembersType.UNSET) membersType = CwtConfigResolverManager.getMembersType(configs)
            return CwtConfigResolverManager.getProperties(configs, membersType)
        }
    override val values: List<CwtValueConfig>
        get() {
            if (membersType == CwtMembersType.UNSET) membersType = CwtConfigResolverManager.getMembersType(configs)
            return CwtConfigResolverManager.getValues(configs, membersType)
        }

    override val valueExpression: CwtDataExpression get() = CwtDataExpression.resolveBlock()

    override fun withConfigs(configs: List<CwtMemberConfig<*>>): Boolean {
        this.configs = configs.optimized() // optimized to optimize memory
        return true
    }

    override fun postProcess() {
        // optimize child configs
        this.membersType = CwtMembersType.UNSET
        // call super
        super.postProcess()
    }

    override fun postOptimize() {
        // optimize child configs
        this.configs = this.configs.optimized()
        this.membersType = CwtMembersType.UNSET
        // call super
        super.postOptimize()
    }
}

// 12 + 4 * 4 = 28 -> 32
private class CwtValueConfigMock(
    override val configGroup: CwtConfigGroup,
    override val valueExpression: CwtDataExpression, // as constructor argument and field directly
) : CwtValueConfigBase() {
    override val pointer: SmartPsiElementPointer<out CwtValue> get() = emptyPointer()
    override val propertyConfig: CwtPropertyConfig? get() = null

    override val value: String get() = valueExpression.expressionString
    override val valueType: CwtType get() = CwtType.String
    override val configs: List<CwtMemberConfig<*>>? get() = null
}

// 12 + 3 * 4 = 24 -> 24
private open class CwtValueConfigDelegate(
    private val delegate: CwtValueConfig
) : CwtValueConfigBase() {
    override val pointer: SmartPsiElementPointer<out CwtValue> get() = delegate.pointer
    override val configGroup: CwtConfigGroup get() = delegate.configGroup
    override val value: String get() = delegate.value
    override val valueType: CwtType get() = delegate.valueType
    override val configs: List<CwtMemberConfig<*>>? get() = delegate.configs
    override val optionData: CwtOptionDataHolder get() = delegate.optionData
    override val propertyConfig: CwtPropertyConfig? get() = delegate.propertyConfig

    override val valueExpression: CwtDataExpression get() = delegate.valueExpression

    override fun <T> getUserData(key: Key<T>) = delegate.getUserData(key) ?: super.getUserData(key)
    override fun <T> putUserData(key: Key<T>, value: T?) = super.putUserData(key, value)
}

// 12 + 5 * 4 = 32 -> 32
private class CwtValueConfigDelegateWithConfigs(
    delegate: CwtValueConfig
) : CwtValueConfigDelegate(delegate) {
    override val value: String get() = blockValue
    override val valueType: CwtType get() = CwtType.Block

    @Volatile override var configs: List<CwtMemberConfig<*>> = emptyList()
    @Volatile private var membersType: CwtMembersType = CwtMembersType.MIXED

    override val properties: List<CwtPropertyConfig>
        get() {
            if (membersType == CwtMembersType.UNSET) membersType = CwtConfigResolverManager.getMembersType(configs)
            return CwtConfigResolverManager.getProperties(configs, membersType)
        }
    override val values: List<CwtValueConfig>
        get() {
            if (membersType == CwtMembersType.UNSET) membersType = CwtConfigResolverManager.getMembersType(configs)
            return CwtConfigResolverManager.getValues(configs, membersType)
        }

    override fun withConfigs(configs: List<CwtMemberConfig<*>>): Boolean {
        this.configs = configs.optimized() // optimized to optimize memory
        return true
    }

    override fun postOptimize() {
        // optimize child configs
        this.configs = this.configs.optimized()
        this.membersType = CwtMembersType.UNSET
        // call super
        super.postOptimize()
    }
}

// 12 + 4 * 4 = 28 -> 32
private class CwtValueConfigDelegateWithValue(
    delegate: CwtValueConfig,
    value: String,
) : CwtValueConfigDelegate(delegate) {
    override val value: String get() = valueExpression.expressionString
    override val configs: List<CwtMemberConfig<*>>? get() = null // should be always null here

    override val valueExpression: CwtDataExpression = CwtDataExpression.resolveValue(value) // as field directly
}

// 12 + 4 * 4 = 28 -> 32
private class CwtValueConfigFromPropertyConfig(
    override val pointer: SmartPsiElementPointer<out CwtValue>,
    override val propertyConfig: CwtPropertyConfig,
) : CwtValueConfigBase() {
    override val configGroup: CwtConfigGroup get() = propertyConfig.configGroup
    override val value: String get() = propertyConfig.value
    override val valueType: CwtType get() = propertyConfig.valueType
    override val configs: List<CwtMemberConfig<*>>? get() = propertyConfig.configs

    override val valueExpression: CwtDataExpression get() = propertyConfig.valueExpression
}

// endregion
