package icu.windea.pls.core.findUsages

import com.intellij.find.findUsages.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

class ParadoxFindUsagesHandlerFactory(project: Project) : FindUsagesHandlerFactory(){
    val findOptions = ParadoxFindUsagesOptions(project)
    val definitionFindOptions = ParadoxDefinitionFindUsagesOptions(project)
    val localisationFindOptions = ParadoxLocalisationFindUsagesOptions(project)
    
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

class ParadoxFindUsagesHandler(
    element: PsiElement,
    factory: ParadoxFindUsagesHandlerFactory
) : FindUsagesHandler(element) {
    
}

class ParadoxDefinitionFindUsagesHandler(
    element: ParadoxScriptDefinitionElement,
    factory: ParadoxFindUsagesHandlerFactory
) : FindUsagesHandler(element) {
    
}

class ParadoxLocalisationFindUsagesHandler(
    element: ParadoxLocalisationProperty,
    factory: ParadoxFindUsagesHandlerFactory
) : FindUsagesHandler(element) {
    
}