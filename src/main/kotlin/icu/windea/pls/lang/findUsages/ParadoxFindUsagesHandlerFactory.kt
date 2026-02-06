package icu.windea.pls.lang.findUsages

import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesHandlerFactory
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.ParadoxLanguage
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxDefinitionElement

class ParadoxFindUsagesHandlerFactory(project: Project) : FindUsagesHandlerFactory() {
    val findOptions = ParadoxFindUsagesOptions(project)
    val findDefinitionOptions = ParadoxDefinitionFindUsagesOptions(project)
    val findLocalisationOptions = ParadoxLocalisationFindUsagesOptions(project)

    override fun canFindUsages(element: PsiElement): Boolean {
        return element.language is ParadoxLanguage
    }

    override fun createFindUsagesHandler(element: PsiElement, forHighlightUsages: Boolean): FindUsagesHandler {
        return when {
            element is ParadoxDefinitionElement && element.definitionInfo != null -> {
                ParadoxDefinitionFindUsagesHandler(element, this)
            }
            element is ParadoxLocalisationProperty && element.type != null -> {
                ParadoxLocalisationFindUsagesHandler(element, this)
            }
            else -> {
                ParadoxFindUsagesHandler(element, this)
            }
        }
    }
}
