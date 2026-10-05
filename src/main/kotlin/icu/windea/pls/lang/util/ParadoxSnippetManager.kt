package icu.windea.pls.lang.util

import icu.windea.pls.config.CwtDataTypeSets
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
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * @see CwtDataTypeSets.Snippet
 * @see ParadoxSnippetElement
 */
@Optimized
object ParadoxSnippetManager {
    /**
     * 得到定义引用片段关联的定义。即按模板参数解析得到的完整引用文本对应的定义。
     */
    fun getRelatedDefinitions(element: ParadoxDefinitionSnippetLightElement): List<ParadoxDefinitionElement> {
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
     * 得到本地化引用片段关联的本地化。即按模板参数解析得到的完整引用文本对应的本地化。
     */
    fun getRelatedLocalisations(element: ParadoxLocalisationSnippetLightElement): List<ParadoxLocalisationProperty> {
        val name = element.name.orNull() ?: return emptyList()
        val snippetTemplates = element.snippetTemplates.orNull() ?: return emptyList()
        val project = element.project
        val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        return snippetTemplates.flatMapFast { template ->
            ParadoxLocalisationSearch.searchNormal(template.resolve(name), selector).findAll()
        }
    }
}
