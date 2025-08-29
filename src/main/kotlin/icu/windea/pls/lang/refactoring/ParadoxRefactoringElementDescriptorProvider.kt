package icu.windea.pls.lang.refactoring

import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.util.CommonRefactoringUtil
import com.intellij.refactoring.util.RefactoringDescriptionLocation
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

class ParadoxRefactoringElementDescriptorProvider : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
        if (location !is RefactoringDescriptionLocation) return null
        return when {
            element is ParadoxLocalisationProperty ->
                PlsBundle.message("refactoring.localisation.desc", CommonRefactoringUtil.htmlEmphasize(element.name))
            element is ParadoxScriptDefinitionElement && element.definitionInfo != null -> {
                val definitionInfo = element.definitionInfo ?: return null
                when {
                    definitionInfo.type == ParadoxDefinitionTypes.Sprite -> PlsBundle.message("refactoring.sprite.desc", CommonRefactoringUtil.htmlEmphasize(definitionInfo.name))
                    else -> PlsBundle.message("refactoring.definition.desc", CommonRefactoringUtil.htmlEmphasize(definitionInfo.name))
                }
            }
            element is PsiFile -> PlsBundle.message("refactoring.file.desc", CommonRefactoringUtil.htmlEmphasize(element.name))
            else -> null
        }
    }
}
