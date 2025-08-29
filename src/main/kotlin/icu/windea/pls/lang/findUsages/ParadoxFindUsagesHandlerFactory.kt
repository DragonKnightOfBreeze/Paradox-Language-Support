package icu.windea.pls.lang.findUsages

import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesHandlerFactory
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.localisationInfo
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class ParadoxFindUsagesHandlerFactory(project: Project) : FindUsagesHandlerFactory() {
    val findOptions = ParadoxFindUsagesOptions(project)
    val findDefinitionOptions = ParadoxDefinitionFindUsagesOptions(project)
    val findLocalisationOptions = ParadoxLocalisationFindUsagesOptions(project)

    override fun canFindUsages(element: PsiElement): Boolean {
        return element.language is ParadoxBaseLanguage
    }

    override fun createFindUsagesHandler(element: PsiElement, forHighlightUsages: Boolean): FindUsagesHandler {
        return when {
            element is ParadoxScriptDefinitionElement && element.definitionInfo != null -> {
                ParadoxDefinitionFindUsagesHandler(element, this)
            }
            element is ParadoxLocalisationProperty && element.localisationInfo != null -> {
                ParadoxLocalisationFindUsagesHandler(element, this)
            }
            else -> {
                ParadoxFindUsagesHandler(element, this)
            }
        }
    }
}
