package icu.windea.pls.lang.util

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.ep.data.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.complex.nodes.*
import icu.windea.pls.script.psi.*

@WithGameType(ParadoxGameType.Stellaris)
object ParadoxGameConceptHandler {
    fun get(nameOrAlias: String, project: Project, contextElement: PsiElement? = null): ParadoxScriptDefinitionElement? {
        val definitionSelector = definitionSelector(project, contextElement)
            .contextSensitive()
            .filterBy { it.name == nameOrAlias || it.getData<StellarisGameConceptData>()?.alias.orEmpty().contains(nameOrAlias) }
        return ParadoxDefinitionSearch.search("game_concept", definitionSelector).find()
    }
    
    /**
     * @return [ParadoxLocalisationConceptText] or [ParadoxLocalisationProperty]
     */
    fun getTextElement(element: ParadoxLocalisationConcept): PsiElement? {
        val conceptText = element.conceptText
        if(conceptText != null) return conceptText
        run r1@{
            val resolved = element.reference?.resolve() ?: return@r1
            run r2@{
                val tooltipOverride = resolved.findProperty("tooltip_override", inline = true)
                    ?.propertyValue?.castOrNull<ParadoxScriptString>()
                    ?: return@r2
                val override = tooltipOverride.references.lastOrNull()?.resolve()
                when {
                    override is ParadoxScriptDefinitionElement -> return ParadoxDefinitionHandler.getPrimaryLocalisation(override)
                    override is ParadoxLocalisationProperty -> return override
                }
            }
            return ParadoxDefinitionHandler.getPrimaryLocalisation(resolved)
        }
        run r1@{
            val resolved = element.conceptName?.references?.findLast { it is ParadoxDatabaseObjectNode.Reference }
                ?.resolve()?.castOrNull<ParadoxScriptDefinitionElement>()
                ?: return@r1
            return ParadoxDefinitionHandler.getPrimaryLocalisation(resolved)
        }
        return null
    }
}
