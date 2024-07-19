package icu.windea.pls.lang.util

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.data.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.expression.complex.nodes.*
import icu.windea.pls.script.psi.*

object ParadoxGameConceptHandler {
    fun get(nameOrAlias: String, project: Project, contextElement: PsiElement? = null): ParadoxScriptDefinitionElement? {
        val definitionSelector = definitionSelector(project, contextElement)
            .contextSensitive()
            .filterBy { it.name == nameOrAlias || it.getData<StellarisGameConceptDataProvider.Data>()?.alias.orEmpty().contains(nameOrAlias) }
        return ParadoxDefinitionSearch.search("game_concept", definitionSelector).find()
    }
    
    /**
     * @return [ParadoxLocalisationConceptText] or [ParadoxLocalisationProperty]
     */
    fun getTextElement(element: ParadoxLocalisationConcept): PsiElement? {
        val conceptText = element.conceptText
        if(conceptText != null) return conceptText
        run {
            val resolved = element.reference?.resolve() ?: return@run
            return ParadoxDefinitionHandler.getPrimaryLocalisation(resolved)
        }
        run {
            val resolved = element.conceptName?.references?.findLast { it is ParadoxDatabaseObjectNode.Reference }
                ?.resolve()?.castOrNull<ParadoxScriptDefinitionElement>()
                ?: return@run
            return ParadoxDefinitionHandler.getPrimaryLocalisation(resolved)
        }
        return null
    }
}
