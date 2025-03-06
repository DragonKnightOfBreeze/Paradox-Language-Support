package icu.windea.pls.config.configContext

import com.google.common.cache.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.configContext.*
import icu.windea.pls.model.*

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
private val CwtConfigGroup.declarationConfigCache by createKey(CwtConfigContext.Keys) {
    createCachedValue(project) {
        CacheBuilder.newBuilder().softValues().buildCache<String, CwtPropertyConfig>().withDependencyItems()
    }
}

var CwtDeclarationConfigContext.provider: CwtDeclarationConfigContextProvider? by createKey(CwtDeclarationConfigContext.Keys)

