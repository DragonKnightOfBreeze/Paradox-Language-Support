package icu.windea.pls.lang.util

import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.ParadoxSnippetElement
import icu.windea.pls.lang.psi.light.ParadoxDefinitionSnippetLightElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationSnippetLightElement
import icu.windea.pls.lang.resolve.ParadoxSnippetService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * @see ParadoxSnippetElement
 * @see ParadoxDefinitionSnippetLightElement
 * @see ParadoxLocalisationSnippetLightElement
 */
@Optimized
object ParadoxSnippetManager {
    // region Related Items

    /**
     * 得到定义引用片段关联的定义。即按模板参数解析得到的完整引用文本对应的定义。
     *
     * 备注：目前不经过缓存。
     *
     * @see ParadoxSnippetService.resolveRelatedDefinitions
     */
    fun getRelatedDefinitions(element: ParadoxDefinitionSnippetLightElement): List<ParadoxDefinitionElement> {
        return ParadoxSnippetService.resolveRelatedDefinitions(element)
    }

    /**
     * 得到本地化引用片段关联的本地化。即按模板参数解析得到的完整引用文本对应的本地化。
     *
     * 备注：目前不经过缓存。
     *
     * @see ParadoxSnippetService.resolveRelatedLocalisations
     */
    fun getRelatedLocalisations(element: ParadoxLocalisationSnippetLightElement, preferredLocale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): List<ParadoxLocalisationProperty> {
        return ParadoxSnippetService.resolveRelatedLocalisations(element, preferredLocale)
    }

    // endregion
}
