package icu.windea.pls.lang.util

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.ParadoxSnippetElement
import icu.windea.pls.lang.psi.light.ParadoxDefinitionSnippetLightElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationSnippetLightElement
import icu.windea.pls.lang.resolve.ParadoxSnippetService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * @see CwtDataTypeSets.Snippet
 * @see ParadoxSnippetElement
 */
@Optimized
object ParadoxSnippetManager {
    /**
     * 得到定义引用片段关联的定义。即按模板参数解析得到的完整引用文本对应的定义。
     *
     * @see ParadoxSnippetService.resolveRelatedDefinitions
     */
    fun getRelatedDefinitions(element: ParadoxDefinitionSnippetLightElement): List<ParadoxDefinitionElement> {
        return ParadoxSnippetService.resolveRelatedDefinitions(element)
    }

    /**
     * 得到本地化引用片段关联的本地化。即按模板参数解析得到的完整引用文本对应的本地化。
     *
     * @see ParadoxSnippetService.resolveRelatedLocalisations
     */
    fun getRelatedLocalisations(element: ParadoxLocalisationSnippetLightElement): List<ParadoxLocalisationProperty> {
        return ParadoxSnippetService.resolveRelatedLocalisations(element)
    }
}
