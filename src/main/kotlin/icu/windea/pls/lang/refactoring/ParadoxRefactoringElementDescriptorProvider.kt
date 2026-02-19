package icu.windea.pls.lang.refactoring

import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.util.CommonRefactoringUtil
import com.intellij.refactoring.util.RefactoringDescriptionLocation
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ParadoxRefactoringElementDescriptorProvider : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
        if (location !is RefactoringDescriptionLocation) return null
        return when {
            element is ParadoxScriptScriptedVariable -> {
                val name = element.name?.orNull() ?: return null
                PlsBundle.message("refactoring.scriptedVariable.desc", CommonRefactoringUtil.htmlEmphasize(name))
            }
            element is ParadoxDefinitionElement && element.definitionInfo != null -> {
                val definitionInfo = element.definitionInfo ?: return null
                if (definitionInfo.name.isEmpty()) return null // skip for anonymous definitions
                PlsBundle.message("refactoring.definition.desc", CommonRefactoringUtil.htmlEmphasize(definitionInfo.name))
            }
            element is ParadoxLocalisationProperty -> {
                val name = element.name.orNull() ?: return null
                PlsBundle.message("refactoring.localisation.desc", CommonRefactoringUtil.htmlEmphasize(name))
            }
            element is PsiFile -> {
                val text = element.name
                PlsBundle.message("refactoring.file.desc", CommonRefactoringUtil.htmlEmphasize(text))
            }
            else -> {
                null
            }
        }
    }
}
