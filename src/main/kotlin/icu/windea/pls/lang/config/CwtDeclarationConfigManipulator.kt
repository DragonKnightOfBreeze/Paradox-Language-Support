package icu.windea.pls.lang.config

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*

/**
 * 用于基于规则上下文处理某些特定的CWT声明规则。
 */
@WithGameTypeEP
interface CwtDeclarationConfigManipulator {
    /**
     * 是否支持此上下文。
     */
    fun supports(context: CwtDeclarationConfigContext): Boolean
    
    /**
     * 得到需要的声明规则缓存的键.
     */
    fun getCacheKey(context: CwtDeclarationConfigContext): String? = null
    
    /**
     * 替代默认实现，使用另外的逻辑获取合并后的声明规则。
     *
     * 需要保证当注入时所有规则列表都是可变的。
     */
    fun getDeclarationMergedConfig(context: CwtDeclarationConfigContext): CwtPropertyConfig? = null
    
    /**
     * 在获取合并的声明规则之后对其进行额外的处理。
     */
    fun handleDeclarationMergedConfig(declarationConfig: CwtPropertyConfig, context: CwtDeclarationConfigContext) {}
    
    /**
     * 替换CWT规则表达式。如果不需要，则返回null。
     */
    fun replaceConfigExpression(configExpression: String, context: CwtDeclarationConfigContext): String? = null
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtDeclarationConfigManipulator>("icu.windea.pls.declarationConfigManipulator")
        
        fun getDeclarationMergedConfig(context: CwtDeclarationConfigContext, manipulators: List<CwtDeclarationConfigManipulator> = EP_NAME.extensionList): CwtPropertyConfig? {
            if(manipulators.isEmpty()) return null
            val gameType = context.configGroup.gameType
            return manipulators.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.getDeclarationMergedConfig(context)
            }
        }
        
        fun handleDeclarationMergedConfig(declarationConfig: CwtPropertyConfig, context: CwtDeclarationConfigContext, manipulators: List<CwtDeclarationConfigManipulator> = EP_NAME.extensionList) {
            if(manipulators.isEmpty()) return
            val gameType = context.configGroup.gameType
            manipulators.forEach f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f
                ep.handleDeclarationMergedConfig(declarationConfig, context)
            }
        }
        
        fun getCacheKey(context: CwtDeclarationConfigContext, manipulators: List<CwtDeclarationConfigManipulator> = EP_NAME.extensionList): String? {
            if(manipulators.isEmpty()) return null
            val gameType = context.configGroup.gameType
            return manipulators.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.getCacheKey(context)
            }
        }
    }
}