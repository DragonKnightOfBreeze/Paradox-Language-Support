package icu.windea.pls.config.config

import com.google.common.cache.*
import com.intellij.openapi.application.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.config.*

data class CwtDeclarationConfig(
    override val pointer: SmartPsiElementPointer<CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val name: String,
    val propertyConfig: CwtPropertyConfig, //definitionName = ...
) : CwtConfig<CwtProperty> {
    private val mergedConfigCache: Cache<String, CwtPropertyConfig> by lazy { CacheBuilder.newBuilder().buildCache() }
    
    /**
     * 得到根据子类型列表进行合并后的配置。
     */
    fun getMergedConfig(configContext: CwtConfigContext): CwtPropertyConfig {
        //定义的值不为代码块的情况
        if(!propertyConfig.isBlock) return propertyConfig
        
        val (_, _, _, subtypes, _, matchOptions) = configContext
        var cacheKey = getRawCacheKey(subtypes, matchOptions)
        cacheKey = CwtDeclarationConfigInjector.getCacheKey(cacheKey, configContext, configContext.injectors) ?: cacheKey
        
        return mergedConfigCache.getOrPut(cacheKey) {
            runReadAction {
                val r = doGetMergedConfig(configContext)
                CwtDeclarationConfigInjector.handleDeclarationMergedConfig(r, configContext, configContext.injectors)
                r.putUserData(CwtMemberConfig.Keys.configContextKey, configContext)
                r
            }
        }
    }
    
    private fun getRawCacheKey(subtypes: List<String>?, matchOptions: Int): String {
        //optimized
        return buildString {
            if(subtypes != null) {
                var isFirst = true
                subtypes.sorted().forEachFast { s ->
                    if(isFirst) isFirst = false else append('.')
                    append(s)
                }
            }
            append('#').append(matchOptions)
        }
    }
    
    private fun doGetMergedConfig(configContext: CwtConfigContext): CwtPropertyConfig {
        val injectedResult = CwtDeclarationConfigInjector.getDeclarationMergedConfig(configContext, configContext.injectors)
        if(injectedResult != null) return injectedResult
        
        val configs = propertyConfig.configs?.flatMap { it.deepMergeConfigs(configContext) }
        return propertyConfig.copy(configs = configs)
        //here propertyConfig.parent should be null
    }
}

val CwtMemberConfig.Keys.configContextKey by lazy { Key.create<CwtConfigContext>("cwt.config.context") }