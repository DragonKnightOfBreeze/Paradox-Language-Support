package icu.windea.pls.lang.cwt.config

import com.google.common.cache.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
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
    
    val manipulators = CwtDeclarationConfigManipulator.EP_NAME.extensionList.filter { it.supports(this) }
    
    /**
     * 得到根据子类型列表进行合并后的配置。
     */
    fun getConfig(declarationConfig: CwtDeclarationConfig): CwtPropertyConfig {
        //处理定义的值不为代码块的情况
        if(!declarationConfig.propertyConfig.isBlock) return declarationConfig.propertyConfig
        
        val project = declarationConfig.info.configGroup.project
        val cache = project.declarationConfigCache.value
        val cacheKey = ooGetCacheKey(declarationConfig)
        return cache.getOrPut(cacheKey) {
            val config = doGetConfig(declarationConfig)
            CwtDeclarationConfigManipulator.handleDeclarationMergedConfig(config, this, manipulators)
            config.declarationConfigCacheKey = cacheKey
            config
        }
    }
    
    private fun ooGetCacheKey(declarationConfig: CwtDeclarationConfig): String {
        val cacheKeyFromManipulators = CwtDeclarationConfigManipulator.getCacheKey(this, manipulators)
        if(cacheKeyFromManipulators != null) return cacheKeyFromManipulators
        
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
        val injectedResult = CwtDeclarationConfigManipulator.getDeclarationMergedConfig(this, manipulators)
        if(injectedResult != null) return injectedResult
        
        val configs = declarationConfig.propertyConfig.configs?.flatMap { ParadoxConfigGenerator.deepCopyConfigsInDeclarationConfig(it, this) }
        return declarationConfig.propertyConfig.copy(configs = configs)
        //declarationConfig.propertyConfig.parent should be null here
    }
}

//project -> cacheKey -> declarationConfig
//use soft values to optimize memory
private val declarationConfigCache by createCachedValueKey<Cache<String, CwtPropertyConfig>>("cwt.declarationConfig.cache") {
    CacheBuilder.newBuilder().softValues().buildCache<String, CwtPropertyConfig>()
        .withDependencyItems()
}
private val Project.declarationConfigCache by PlsKeys.declarationConfigCache

val CwtMemberConfig.Keys.declarationConfigCacheKey by createKey<String>("cwt.declarationConfig.cacheKey")

var CwtMemberConfig<*>.declarationConfigCacheKey by CwtMemberConfig.Keys.declarationConfigCacheKey