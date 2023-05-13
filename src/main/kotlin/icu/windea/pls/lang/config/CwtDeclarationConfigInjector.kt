package icu.windea.pls.lang.config

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*

/**
 * 某些情况下，需要基于上下文对CWT规则进行添加、删除和修改。
 */
@WithGameTypeEP
interface CwtDeclarationConfigInjector {
    /**
     * 是否支持此上下文。
     */
    fun supports(configContext: CwtConfigContext): Boolean
    
    /**
     * 处理得到合并后的声明规则的缓存的键。
     */
    fun handleCacheKey(cacheKey: String, configContext: CwtConfigContext): String? = null
    
    /**
     * 替代默认实现，使用另外的逻辑获取合并后的声明规则。
     *
     * 需要保证当注入时所有规则列表都是可变的。
     */
    fun getDeclarationMergedConfig(configContext: CwtConfigContext): CwtPropertyConfig? = null
    
    /**
     * 在获取合并的声明规则之后对其进行额外的处理。
     * @return 是否继续使用下一个支持此上下文的规则注入器进行处理。
     */
    fun handleDeclarationMergedConfig(declarationConfig: CwtPropertyConfig, configContext: CwtConfigContext) {}
    
    /**
     * 替换CWT规则表达式。如果不需要，则返回null。
     */
    fun replaceConfigExpression(configExpression: String, configContext: CwtConfigContext): String? = null
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<CwtDeclarationConfigInjector>("icu.windea.pls.declarationConfigInjector")
        
        fun getDeclarationMergedConfig(configContext: CwtConfigContext, injectors: List<CwtDeclarationConfigInjector> = EP_NAME.extensionList): CwtPropertyConfig? {
            if(injectors.isEmpty()) return null
            val gameType = configContext.configGroup.gameType
            return injectors.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.getDeclarationMergedConfig(configContext)
            }
        }
        
        fun handleDeclarationMergedConfig(declarationConfig: CwtPropertyConfig, configContext: CwtConfigContext, injectors: List<CwtDeclarationConfigInjector> = EP_NAME.extensionList) {
            if(injectors.isEmpty()) return
            val gameType = configContext.configGroup.gameType
            injectors.forEach f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f
                ep.handleDeclarationMergedConfig(declarationConfig, configContext)
            }
        }
        
        fun handleCacheKey(cacheKey: String, configContext: CwtConfigContext, injectors: List<CwtDeclarationConfigInjector> = EP_NAME.extensionList): String? {
            if(injectors.isEmpty()) return null
            val gameType = configContext.configGroup.gameType
            return injectors.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.handleCacheKey(cacheKey, configContext)
            }
        }
    }
}