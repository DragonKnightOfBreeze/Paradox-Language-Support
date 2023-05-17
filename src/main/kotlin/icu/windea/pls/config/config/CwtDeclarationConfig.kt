package icu.windea.pls.config.config

import com.google.common.cache.*
import com.intellij.openapi.application.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.config.*

data class CwtDeclarationConfig(
    override val pointer: SmartPsiElementPointer<CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val name: String,
    val propertyConfig: CwtPropertyConfig, //definitionName = ...
) : CwtConfig<CwtProperty> {
    companion object {
        private val mergedConfigCache: Cache<String, CwtPropertyConfig> by lazy { CacheBuilder.newBuilder().buildCache() }
        
        val configContextKey = Key.create<CwtConfigContext>("cwt.config.context")
    }
    
    /**
     * 得到根据子类型列表进行合并后的配置。
     */
    fun getMergedConfig(configContext: CwtConfigContext): CwtPropertyConfig {
        //定义的值不为代码块的情况
        if(!propertyConfig.isBlock) return propertyConfig
        
        val (_, _, type, subtypes, _, matchType) = configContext
        var cacheKey = buildString {
            append(type)
            if(subtypes != null) {
                append(".")
                append(subtypes.sorted().joinToString("."))
            }
            append("#").append(matchType)
        }
        cacheKey = CwtDeclarationConfigInjector.getCacheKey(cacheKey, configContext, configContext.injectors) ?: cacheKey
        
        return mergedConfigCache.getOrPut(cacheKey) {
            runReadAction {
                val r = doGetMergedConfig(configContext)
                CwtDeclarationConfigInjector.handleDeclarationMergedConfig(r, configContext, configContext.injectors)
                r.putUserData(configContextKey, configContext)
                r
            }
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