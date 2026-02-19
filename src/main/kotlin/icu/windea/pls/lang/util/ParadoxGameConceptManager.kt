package icu.windea.pls.lang.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.ep.util.data.StellarisGameConceptData
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.getDefinitionData
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptCommand
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptText
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.propertyValue

@WithGameType(ParadoxGameType.Stellaris)
object ParadoxGameConceptManager {
    fun get(nameOrAlias: String, project: Project, contextElement: PsiElement? = null): ParadoxScriptProperty? {
        val definitionSelector = selector(project, contextElement).definition().contextSensitive()
        val fromName = ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.gameConcept, definitionSelector).find()
        if (fromName != null) return fromName
        val all = ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.gameConcept, definitionSelector).findAll()
        return all.find { nameOrAlias == getName(it) || nameOrAlias in getAlias(it) }
    }

    fun getName(element: ParadoxScriptProperty): String {
        return element.name // = element.definitionInfo?.name
    }

    fun getAlias(element: ParadoxScriptProperty): Set<String> {
        return element.getDefinitionData<StellarisGameConceptData>()?.alias.orEmpty()
    }

    /**
     * - locationElement: [ParadoxScriptProperty]
     * - textElement: [ParadoxLocalisationConceptText] or [ParadoxLocalisationProperty]
     */
    fun getReferenceElementAndTextElement(element: ParadoxLocalisationConceptCommand): Tuple2<PsiElement?, PsiElement?> {
        val conceptText = element.conceptText
        run r1@{
            val resolved = element.reference?.resolve() ?: return@r1
            if (resolved !is ParadoxScriptProperty) return@r1
            if (conceptText != null) return resolved to conceptText
            run r2@{
                val overrideProperty = selectScope { resolved.properties(inline = true).ofKey("tooltip_override").one() }
                val overrideValue = overrideProperty?.propertyValue<ParadoxScriptString>() ?: return@r2
                val override = overrideValue.references.lastOrNull()?.resolve() ?: return@r2
                when {
                    override is ParadoxScriptProperty -> return resolved to ParadoxDefinitionManager.getPrimaryLocalisation(override)
                    override is ParadoxLocalisationProperty -> return resolved to override
                }
            }
            return resolved to ParadoxDefinitionManager.getPrimaryLocalisation(resolved)
        }
        run r1@{
            val resolved = element.conceptName?.references?.lastOrNull()?.resolve()
            if (resolved !is ParadoxScriptProperty) return@r1
            if (conceptText != null) return resolved to conceptText
            return resolved to ParadoxDefinitionManager.getPrimaryLocalisation(resolved)
        }
        return null to null
    }
}
