package icu.windea.pls.lang.cwt.config

import com.google.common.cache.*
import com.intellij.openapi.application.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.model.*

class CwtDeclarationConfigContext(
    element: PsiElement,
    val definitionName: String?,
    val definitionType: String,
    val definitionSubtypes: List<String>?,
    val configGroup: CwtConfigGroup,
    val matchOptions: Int = ParadoxConfigMatcher.Options.Default
) : UserDataHolderBase() {
    private val elementPointer = element.createPointer()
    
    val element: PsiElement? get() = elementPointer.element
    
    val injectors = CwtDeclarationConfigInjector.EP_NAME.extensionList.filter { it.supports(this) }
    
    companion object {
        //use soft values to optimize memory
        private val configCache: Cache<String, CwtPropertyConfig> = CacheBuilder.newBuilder().softValues().buildCache()
    }
    
    /**
     * 得到根据子类型列表进行合并后的配置。
     */
    fun getConfig(declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        //处理定义的值不为代码块的情况
        if(!declarationConfig.propertyConfig.isBlock) return declarationConfig.propertyConfig
        
        val cacheKey = ooGetCacheKey(declarationConfig)
        return configCache.getOrPut(cacheKey) {
            runReadAction {
                val config = doGetConfig(declarationConfig)
                CwtDeclarationConfigInjector.handleDeclarationMergedConfig(config, this, injectors)
                config.declarationConfigCacheKey = cacheKey
                config
            }
        }
    }
    
    private fun ooGetCacheKey(declarationConfig: CwtDeclarationConfig): String {
        val cacheKeyFromInjectors = CwtDeclarationConfigInjector.getCacheKey(this, injectors)
        if(cacheKeyFromInjectors != null) return cacheKeyFromInjectors
        
        //optimized
        return buildString {
            val gameTypeId = configGroup.gameType.id
            append(gameTypeId).append(':')
            append(matchOptions).append('#')
            append(definitionType)
            if(definitionSubtypes.isNotNullOrEmpty()) {
                append('.')
                var isFirst = true
                declarationConfig.subtypesToDistinct.forEach { s ->
                    if(definitionSubtypes.contains(s)) {
                        if(isFirst) isFirst = false else append('.')
                        append(s)
                    }
                }
            }
        }
    }
    
    private fun doGetConfig(declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        val injectedResult = CwtDeclarationConfigInjector.getDeclarationMergedConfig(this, injectors)
        if(injectedResult != null) return injectedResult
        
        val configs = declarationConfig.propertyConfig.configs?.flatMap { it.deepCopyConfigsInDeclarationConfig(this) }
        return declarationConfig.propertyConfig.copy(configs = configs)
        //declarationConfig.propertyConfig.parent should be null here
    }
}

val CwtMemberConfig.Keys.declarationConfigCacheKey by createKey<String>("cwt.declarationConfig.cacheKey")
var CwtMemberConfig<*>.declarationConfigCacheKey by CwtMemberConfig.Keys.declarationConfigCacheKey