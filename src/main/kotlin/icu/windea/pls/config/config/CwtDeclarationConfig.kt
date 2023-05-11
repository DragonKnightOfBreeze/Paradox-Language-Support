package icu.windea.pls.config.config

import com.google.common.cache.*
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
    }
    
    /**
     * 得到根据子类型列表进行合并后的配置。
     */
    fun getMergedConfig(configContext: CwtConfigContext): CwtPropertyConfig {
        //定义的值不为代码块的情况
        if(!propertyConfig.isBlock) return propertyConfig
        
        val (_, name, type, subtypes, _, matchType) = configContext
        val cacheKey = buildString {
            if(configContext.injectors.isNotEmpty()) {
                append(name).append(" ")
            }
            append(type).append(" ")
            if(subtypes != null) {
                append(subtypes.sorted().joinToString(","))
            } else {
                append("*")
            }
            append("#").append(matchType)
        }
        
        return mergedConfigCache.getOrPut(cacheKey) {
            val r = doGetMergedConfig(configContext)
            CwtDeclarationConfigInjector.handleDeclarationMergedConfig(r, configContext, configContext.injectors)
            r
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