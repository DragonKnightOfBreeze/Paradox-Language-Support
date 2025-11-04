package icu.windea.pls.config.configContext

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.declarationConfigCacheKey
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.createCachedValue
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.cancelable
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.ep.configContext.CwtDeclarationConfigContextProvider

/**
 * 声明规则上下文。
 *
 * 用于后续获取处理后的声明规则，从而确定符合条件的定义声明的结构。
 *
 * @see CwtDeclarationConfigContext
 */
class CwtDeclarationConfigContext(
    val definitionName: String?,
    val definitionType: String,
    val definitionSubtypes: List<String>?,
    val configGroup: CwtConfigGroup,
) : UserDataHolderBase() {
    /**
     * 得到按子类型列表合并后的声明规则。
     */
    fun getConfig(declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val cache = configGroup.declarationConfigCache.value
        val cacheKey = ooGetCacheKey(declarationConfig)
        val cached = cache.get(cacheKey) {
            doGetConfig(declarationConfig).apply { declarationConfigCacheKey = cacheKey }
        }
        return cached
    }

    private fun ooGetCacheKey(declarationConfig: CwtDeclarationConfig): String {
        return provider!!.getCacheKey(this, declarationConfig)
    }

    private fun doGetConfig(declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        return provider!!.getConfig(this, declarationConfig)
    }

    object Keys : KeyRegistry()
}

private val CwtConfigGroup.declarationConfigCache by createKey(CwtDeclarationConfigContext.Keys) {
    createCachedValue(project) {
        // cacheKey -> declarationConfig
        // use soft values to optimize memory
        CacheBuilder().softValues().build<String, CwtPropertyConfig>().cancelable().withDependencyItems()
    }
}

var CwtDeclarationConfigContext.provider: CwtDeclarationConfigContextProvider? by createKey(CwtDeclarationConfigContext.Keys)

