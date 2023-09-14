package icu.windea.pls.lang.overridden

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.model.*

/**
 * 用于为某些特定的脚本表达式提供重载后的作用域上下文。
 *
 * 这里获取的作用域上下文会覆盖原始的作用域上下文。
 */
@WithGameTypeEP
interface ParadoxOverriddenScopeContextProvider {
    /**
     * 从指定的上下文元素[contextElement]和对应的CWT规则[config]获取重载后的作用域上下文。
     */
    fun getOverriddenScopeContext(contextElement: PsiElement, config: CwtMemberConfig<*>,parentScopeContext: ParadoxScopeContext?): ParadoxScopeContext?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxOverriddenScopeContextProvider>("icu.windea.pls.overriddenScopeContextProvider")
        
        fun getOverriddenScopeContext(contextElement: PsiElement, config: CwtMemberConfig<*>, parentScopeContext: ParadoxScopeContext?): ParadoxScopeContext? {
            val gameType = config.info.configGroup.gameType ?: return null
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.getOverriddenScopeContext(contextElement, config, parentScopeContext)
                    ?.also { it.overriddenProvider = ep }
            }
        }
    }
}
