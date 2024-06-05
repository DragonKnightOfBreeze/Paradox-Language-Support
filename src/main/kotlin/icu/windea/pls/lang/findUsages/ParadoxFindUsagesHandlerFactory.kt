package icu.windea.pls.lang.findUsages

import com.intellij.find.findUsages.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

class ParadoxFindUsagesHandlerFactory(project: Project) : FindUsagesHandlerFactory() {
    val findOptions = ParadoxFindUsagesOptions(project)
    val findDefinitionOptions = ParadoxDefinitionFindUsagesOptions(project)
    val findLocalisationOptions = ParadoxLocalisationFindUsagesOptions(project)
    
    override fun canFindUsages(element: PsiElement): Boolean {
        return element.language.isParadoxLanguage()
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
