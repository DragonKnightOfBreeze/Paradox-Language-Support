package icu.windea.pls.ep.scope

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.core.annotations.WithGameTypeEP
import icu.windea.pls.lang.supportsByAnnotation
import icu.windea.pls.model.ParadoxScopeContext
import icu.windea.pls.model.overriddenProvider

/**
 * 用于基于上下文为某些特定的脚本表达式提供重载后的作用域上下文。
 *
 * 这里获取的作用域上下文会覆盖原始的作用域上下文。
 */
@WithGameTypeEP
interface ParadoxOverriddenScopeContextProvider {
    /**
     * 基于指定的上下文PSI元素[contextElement]和对应的CWT规则[config]获取重载后的作用域上下文。
     */
    fun getOverriddenScopeContext(contextElement: PsiElement, config: CwtMemberConfig<*>, parentScopeContext: ParadoxScopeContext?): ParadoxScopeContext?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxOverriddenScopeContextProvider>("icu.windea.pls.overriddenScopeContextProvider")

        fun getOverriddenScopeContext(contextElement: PsiElement, config: CwtMemberConfig<*>, parentScopeContext: ParadoxScopeContext?): ParadoxScopeContext? {
            val gameType = config.configGroup.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (!gameType.supportsByAnnotation(ep)) return@f null
                ep.getOverriddenScopeContext(contextElement, config, parentScopeContext)
                    ?.also { it.overriddenProvider = ep }
            }
        }
    }
}
