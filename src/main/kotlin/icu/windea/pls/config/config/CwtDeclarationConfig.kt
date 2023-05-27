package icu.windea.pls.config.config

import com.google.common.cache.*
import com.intellij.openapi.application.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.config.*

class CwtDeclarationConfig(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val name: String,
    val propertyConfig: CwtPropertyConfig, //definitionName = ...
) : CwtConfig<CwtProperty> {
    private val mergedConfigCache: Cache<String, CwtPropertyConfig> by lazy { CacheBuilder.newBuilder().buildCache() }
    
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
    fun getMergedConfig(configContext: CwtConfigContext): CwtPropertyConfig {
        //定义的值不为代码块的情况
        if(!propertyConfig.isBlock) return propertyConfig
        
        val cacheKey = getCacheKey(configContext)
        return mergedConfigCache.getOrPut(cacheKey) {
            runReadAction {
                val r = doGetMergedConfig(configContext)
                CwtDeclarationConfigInjector.handleDeclarationMergedConfig(r, configContext, configContext.injectors)
                r.putUserData(CwtMemberConfig.Keys.configContextKey, configContext)
                r
            }
        }
    }
    
    private fun getCacheKey(configContext: CwtConfigContext): String {
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
    
    private fun doGetMergedConfig(configContext: CwtConfigContext): CwtPropertyConfig {
        val injectedResult = CwtDeclarationConfigInjector.getDeclarationMergedConfig(configContext, configContext.injectors)
        if(injectedResult != null) return injectedResult
        
        val configs = propertyConfig.configs?.flatMap { it.deepMergeConfigs(configContext) }
        return propertyConfig.copy(configs = configs)
        //here propertyConfig.parent should be null
    }
}

val CwtMemberConfig.Keys.configContextKey by lazy { Key.create<CwtConfigContext>("cwt.config.context") }