package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.caseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.optimized
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.scope.ParadoxScopeContext

/**
 * 作用域分组规则。
 *
 * 用于分组作用域类型（scope type），便于在其他规则中按分组引用与校验。
 *
 * 路径定位：`scope_groups/{name}`，`{name}` 匹配规则名称（分组名）。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * scope_groups = {
 *     target_species = {
 *         country pop_group leader planet ship fleet army species first_contact
 *     }
 * }
 * ```
 *
 * @property name 名称（分组名）。
 * @property values 分组内的作用域 ID 集合（忽略大小写）。
 * @property valueConfigMap 每个作用域 ID 到其原始值规则的映射。
 *
 * @see ParadoxScope
 * @see ParadoxScopeContext
 */
interface CwtScopeGroupConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromProperty(": string[]")
    val values: Set<@CaseInsensitive String>

    val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>

    interface Resolver {
        /** 由属性规则解析为作用域分组规则。 */
        fun resolve(config: CwtPropertyConfig): CwtScopeGroupConfig?
    }

    companion object : Resolver by CwtScopeGroupConfigResolverImpl()
}

// region Implementations

private class CwtScopeGroupConfigResolverImpl : CwtScopeGroupConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtScopeGroupConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtScopeGroupConfig? {
        val name = config.key
        val valueElements = config.values
        if (valueElements == null) {
            logger.warn("Skipped invalid scope group config (name: $name): Null values.".withLocationPrefix(config))
            return null
        }
        if (valueElements.isEmpty()) {
            logger.debug { "Resolved scope group config with empty values (name: $name).".withLocationPrefix(config) }
            return CwtScopeGroupConfigImpl(config, name, emptySet(), emptyMap())
        }
        val values = caseInsensitiveStringSet() // ignore case
        val valueConfigMap = caseInsensitiveStringKeyMap<CwtValueConfig>() // ignore case
        for (valueElement in valueElements) {
            values.add(valueElement.value)
            valueConfigMap.put(valueElement.value, valueElement)
        }
        logger.debug { "Resolved scope group config (name: $name).".withLocationPrefix(config) }
        return CwtScopeGroupConfigImpl(config, name, values.optimized(), valueConfigMap.optimized())
    }
}

private class CwtScopeGroupConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val values: Set<String>,
    override val valueConfigMap: Map<String, CwtValueConfig>
) : UserDataHolderBase(), CwtScopeGroupConfig {
    override fun toString() = "CwtScopeGroupConfigImpl(name='$name')"
}

// endregion
