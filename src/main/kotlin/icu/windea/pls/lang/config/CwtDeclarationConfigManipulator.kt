package icu.windea.pls.lang.config

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.cwt.config.*

/**
 * 某些情况下，需要基于上下文对CWT规则进行添加、删除和修改。
 */
@WithGameTypeEP
interface CwtDeclarationConfigManipulator {
    /**
     * 是否支持此上下文。
     */
    fun supports(configContext: CwtDeclarationConfigContext): Boolean
    
    /**
     * 得到需要的声明规则缓存的键.
     */
    fun getCacheKey(configContext: CwtDeclarationConfigContext): String? = null
    
    /**
     * 替代默认实现，使用另外的逻辑获取合并后的声明规则。
     *
     * 需要保证当注入时所有规则列表都是可变的。
     */
    fun getDeclarationMergedConfig(configContext: CwtDeclarationConfigContext): CwtPropertyConfig? = null
    
    /**
     * 在获取合并的声明规则之后对其进行额外的处理。
     */
    fun handleDeclarationMergedConfig(declarationConfig: CwtPropertyConfig, configContext: CwtDeclarationConfigContext) {}
    
    /**
     * 替换CWT规则表达式。如果不需要，则返回null。
     */
    fun replaceConfigExpression(configExpression: String, configContext: CwtDeclarationConfigContext): String? = null
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtDeclarationConfigManipulator>("icu.windea.pls.declarationConfigManipulator")
        
        fun getDeclarationMergedConfig(configContext: CwtDeclarationConfigContext, manipulators: List<CwtDeclarationConfigManipulator> = EP_NAME.extensionList): CwtPropertyConfig? {
            if(manipulators.isEmpty()) return null
            val gameType = configContext.configGroup.gameType
            return manipulators.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.getDeclarationMergedConfig(configContext)
            }
        }
        
        fun handleDeclarationMergedConfig(declarationConfig: CwtPropertyConfig, configContext: CwtDeclarationConfigContext, manipulators: List<CwtDeclarationConfigManipulator> = EP_NAME.extensionList) {
            if(manipulators.isEmpty()) return
            val gameType = configContext.configGroup.gameType
            manipulators.forEach f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f
                ep.handleDeclarationMergedConfig(declarationConfig, configContext)
            }
        }
        
        fun getCacheKey(configContext: CwtDeclarationConfigContext, manipulators: List<CwtDeclarationConfigManipulator> = EP_NAME.extensionList): String? {
            if(manipulators.isEmpty()) return null
            val gameType = configContext.configGroup.gameType
            return manipulators.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.getCacheKey(configContext)
            }
        }
    }
}