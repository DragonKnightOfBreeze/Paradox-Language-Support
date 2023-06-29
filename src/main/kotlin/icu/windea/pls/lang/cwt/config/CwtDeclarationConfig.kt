package icu.windea.pls.lang.cwt.config

import com.google.common.cache.*
import com.intellij.openapi.application.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.cwt.*

class CwtDeclarationConfig(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val name: String,
    val propertyConfig: CwtPropertyConfig, //definitionName = ...
) : CwtConfig<CwtProperty> {
    //use soft values to optimize memory
    private val configCache: Cache<String, CwtPropertyConfig> = CacheBuilder.newBuilder().softValues().buildCache()
    
    private val subtypesToDistinctCache by lazy {
        val result = sortedSetOf<String>()
        propertyConfig.processDescendants {
            if(it is CwtPropertyConfig) {
                val subtypeExpression = it.key.removeSurroundingOrNull("subtype[", "]")
                if(subtypeExpression != null) {
                    val resolved = ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression)
                    resolved.subtypes.forEach { (_, subtype) -> result.add(subtype) }
                }
            }
            true
        }
        result
    }
    
    /**
     * 得到根据子类型列表进行合并后的配置。
     */
    fun getConfig(configContext: CwtDeclarationConfigContext): CwtPropertyConfig {
        //定义的值不为代码块的情况
        if(!propertyConfig.isBlock) return propertyConfig
        
        val cacheKey = getCacheKey(configContext)
        return configCache.getOrPut(cacheKey) {
            runReadAction {
                val config = doGetConfig(configContext)
                CwtDeclarationConfigInjector.handleDeclarationMergedConfig(config, configContext, configContext.injectors)
                config
            }
        }
    }
    
    private fun getCacheKey(configContext: CwtDeclarationConfigContext): String {
        val cacheKeyFromInjectors = CwtDeclarationConfigInjector.getCacheKey(configContext, configContext.injectors)
        if(cacheKeyFromInjectors != null) return cacheKeyFromInjectors
        
        //optimized
        return buildString {
            if(configContext.definitionSubtypes != null) {
                var isFirst = true
                subtypesToDistinctCache.forEach { s ->
                    if(configContext.definitionSubtypes.contains(s)) {
                        if(isFirst) isFirst = false else append('.')
                        append(s)
                    }
                }
            }
            append('#').append(configContext.matchOptions)
        }
    }
    
    private fun doGetConfig(configContext: CwtDeclarationConfigContext): CwtPropertyConfig {
        val injectedResult = CwtDeclarationConfigInjector.getDeclarationMergedConfig(configContext, configContext.injectors)
        if(injectedResult != null) return injectedResult
        
        val configs = propertyConfig.configs?.flatMap { it.deepCopyConfigsInDeclarationConfig(configContext) }
        return propertyConfig.copy(configs = configs)
        //here propertyConfig.parent should be null
    }
}
