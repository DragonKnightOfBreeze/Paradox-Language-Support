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
import icu.windea.pls.model.ParadoxGameType

/**
 * CWT声明规则上下文。
 */
class CwtDeclarationConfigContext(
    //val element: PsiElement, //unused yet
    val definitionName: String?,
    val definitionType: String,
    val definitionSubtypes: List<String>?,
    val gameType: ParadoxGameType,
    val configGroup: CwtConfigGroup,
) : UserDataHolderBase() {
    /**
     * 得到根据子类型列表进行合并后的CWT声明规则。
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

//cacheKey -> declarationConfig
//use soft values to optimize memory
//depends on config group
private val CwtConfigGroup.declarationConfigCache by createKey(CwtDeclarationConfigContext.Keys) {
    createCachedValue(project) {
        CacheBuilder().softValues().build<String, CwtPropertyConfig>().cancelable().withDependencyItems()
    }
}

var CwtDeclarationConfigContext.provider: CwtDeclarationConfigContextProvider? by createKey(CwtDeclarationConfigContext.Keys)

