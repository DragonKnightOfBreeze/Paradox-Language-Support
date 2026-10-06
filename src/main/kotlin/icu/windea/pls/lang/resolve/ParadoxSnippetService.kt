package icu.windea.pls.lang.resolve

import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.flatMapFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.ParadoxSnippetElement
import icu.windea.pls.lang.psi.light.ParadoxDefinitionSnippetLightElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationSnippetLightElement
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * @see ParadoxSnippetElement
 * @see ParadoxDefinitionSnippetLightElement
 * @see ParadoxLocalisationSnippetLightElement
 */
@Optimized
object ParadoxSnippetService {
    /**
     * 解析定义引用片段关联的定义。即按模板参数解析得到的完整引用文本对应的定义。
     */
    fun resolveRelatedDefinitions(element: ParadoxDefinitionSnippetLightElement): List<ParadoxDefinitionElement> {
        val name = element.name.orNull() ?: return emptyList()
        val type = element.definitionType.orNull() ?: return emptyList()
        val snippetTemplates = element.snippetTemplates.orNull() ?: return emptyList()
        val project = element.project
        val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
        return snippetTemplates.flatMapFast { template ->
            ParadoxDefinitionSearch.searchElement(template.resolve(name), type, selector).findAll()
        }
    }

    /**
     * 解析本地化引用片段关联的本地化。即按模板参数解析得到的完整引用文本对应的本地化。
     */
    fun resolveRelatedLocalisations(element: ParadoxLocalisationSnippetLightElement, preferredLocale: CwtLocaleConfig = ParadoxLocaleManager.getPreferredLocaleConfig()): List<ParadoxLocalisationProperty> {
        val name = element.name.orNull() ?: return emptyList()
        val snippetTemplates = element.snippetTemplates.orNull() ?: return emptyList()
        val project = element.project
        val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive()
            .preferLocale(preferredLocale)
        return snippetTemplates.flatMapFast { template ->
            ParadoxLocalisationSearch.searchNormal(template.resolve(name), selector).findAll()
        }
    }
}
