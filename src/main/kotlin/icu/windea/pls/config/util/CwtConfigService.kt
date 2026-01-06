package icu.windea.pls.config.util

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.originalConfig
import icu.windea.pls.config.config.overriddenProvider
import icu.windea.pls.config.configContext.CwtConfigContext
import icu.windea.pls.config.configContext.CwtDeclarationConfigContext
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.ep.config.CwtConfigPostProcessor
import icu.windea.pls.ep.config.CwtInjectedConfigProvider
import icu.windea.pls.ep.config.CwtOverriddenConfigProvider
import icu.windea.pls.ep.config.CwtRelatedConfigProvider
import icu.windea.pls.ep.configContext.CwtConfigContextProvider
import icu.windea.pls.ep.configContext.CwtDeclarationConfigContextProvider
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.resolve.ParadoxMemberService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.script.psi.ParadoxScriptMember

object CwtConfigService {
    /**
     * @see CwtConfigPostProcessor.postProcess
     */
    fun postProcess(config: CwtMemberConfig<*>) {
        val gameType = config.configGroup.gameType
        CwtConfigPostProcessor.EP_NAME.extensionList.forEach f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            if (!ep.supports(config)) return@f
            if (ep.deferred(config)) {
                val deferredActions = CwtConfigResolverManager.getPostProcessActions(config.configGroup)
                deferredActions += Runnable { ep.postProcess(config) }
            } else {
                ep.postProcess(config)
            }
        }
    }

    /**
     * @see CwtInjectedConfigProvider.injectConfigs
     */
    fun injectConfigs(parentConfig: CwtMemberConfig<*>, configs: MutableList<CwtMemberConfig<*>>): Boolean {
        val gameType = parentConfig.configGroup.gameType
        var r = false
        CwtInjectedConfigProvider.EP_NAME.extensionList.forEach f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            r = r || ep.injectConfigs(parentConfig, configs)
        }
        return r
    }

    /**
     * @see CwtOverriddenConfigProvider.getOverriddenConfigs
     */
    fun <T : CwtMemberConfig<*>> getOverriddenConfigs(contextElement: PsiElement, config: T): List<T> {
        val gameType = config.configGroup.gameType
        return CwtOverriddenConfigProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getOverriddenConfigs(contextElement, config).orNull()
                ?.onEach {
                    it.originalConfig = config
                    it.overriddenProvider = ep
                }
        }.orEmpty()
    }

    /**
     * @see CwtRelatedConfigProvider.getRelatedConfigs
     */
    fun getRelatedConfigs(file: PsiFile, offset: Int): Collection<CwtConfig<*>> {
        val gameType = selectGameType(file) ?: return emptySet()
        val result = mutableSetOf<CwtConfig<*>>()
        CwtRelatedConfigProvider.EP_NAME.extensionList.forEach f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f
            val r = ep.getRelatedConfigs(file, offset)
            result += r
        }
        return result
    }

    /**
     * @see CwtConfigContextProvider.getContext
     */
    fun getConfigContext(element: ParadoxScriptMember): CwtConfigContext? {
        val file = element.containingFile ?: return null
        val memberPath = ParadoxMemberService.getPath(element) ?: return null
        val gameType = selectGameType(file)
        return CwtConfigContextProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getContext(element, memberPath, file)?.also { it.provider = ep }
        }
    }

    /**
     * @see CwtDeclarationConfigContextProvider.getContext
     */
    fun getDeclarationConfigContext(element: PsiElement, definitionName: String?, definitionType: String, definitionSubtypes: List<String>?, configGroup: CwtConfigGroup): CwtDeclarationConfigContext? {
        val gameType = configGroup.gameType
        return CwtDeclarationConfigContextProvider.EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            if (!PlsAnnotationManager.check(ep, gameType)) return@f null
            ep.getContext(element, definitionName, definitionType, definitionSubtypes, configGroup)?.also { it.provider = ep }
        }
    }
}
