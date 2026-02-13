package icu.windea.pls.lang.resolve

import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.util.CwtConfigExpressionManager
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextPlainRenderer
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

object ParadoxLocalisationService {
    fun resolveLocalizedText(element: ParadoxLocalisationProperty): String? {
        return ParadoxLocalisationTextPlainRenderer().render(element).orNull()
    }

    fun resolveRelatedScriptedVariables(element: ParadoxLocalisationProperty): List<ParadoxScriptScriptedVariable> {
        val name = element.name.orNull() ?: return emptyList()
        val project = element.project
        val gameType = selectGameType(element)
        if (gameType == null) return emptyList()
        val selector = selector(project, element).scriptedVariable().contextSensitive()
        ProgressManager.checkCanceled()
        // search for all scripted variable with same name
        val result = ParadoxScriptedVariableSearch.search(name, null, selector).findAll().toList()
        return result
    }

    fun resolveRelatedDefinitions(element: ParadoxLocalisationProperty): List<ParadoxDefinitionElement> {
        val name = element.name.orNull() ?: return emptyList()
        val project = element.project
        val gameType = selectGameType(element) ?: return emptyList()
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val patterns = configGroup.relatedLocalisationPatterns
        val namesToSearch = mutableSetOf<String>()
        patterns.forEach { (prefix, suffix) ->
            name.removeSurroundingOrNull(prefix, suffix)?.let { namesToSearch += it }
        }
        if (namesToSearch.isEmpty()) return emptyList()
        val selector = selector(project, element).definition().contextSensitive()
        val result = mutableListOf<ParadoxDefinitionElement>()
        namesToSearch.forEach f1@{ nameToSearch ->
            ProgressManager.checkCanceled()
            // NOTE 2.1.3 skip file definitions
            ParadoxDefinitionSearch.searchProperty(nameToSearch, null, selector).findAll().forEach f2@{ definition ->
                ProgressManager.checkCanceled()
                val definitionInfo = definition.definitionInfo ?: return@f2
                val definitionName = definitionInfo.name.orNull() ?: return@f2
                definitionInfo.localisations.forEach f3@{ l ->
                    val resolved = CwtConfigExpressionManager.resolvePlaceholder(l.locationExpression, definitionName) ?: return@f3
                    if (resolved != name) return@f3
                    result += definition
                    return@f2
                }
            }
        }
        return result
    }
}
