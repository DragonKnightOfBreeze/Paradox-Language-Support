package icu.windea.pls.lang

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.data.impl.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

object ParadoxGameConceptHandler {
    fun get(nameOrAlias: String, project: Project, contextElement: PsiElement? = null): ParadoxScriptDefinitionElement? {
        val definitionSelector = definitionSelector(project, contextElement)
            .contextSensitive()
            .filterBy { it.name == nameOrAlias || it.getData<StellarisGameConceptDataProvider.Data>()?.alias.orEmpty().contains(nameOrAlias) }
        return ParadoxDefinitionSearch.search("game_concept", definitionSelector).findFirst()
    }
    
    fun getTextElement(element: ParadoxLocalisationConceptName): PsiElement? {
        val conceptText = element.siblings(true).findIsInstance<ParadoxLocalisationConceptText>()
        if(conceptText != null) return conceptText
        val resolved = element.reference?.resolve() ?: return null
        return ParadoxDefinitionHandler.getPrimaryLocalisation(resolved)
    }
}