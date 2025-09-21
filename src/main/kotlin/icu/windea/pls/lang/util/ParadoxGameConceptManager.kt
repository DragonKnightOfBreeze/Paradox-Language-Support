package icu.windea.pls.lang.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.core.annotations.WithGameType
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.ep.data.StellarisGameConceptData
import icu.windea.pls.lang.getData
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.filterBy
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptText
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.findProperty

@WithGameType(ParadoxGameType.Stellaris)
object ParadoxGameConceptManager {
    fun get(nameOrAlias: String, project: Project, contextElement: PsiElement? = null): ParadoxScriptDefinitionElement? {
        val definitionSelector = selector(project, contextElement).definition()
            .contextSensitive()
            .filterBy { it.name == nameOrAlias || it.getData<StellarisGameConceptData>()?.alias.orEmpty().contains(nameOrAlias) }
        return ParadoxDefinitionSearch.search(null, ParadoxDefinitionTypes.GameConcept, definitionSelector).find()
    }

    /**
     * * locationElement: [ParadoxScriptDefinitionElement]
     * * textElement: [ParadoxLocalisationConceptText] or [ParadoxLocalisationProperty]
     */
    fun getReferenceElementAndTextElement(element: ParadoxLocalisationConceptCommand): Tuple2<PsiElement?, PsiElement?> {
        val conceptText = element.conceptText
        run r1@{
            val resolved = element.reference?.resolve() ?: return@r1
            if (conceptText != null) return resolved to conceptText
            run r2@{
                val tooltipOverride = resolved.findProperty("tooltip_override", inline = true)
                    ?.propertyValue?.castOrNull<ParadoxScriptString>()
                    ?: return@r2
                val override = tooltipOverride.references.lastOrNull()?.resolve() ?: return@r2
                when {
                    override is ParadoxScriptDefinitionElement -> return resolved to ParadoxDefinitionManager.getPrimaryLocalisation(override)
                    override is ParadoxLocalisationProperty -> return resolved to override
                }
            }
            if(resolved !is ParadoxScriptDefinitionElement) return@r1
            return resolved to ParadoxDefinitionManager.getPrimaryLocalisation(resolved)
        }
        run r1@{
            val resolved = element.conceptName?.references?.lastOrNull()?.resolve()?.castOrNull<ParadoxScriptDefinitionElement>() ?: return@r1
            if (conceptText != null) return resolved to conceptText
            return resolved to ParadoxDefinitionManager.getPrimaryLocalisation(resolved)
        }
        return null to null
    }
}
